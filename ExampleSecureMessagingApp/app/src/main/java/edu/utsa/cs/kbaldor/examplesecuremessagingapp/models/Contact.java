package edu.utsa.cs.kbaldor.examplesecuremessagingapp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.security.PublicKey;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Crypto;

/**
 * Created by kbaldor on 8/9/16.
 */
public class Contact {
    public final String name;
    public final String publicKeyString;
    public final String imageString;
    public final PublicKey publicKey;
    public final Bitmap bitmap;
    public boolean logged_in;

    public Contact(String name, String publicKeyString, String imageString){
        this.logged_in=false;
        this.name = name;
        this.publicKeyString = publicKeyString;
        this.imageString = imageString;

        this.publicKey = Crypto.getPublicKeyFromString(publicKeyString);

        byte[] imageBytes = Base64.decode(imageString,Base64.NO_WRAP);
        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}
