package com.example.kbaldor.rxcontactstatus.stages;

/**
 * Created by kbaldor on 7/26/16.
 */
public class RegistrationStruct
{
    final public String server;
    final public String username;
    final public String base64Image;
    final public String keyString;

    public RegistrationStruct(String server,
                              String username,
                              String base64Image,
                              String keyString){
        this.server = server;
        this.username = username;
        this.base64Image = base64Image;
        this.keyString = keyString;
    }
}
