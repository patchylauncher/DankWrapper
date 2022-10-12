package com.eziosoft.DankWrapper.skinpatch;

import com.eziosoft.DankWrapper.Launch;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class SkinPatcher extends HttpURLConnection {
    protected SkinPatcher(URL u) {
        super(u);
    }

    @Override
    public void disconnect() {
        // NOTHING!
    }

    private InputStream skinstream;

    @Override
    public boolean usingProxy() {
        return false;
    }

    private static String useragent = "Mozilla/5.0 (PatchyLauncherOS) Firefox/69.0";

    @Override
    public void connect() throws IOException {
        try {
            URL skinapiurl = new URL("http://mcmirror.reimu.info/skinapi/api.php?skinid="+Launch.skinid+"&legacy=1");
            URLConnection fuckuseragents = skinapiurl.openConnection();
            fuckuseragents.setRequestProperty("User-Agent", useragent);
            String rawskinurl = IOUtils.toString(fuckuseragents.getInputStream(), "UTF-8");
            System.out.println(rawskinurl);
            // then we have to get an input stream of the returned url
            URL getskin = new URL(rawskinurl);
            fuckuseragents = getskin.openConnection();
            fuckuseragents.setRequestProperty("User-Agent", useragent);
            skinstream = fuckuseragents.getInputStream();
        } catch (Exception e){
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException{
        return skinstream;
    }
}
