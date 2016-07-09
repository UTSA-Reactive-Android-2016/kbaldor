package com.example.kbaldor.apidemo;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by kbaldor on 7/7/16.
 */
public class Crypto {
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(),1);
    }

    KeyPair myRSAKeyPair;

    public Crypto(SharedPreferences preferences){
        String RSAPrivateKey = preferences.getString("RSAPrivateKey","");
        String RSAPublicKey  = preferences.getString("RSAPublicKey","");

        Log.d("Crypto","Public key string: "+RSAPublicKey);
        Log.d("Crypto","Decoded: "+getPublicKeyFromString(RSAPublicKey));


        if(RSAPrivateKey.isEmpty() || !readKeyPair(RSAPrivateKey,RSAPublicKey)) {
            myRSAKeyPair =geneateNewRSAKeyPair();
        }
    }

    public void saveKeys(SharedPreferences preferences){
        String rsaPublicString = Base64.encodeToString(myRSAKeyPair.getPublic().getEncoded(),Base64.DEFAULT);
        String rsaPrivateString = Base64.encodeToString(myRSAKeyPair.getPrivate().getEncoded(),Base64.DEFAULT);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("RSAPublicKey",rsaPublicString);
        editor.putString("RSAPrivateKey",rsaPrivateString);
        Log.d("LOG","Public: "+rsaPublicString);
        editor.commit();
    }

    public String getPublicKeyString(){
        return Base64.encodeToString(myRSAKeyPair.getPublic().getEncoded(),Base64.DEFAULT);
    }

    public PublicKey getPublicKeyFromString(String keyString){
        try {
            KeyFactory constructor_claves = KeyFactory.getInstance("RSA");
            KeySpec clave_raw = new X509EncodedKeySpec(Base64.decode(keyString, Base64.NO_WRAP));
            return constructor_claves.generatePublic(clave_raw);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PrivateKey getPrivateKeyFromString(String keyString){
        try {
            KeyFactory constructor_claves = KeyFactory.getInstance("RSA");
            KeySpec clave_raw = new PKCS8EncodedKeySpec(Base64.decode(keyString, Base64.NO_WRAP));
            return constructor_claves.generatePrivate(clave_raw);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean readKeyPair(String rsaPrivateKey, String rsaPublicKey) {
        PrivateKey privateKey = null;
        PublicKey publicKey = null;

        publicKey = getPublicKeyFromString(rsaPublicKey);
        privateKey = getPrivateKeyFromString(rsaPrivateKey);

//        try {
//            KeyFactory  constructor_claves = KeyFactory.getInstance("RSA");
//            KeySpec clave_raw = new X509EncodedKeySpec(Base64.decode(rsaPrivateKey,Base64.DEFAULT));
//            privateKey = constructor_claves.generatePrivate(clave_raw);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        }


        if((privateKey != null) && (publicKey != null)){
            myRSAKeyPair = new KeyPair(publicKey,privateKey);
            return true;
        }
        return false;
    }

    private KeyPair geneateNewRSAKeyPair(){
        SecureRandom random = new SecureRandom();
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA","SC");
            generator.initialize(spec,random);

            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] cipherText){
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA","SC");
            rsaCipher.init(Cipher.DECRYPT_MODE, myRSAKeyPair.getPrivate());
            return rsaCipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] encrypt(byte[] clearText, PublicKey key){
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA","SC");
            rsaCipher.init(Cipher.ENCRYPT_MODE, key);
            return rsaCipher.doFinal(clearText);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
