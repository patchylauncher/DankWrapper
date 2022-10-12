package com.eziosoft.DankWrapper.injectors;

public abstract class BasicInjector {

    public String targetclass;
    public String launchtarget;
    public boolean hasOptions;
    public boolean acceptsParams = false;
    public String shortarg;
    public String desc;
    public boolean required;
    public boolean hasLaunchTarget = false;
    public String[] loadExceptions;

    public abstract byte[] inject(byte[] input) throws Exception;

    public abstract void AcceptArgument(String input);


}
