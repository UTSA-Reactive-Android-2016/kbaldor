package com.example.kbaldor.rxcontactstatus;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import org.spongycastle.util.Arrays;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kbaldor on 7/7/16.
 */
public class Crypto {
    private static String LOG = "Crypto";

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(),1);
    }

    KeyPair myRSAKeyPair;

    public Crypto(SharedPreferences preferences){
        String RSAPrivateKey = preferences.getString("RSAPrivateKey","");
        String RSAPublicKey  = preferences.getString("RSAPublicKey","");

        if(RSAPrivateKey.isEmpty() || !readKeyPair(RSAPrivateKey,RSAPublicKey)) {
            Log.d("LOG","Creating new KeyPair");
            myRSAKeyPair = geneateNewRSAKeyPair();
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

    public static PublicKey getPublicKeyFromString(String keyString){
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

    public static PrivateKey getPrivateKeyFromString(String keyString){
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

    static public byte[] decryptRSA(byte[] cipherText, PrivateKey privateKey){
        try {
            Cipher rsaCipher = Cipher.getInstance("RSA","SC");
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
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

    public byte[] decryptRSA(byte[] cipherText){
        return decryptRSA(cipherText,myRSAKeyPair.getPrivate());
    }

    static public byte[] encryptRSA(byte[] clearText, PublicKey key){
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

    public static SecretKey getAESSecretKeyFromBytes(byte[] keyBytes){
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static SecretKey createAESKey(){
        SecureRandom random = new SecureRandom();

        try {
            KeyGenerator aesGenerator = KeyGenerator.getInstance("AES","SC");
            aesGenerator.init(128,random);
            return aesGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encryptAES(byte[] clearText, SecretKey aesKey){
        try {
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SC");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] bytes = aesCipher.doFinal(clearText);

            byte[] concatBytes = new byte[bytes.length + 16];
            System.arraycopy(aesCipher.getIV(), 0, concatBytes, 0, 16);
            System.arraycopy(bytes, 0, concatBytes, 16, bytes.length);

            return concatBytes;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decryptAES(byte[] cipherText, SecretKey aesKey){
        try {
            byte[] iv = Arrays.copyOfRange(cipherText, 0, 16);
            byte[] decode = Arrays.copyOfRange(cipherText, 16, cipherText.length);

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding","SC");
            aesCipher.init(Cipher.DECRYPT_MODE,aesKey,new IvParameterSpec(iv));

            return aesCipher.doFinal(decode);

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
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
