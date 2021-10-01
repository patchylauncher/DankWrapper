package com.eziosoft.DankWrapper;

import javax.imageio.ImageIO;
import java.io.File;

public class Patches {

    public static File workDirPatch(){
        // nuke disk caching
        System.out.println("Turning off imageio disk caching...");
        ImageIO.setUseCache(false);
        // todo: shit
        System.out.println("Setting gamedir to " + Launch.workdir);
        return new File(Launch.workdir);

    }

}
