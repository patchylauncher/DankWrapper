package com.eziosoft.DankWrapper.debug;

public class NotAnErrorException extends Exception{

    public NotAnErrorException(String why){
        super(why);
    }

}
