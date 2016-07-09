package com.example.kbaldor.cryptotest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.spongycastle.util.Arrays;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private static String DEBUG = "CryptoTestMain";

    KeyPair myKeyPair;

    SecretKey myAESKey;
//    byte[] ivBytes = new byte[128];
//    IvParameterSpec ivSpec;
//    Key myAESEncriptionKey;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(),1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make a new key

        try {
            SecureRandom random = new SecureRandom();
//            random.nextBytes(ivBytes);
//            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            KeyGenerator aesGenerator = KeyGenerator.getInstance("AES","SC");
            aesGenerator.init(256,random);
            myAESKey = aesGenerator.generateKey();
            //myAESEncriptionKey = new SecretKeySpec(myAESKey.getEncoded(), "AES");

            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA","SC");
            generator.initialize(spec,random);

            myKeyPair = generator.generateKeyPair();

            Log.d(DEBUG,"GOT KEY!");

            StringWriter writer = new StringWriter();
            PemWriter pemWriter = new PemWriter(writer);
            pemWriter.writeObject(new PemObject("PUBLIC KEY",myAESKey.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
            ((TextView)findViewById(R.id.public_key_field)).setText(writer.toString());
            ((TextView)findViewById(R.id.public_key_field)).setText(Base64.encodeToString(myAESKey.getEncoded(),Base64.DEFAULT));


//            writer = new StringWriter();
//            pemWriter = new PemWriter(writer);
//            pemWriter.writeObject(new PemObject("PRIVATE KEY",myKeyPair.getPrivate().getEncoded()));
//            pemWriter.flush();
//            pemWriter.close();
//            ((TextView)findViewById(R.id.private_key_field)).setText(writer.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String init = "";
        for(int i=0;i<85;i++){
            init += "a";
        }
        Log.d(DEBUG,init);
        encryptToBase64(init);
        ((EditText)findViewById(R.id.edit_text)).setText(init);
    }

    private String encryptToBase64(String clearText){
        try {
            Log.d(DEBUG,"clear text is of length "+clearText.getBytes().length);
//            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding","SC");
//            rsaCipher.init(Cipher.ENCRYPT_MODE, myKeyPair.getPublic());
//            byte[] bytes = rsaCipher.doFinal(clearText.getBytes());

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding","SC");
            aesCipher.init(Cipher.ENCRYPT_MODE,myAESKey);
            byte[] bytes = aesCipher.doFinal(clearText.getBytes());
            Log.d(DEBUG,"cipher bytes is of length "+bytes.length);
            Log.d(DEBUG,"");

            byte[] concatBytes = new byte[bytes.length+16];
            System.arraycopy(aesCipher.getIV(),0,concatBytes,0,16);
            System.arraycopy(bytes,0,concatBytes,16,bytes.length);

            return Base64.encodeToString(concatBytes,Base64.DEFAULT);
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
        return "";
    }

    private String decryptFromBase64(String cipherText){
        try {
            byte[] bytes = Base64.decode(cipherText,Base64.DEFAULT);
            byte[] iv = Arrays.copyOfRange(bytes, 0, 16);
            byte[] decode = Arrays.copyOfRange(bytes, 16, bytes.length);
//            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding","SC");
//            rsaCipher.init(Cipher.DECRYPT_MODE, myKeyPair.getPrivate());

            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding","SC");
            aesCipher.init(Cipher.DECRYPT_MODE,myAESKey,new IvParameterSpec(iv));

//            bytes = rsaCipher.doFinal(bytes);
            bytes = aesCipher.doFinal(decode);

            return new String(bytes,"UTF-8");
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            Log.d(DEBUG,"Ouch",e);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void doEncrypt(View view){
        Log.d(DEBUG,"doEncrypt");
        String content = ((EditText)findViewById(R.id.edit_text)).getText().toString();
        String cipher = encryptToBase64(content);
        ((EditText)findViewById(R.id.edit_text)).setText(cipher);
    }

    public void doDecrypt(View view){
        Log.d(DEBUG,"doDecrypt");
        String content = ((EditText)findViewById(R.id.edit_text)).getText().toString();
        String clear = decryptFromBase64(content);
        ((EditText)findViewById(R.id.edit_text)).setText(clear);
    }
}
