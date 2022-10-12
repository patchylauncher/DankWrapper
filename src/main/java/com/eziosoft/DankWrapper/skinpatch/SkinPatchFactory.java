package com.eziosoft.DankWrapper.skinpatch;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class SkinPatchFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String procol){
        // we just want to overwrite http
        if (procol.equals("http")){
            return new SkinPatchHandler();
        }
        System.out.println("Protocol " + procol + " unsupported by this library!");
        return null;
    }
}
