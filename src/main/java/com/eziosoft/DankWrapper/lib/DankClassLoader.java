package com.eziosoft.DankWrapper.lib;

import com.eziosoft.DankWrapper.Launch;
import com.eziosoft.DankWrapper.debug.NotAnErrorException;
import com.eziosoft.DankWrapper.injectors.BasicInjector;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DankClassLoader  extends URLClassLoader {

    private final Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private final URLClassLoader parent;
    private final Set<String> exceptions = new HashSet<String>();
    private final List<String> invalidClassCache = new ArrayList<String>();
    private List<BasicInjector> injectors = new ArrayList<BasicInjector>();

    public DankClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);
        this.parent = new URLClassLoader(urls, parent);

        // add exceptions
        this.exceptions.add("java.");
        this.exceptions.add("sun.");
        this.exceptions.add("org.lwjgl.");
        this.exceptions.add("org.apache.logging.");
        this.exceptions.add("com.eziosoft.DankWrapper.");
        this.exceptions.add("javax.");
        this.exceptions.add("org.w3c.");

    }

    public void setInjectors(List<BasicInjector> in){
        this.injectors = in;
    }

    public void addLoaderExceptions(String exc){
        this.exceptions.add(exc);
    }

    // sure, we ship default exceptions
    // but what if we want to REMOVE exceptions?
    public void removeLoaderException(String exception){
        this.exceptions.remove(exception);
    }

    private void closeSlient(Closeable c){
        if (c != null){
            try {
                c.close();
            } catch (Exception die){}
        }
    }

    private byte[] readClassBinary(String weed) {
        InputStream in = null;
        try {
            String path = weed.replaceAll("\\.", "/").concat(".class");
            URL rsc = parent.getResource(path);
            if (rsc == null){
                System.err.println("Warning: failed to get bytes for class " + weed);
                return null;
            }
            in = rsc.openStream();
            byte[] oof = IOUtils.toByteArray(in);
            return oof;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeSlient(in);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException{
        if (Launch.isdebug) System.out.println("Attempting to load class " + name);
        for (String s : exceptions){
            if (name.startsWith(s)){
                if (Launch.isdebug) System.out.println("Class is listed in exceptions, loading with parent...");
                return parent.loadClass(name);
            }
        }
        if (invalidClassCache.contains(name)){
            if (Launch.isdebug){
                System.err.println("Error: invalid class is listed in cache: " + name);
                try {
                    throw new NotAnErrorException("Dirity hack to print stack trace");
                } catch (NotAnErrorException e){
                    e.printStackTrace();
                }
            }
            throw new ClassNotFoundException("BRUH! Invalid class!");
        }
        if (cache.containsKey(name)){
            if (Launch.isdebug) System.out.println("Class in cache, returning that version...");
            return cache.get(name);
        }
        if (Launch.isdebug) System.out.println("Loading class manually...");
        // we cheat here by using a second class loader to bypass duplicate definition errors
        byte[] req = readClassBinary(name);
        byte[] patched = null;
        try {
            for (BasicInjector inject : injectors){
                if (inject.targetclass.equals(name)){
                    patched = inject.inject(req);
                } else if (!name.contains(".") && inject.targetclass.equals("nopackage")){
                    patched = inject.inject(req, name);
                }
            }
        } catch (Exception e){
            System.err.println("Error trying to patch class!");
            e.printStackTrace();
        }

        // we have to get the signers for the code, too, otherwise it will combust and crash
        String filename = name.replace(".", "/").concat(".class");
        URLConnection url = findCodeSourceConnectionFor(filename);
        // init codesigning array
        CodeSigner[] sign = null;
        // get a class file and then get the signers from that
        try {
            if (url instanceof JarURLConnection) {
                JarURLConnection jurlc = (JarURLConnection) url;
                JarFile jar;
                JarEntry ent = null;
                try {
                    jar = jurlc.getJarFile();
                    if (jar != null && jar.getManifest() != null) {
                        ent = jar.getJarEntry(filename);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-2);
                }
                assert ent != null;
                sign = ent.getCodeSigners();
            }
        } catch (NullPointerException e){
            if (Launch.isdebug) {
                System.err.println("Caught fatal error while trying to get code signers");
                System.err.println("Error is as follows");
                e.printStackTrace();
            }
        }
        // ok that sucked but now we can sign the code correctly
        CodeSource code = url == null ? null : new CodeSource(url.getURL(), sign);
        Class<?> clazz = null;
        if (patched != null) {
            clazz = defineClass(name, patched, 0, patched.length, code);
        } else {
            if (req != null) {
                clazz = defineClass(name, req, 0, req.length, code);
            } else {
                this.invalidClassCache.add(name);
                throw new ClassNotFoundException("Bruh: invalid/nonexistent class " + name);
            }
        }
        cache.put(name, clazz);
        return clazz;
    }

    private URLConnection findCodeSourceConnectionFor(final String name) {
        final URL resource = findResource(name);
        if (resource != null) {
            try {
                return resource.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (Launch.isdebug ) System.out.println("Loading class " + name);
        return findClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return findClass(name);
    }

    @Override
    public URL findResource(String resource){
        if (Launch.isdebug) System.out.println("findResource: " + resource);
        return parent.findResource(resource);
    }

    @Override
    public URL getResource(String resource){
        if (Launch.isdebug) System.out.println("getResource: " + resource);
        return parent.getResource(resource);
    }
}
