package com.eziosoft.DankWrapper;

import com.eziosoft.DankWrapper.lib.DankClassLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Launch {

    public static String workdir = "H:/danktest";
    public static DankClassLoader loader;
    public static List<URL> urlclasspath = new ArrayList<URL>();

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("Starting DankWrapper...");
        // do stuff here or something idk
        System.out.println(System.getProperty("java.class.path"));
        String[] cpsplit = System.getProperty("java.class.path").split(";");
        List<String> libs = new ArrayList<String>();
        libs.add("H:/danktest/minecraft.jar");
        for (File f : new File("H:/danktest/libs").listFiles()){
            System.out.println("found " + f.getName());
            libs.add(f.getAbsolutePath());
        }
        for (String s : libs){
            urlclasspath.add(new File(s).toURI().toURL());
        }
        loader = new DankClassLoader(urlclasspath.toArray(new URL[]{}), DankClassLoader.class.getClassLoader());
        //Class<?> blenders = Class.forName("net.minecraft.client.Minecraft", true, loader);
        Class<?> blenders = loader.findClass("net.minecraft.client.Minecraft");
        blenders.getMethod("main", String[].class).invoke(null, (Object) new String[]{"e","${auth_session}", "--gameDir", "H:\\danktest","--assetsDir","${game_assets}"});
    }
}
