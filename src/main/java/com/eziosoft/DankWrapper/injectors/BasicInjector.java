package com.eziosoft.DankWrapper.injectors;

import java.net.URL;

public abstract class BasicInjector {

    public String targetclass;
    public String launchtarget;
    public boolean hasOptions = false;
    public boolean acceptsParams = false;
    public String shortarg;
    public String desc;
    public boolean required = false;
    public boolean hasLaunchTarget = false;
    public String[] loadExceptions;

    public abstract byte[] inject(byte[] input) throws Exception;
    public abstract byte[] inject(byte[] input, String name) throws Exception;

    public abstract void AcceptArgument(String input);

    public abstract void Initialize();

    public abstract URL[] getClassPathItems();


}
