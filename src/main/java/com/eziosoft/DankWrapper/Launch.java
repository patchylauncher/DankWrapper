package com.eziosoft.DankWrapper;

import com.eziosoft.DankWrapper.injectors.BasicInjector;
import com.eziosoft.DankWrapper.lib.DankClassLoader;
import com.eziosoft.DankWrapper.skinpatch.SkinPatchFactory;
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
    public static DankClassLoader loader;
    public static List<URL> urlclasspath = new ArrayList<URL>();
    public static boolean isdebug = false;

    public static String skinid = "";

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ParseException {
        System.out.println("Starting DankWrapper...");
        // first we need to get all the injectors specified at the command line
        String[] inject = args[0].split(":");
        // get rid of the first argument
        args = Arrays.copyOfRange(args, 1, args.length);
        List<BasicInjector> injectors = new ArrayList<BasicInjector>();
        for (String s : inject){
            try {
                Class<?> clazz = Launch.class.getClassLoader().loadClass(s);
                BasicInjector b = (BasicInjector) clazz.getConstructor().newInstance();
                injectors.add(b);
            } catch (Exception e){
                System.err.println("Error while trying to load injector " + s);
                e.printStackTrace();
                System.exit(-1);
            }
        }
        System.out.println("Loaded " + injectors.size() + " injectors");
        // init the options now
        Options opts = new Options();
        int total = 0;
        for (BasicInjector b : injectors){
            if (!opts.hasShortOption(b.shortarg)) {
                if (b.hasOptions) {
                    Option opt = new Option(b.shortarg, b.acceptsParams, b.desc);
                    opt.setRequired(b.required);
                    opts.addOption(opt);
                    if (b.required) {
                        total++;
                        if (b.acceptsParams) {
                            total++;
                        }
                    }
                }
            } else {
                System.err.println("An injector tried to redefine the options " + b.shortarg);
            }
        }
        // for debug output if required
        Option debug = new Option("de", false, "Enable debug features");
        debug.setOptionalArg(true);
        opts.addOption(debug);
        // for skinpatching
        Option skin = new Option("s", true, "Enable SkinPatch");
        skin.setOptionalArg(true);
        opts.addOption(skin);
        // parse the options
        CommandLine cmd = new DefaultParser().parse(opts, args, true);
        for (BasicInjector b : injectors){
            if (b.hasOptions && b.acceptsParams){
                b.AcceptArgument(cmd.getOptionValue(b.shortarg));
            }
        }
        // debugging features if required!
        if (cmd.hasOption("de")){
            isdebug = !isdebug;
            total++;
        }
        // skinpatching flag
        if (cmd.hasOption("s")){
            URL.setURLStreamHandlerFactory(new SkinPatchFactory());
            total++;
            total++;
            skinid = cmd.getOptionValue("s");
        }
        // setup our custom classloader
        System.out.println(System.getProperty("java.class.path"));
        String[] cpsplit = System.getProperty("java.class.path").split(";");
        for (String s : cpsplit){
            urlclasspath.add(new File(s).toURI().toURL());
        }
        // init injectors and add whatever they need to the classpath
        for (BasicInjector i : injectors){
            i.Initialize();
            URL[] e = i.getClassPathItems();
            if (e != null){
                urlclasspath.addAll(Arrays.asList(e));
            }
        }
        loader = new DankClassLoader(urlclasspath.toArray(new URL[]{}), DankClassLoader.class.getClassLoader());
        // give it our injectors
        loader.setInjectors(injectors);
        // load the main class of the game
        // TODO: make this customizable somehow
        //Class<?> blenders = Class.forName("net.minecraft.client.Minecraft", true, loader);
        Class<?> blenders = loader.findClass("net.minecraft.client.Minecraft");
        String[] mcargs = Arrays.copyOfRange(args, total, args.length);
        System.out.println("new args are: " + Arrays.toString(mcargs));
        blenders.getMethod("main", String[].class).invoke(null, (Object) mcargs);
    }
}
