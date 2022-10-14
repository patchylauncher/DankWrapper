package com.eziosoft.DankWrapper.skinpatch;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class SkinPatchFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String procol){
        // we just want to overwrite http
        if (procol.equals("http")){
            return new SkinPatchHandler();
        } else if (procol.equals("file")){
            return new sun.net.www.protocol.file.Handler();
        } else if (procol.equals("jar")){
            return new sun.net.www.protocol.jar.Handler();
        }
        System.out.println("Protocol " + procol + " unsupported by this library!");
        return null;
    }
}
