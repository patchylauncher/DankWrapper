package com.eziosoft.DankWrapper;

import com.eziosoft.DankWrapper.lib.DankClassLoader;
import org.apache.commons.cli.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Launch {

    public static String workdir;
    public static DankClassLoader loader;
    public static List<URL> urlclasspath = new ArrayList<URL>();
    public static boolean isdebug = false;

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        System.out.println("Starting DankWrapper...");
        Options opts = new Options();
        Option dir = new Option("d", true, "directory to run game from");
        Option debug = new Option("de", false, "Enable features");
        debug.setOptionalArg(true);
        dir.setRequired(true);
        opts.addOption(dir);
        opts.addOption(debug);
        CommandLine cmd = new DefaultParser().parse(opts, args, true);
        workdir = cmd.getOptionValue("d");
        if (cmd.hasOption("de")){
            isdebug = !isdebug;
        }
        // do stuff here or something idk
        System.out.println(System.getProperty("java.class.path"));
        String[] cpsplit = System.getProperty("java.class.path").split(";");
        for (String s : cpsplit){
            urlclasspath.add(new File(s).toURI().toURL());
        }
        loader = new DankClassLoader(urlclasspath.toArray(new URL[]{}), DankClassLoader.class.getClassLoader());
        //Class<?> blenders = Class.forName("net.minecraft.client.Minecraft", true, loader);
        Class<?> blenders = loader.findClass("net.minecraft.client.Minecraft");
        blenders.getMethod("main", String[].class).invoke(null, (Object) Arrays.copyOfRange(args, isdebug ? 3 : 2, args.length));
    }
}
