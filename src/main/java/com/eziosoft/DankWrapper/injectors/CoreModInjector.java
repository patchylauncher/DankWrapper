package com.eziosoft.DankWrapper.injectors;

import com.eziosoft.DankWrapper.Launch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CoreModInjector extends BasicInjector{

    private File gamedir;
    private Map<String, String> classes;
    private List<URL> cpitems;

    public CoreModInjector(){
        // nopackage = any of the obfuscated classes in the root of the minecraft jar
        this.targetclass = "nopackage";
        // get the game directory just in case
        this.shortarg = "d";
        this.desc = "Game Directory";
        this.required = true;
        this.hasOptions = true;
        this.acceptsParams = true;
        // other shit we need to do
        this.classes = new HashMap<String, String>();
    }
    @Override
    public byte[] inject(byte[] input) throws Exception {
        return input;
    }

    @Override
    public byte[] inject(byte[] in, String name) throws Exception {
        // first check and see if we even provide the class in the first place
        if (!classes.containsKey(name.concat(".class"))){
            if (Launch.isdebug) System.out.println("Coremod provider does not provide class " + name);
            return null;
        }
        System.out.println("Coremod provider is now loading class " + name);
        // ok, now we have to load the class in question from the jar file
        FileInputStream fin = new FileInputStream(this.classes.get(name.concat(".class")));
        ZipInputStream zipin = new ZipInputStream(fin);
        ZipEntry ent = zipin.getNextEntry();
        ByteArrayOutputStream out = null;
        boolean flag = false;
        while (ent != null){
            System.out.println(ent.getName());
            if (ent.getName().equals(name.concat(".class"))){
                // get the bytes!
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[(int) ent.getSize()];
                int read = 0;
                while ((read = zipin.read(buffer)) != -1){
                    out.write(buffer, 0, read);
                }
                // set flag
                flag = true;
            }
            zipin.closeEntry();
            if (!flag){
                ent = zipin.getNextEntry();
            } else {
                ent = null;
            }
        }
        // did we get anything? we can check the flag!
        if (!flag){
            throw new RuntimeException("Coremod Provider: Error loading class " + name);
        }
        // we did, cool, clean up first
        zipin.close();
        fin.close();
        try {
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    @Override
    public void AcceptArgument(String input) {
        // store the string as the new gamedir
        this.gamedir = new File(input);
        return;
    }

    @Override
    public void Initialize() {
        System.out.println("DankWrapper Coremod Injector Initializing...");
        System.out.println("Scanning for mods....");
        // init the mods folder into the classloader
        File dir = new File(gamedir, "mods");
        if (!dir.exists()){
            // make it and do nothing basically
            dir.mkdir();
        } else {
            // loop thru it and add every jar file
            this.cpitems = new ArrayList<URL>();
            for (File f : dir.listFiles()){
                try {
                    if (f.getName().endsWith(".jar")){
                        this.cpitems.add(f.toURI().toURL());
                    }
                } catch (Exception e){
                    System.err.println("Error processoring file " + f.getName());
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Scanning for coremods...");
        // open the folder
        File coremod = new File(gamedir, "coremods");
        if (!coremod.exists()){
            // make the folder
            coremod.mkdir();
            // itll be empty so return nothing
            System.out.println("coremods folder empty, no mods to process");
            return;
        } else {
            if (coremod.isFile()){
                throw new RuntimeException("Coremods folder is a file!");
            }
            List<File> mods = new ArrayList<File>();
            for (File f : coremod.listFiles()){
                if (f.getName().endsWith(".jar")){
                    System.out.println("Found coremod " + f.getName());
                    mods.add(f);
                } else if (f.getName().endsWith(".zip")){
                    System.out.println("Found zip file " + f.getName());
                    mods.add(f);
                } else {
                    System.out.println("Ignoring file " + f.getName());
                }
            }
            System.out.println("Found " + mods.size() + " coremods");
            // process all classes that we will provide
            for (File f : mods){
                try {
                    FileInputStream fin = new FileInputStream(f);
                    ZipInputStream zipin = new ZipInputStream(fin);
                    ZipEntry entry = zipin.getNextEntry();
                    while (entry != null){
                        if (this.classes.containsKey(entry.getName())){
                            throw new RuntimeException("Coremod " + f.getName() + " has class " + entry.getName() + " that is already provided by another coremod!");
                        }
                        if (entry.getName().endsWith(".class")){
                            this.classes.put(entry.getName(), f.getAbsolutePath());
                        }
                        zipin.closeEntry();
                        entry = zipin.getNextEntry();
                    }
                    zipin.close();
                    fin.close();
                } catch (Exception e){
                    System.err.println("Error processing coremod file " + f.getName());
                    e.printStackTrace();
                    continue;
                }
            }
            System.out.println("CoreModInjector now providing " + classes.size() + " classes");
            return;
        }
    }

    @Override
    public URL[] getClassPathItems() {
        return cpitems.toArray(cpitems.toArray(new URL[0]));
    }
}
