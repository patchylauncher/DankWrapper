package com.eziosoft.DankWrapper.skinpatch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class SkinPatchHandler extends URLStreamHandler {
    // old URLs that we need to look for
    private final static String[] old_skins = new String[]{
            "http://www.minecraft.net/skin/",
            "http://s3.amazonaws.com/MinecraftSkins/",
            "http://skins.minecraft.net/MinecraftSkins/"
    };

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        for (String s : old_skins){
            if (url.toString().startsWith(s)){
                System.out.println("Attempt to load old skin detected, hijacking...");
                return new SkinPatcher(url);
            }
        }
        return new sun.net.www.protocol.http.HttpURLConnection(url, null);
    }
}
