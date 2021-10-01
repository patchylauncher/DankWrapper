package com.eziosoft.DankWrapper.lib;

import com.eziosoft.DankWrapper.Launch;
import org.objectweb.asm.ClassWriter;

import java.net.URL;
import java.net.URLClassLoader;

public class DankClassWriter extends ClassWriter {

    public DankClassWriter(int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2){
        URLClassLoader fuck = new URLClassLoader(Launch.urlclasspath.toArray(new URL[]{}), Thread.currentThread().getContextClassLoader());
        // we need to use my own class loader instead of the system class loader
        Class c;
        Class d;
        try {
            c = Class.forName(type1.replace('/', '.'), false, fuck);
            d = Class.forName(type2.replace('/', '.'), false, fuck);
        } catch (Exception var7) {
            throw new RuntimeException(var7.toString());
        }

        if (c.isAssignableFrom(d)) {
            return type1;
        } else if (d.isAssignableFrom(c)) {
            return type2;
        } else if (!c.isInterface() && !d.isInterface()) {
            do {
                c = c.getSuperclass();
            } while(!c.isAssignableFrom(d));

            return c.getName().replace('.', '/');
        } else {
            return "java/lang/Object";
        }
    }
}
