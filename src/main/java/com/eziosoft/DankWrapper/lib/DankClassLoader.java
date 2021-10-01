package com.eziosoft.DankWrapper.lib;

import com.eziosoft.DankWrapper.Launch;
import com.eziosoft.DankWrapper.Patches;

import java.io.IOException;
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

    private List<URL> urls = new ArrayList<URL>();
    private Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
    private URLClassLoader trash;

    public DankClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls.addAll(Arrays.asList(urls));
        this.trash = new URLClassLoader(Launch.urlclasspath.toArray(new URL[]{}), Patches.class.getClassLoader());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException{
        System.out.println("Attempting to load class " + name);
        if (cache.containsKey(name)){
            System.out.println("Class in cache, returning that version...");
            return cache.get(name);
        }
        // we cheat here by using a second class loader to bypass duplicate definition errors
        Class<?> req = trash.loadClass(name);
        byte[] patched = null;
        try {
            if ("net.minecraft.client.Minecraft".equals(name)) {
                patched = PatchCode.PatchDirImageIO(req, name);
            }
        } catch (Exception e){
            System.err.println("Error trying to patch class!");
            e.printStackTrace();
        }
        if (patched != null){
            // we have to get the signers for the code, too, otherwise it will combust and crash
            String filename = name.replace(".", "/").concat(".class");
            URLConnection url = findCodeSourceConnectionFor(filename);
            // init codesigning array
            CodeSigner[] sign = null;
            // get a class file and then get the signers from that
            if (url instanceof JarURLConnection){
                JarURLConnection jurlc = (JarURLConnection) url;
                JarFile jar;
                JarEntry ent = null;
                try {
                    jar = jurlc.getJarFile();
                    if (jar != null && jar.getManifest() != null){
                        ent = jar.getJarEntry(filename);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-2);
                }
                assert ent != null;
                sign = ent.getCodeSigners();
            }
            // ok that sucked but now we can sign the code correctly
            CodeSource code = url == null ? null : new CodeSource(url.getURL(), sign);
            Class<?> mod = defineClass(name, patched, 0, patched.length, code);
            cache.put(name, mod);
            return mod;
        } else {
            return req;
        }
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
}
