package com.example.kbaldor.cryptotest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    private static String DEBUG = "CryptoTestMain";

    KeyPair myKeyPair;

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
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA","SC");
            generator.initialize(spec,random);

            myKeyPair = generator.generateKeyPair();

            Log.d(DEBUG,"GOT KEY!");

            StringWriter writer = new StringWriter();
            PemWriter pemWriter = new PemWriter(writer);
            pemWriter.writeObject(new PemObject("PUBLIC KEY",myKeyPair.getPublic().getEncoded()));
            pemWriter.flush();
            pemWriter.close();
            ((TextView)findViewById(R.id.public_key_field)).setText(writer.toString());


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
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding","SC");
            rsaCipher.init(Cipher.ENCRYPT_MODE, myKeyPair.getPrivate());
            byte[] bytes = rsaCipher.doFinal(clearText.getBytes());
            Log.d(DEBUG,"cipher bytes is of length "+bytes.length);
            Log.d(DEBUG,"");

            return Base64.encodeToString(bytes,Base64.DEFAULT);
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
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding","SC");
            rsaCipher.init(Cipher.DECRYPT_MODE, myKeyPair.getPublic());
            byte[] bytes = Base64.decode(cipherText,Base64.DEFAULT);
            bytes = rsaCipher.doFinal(bytes);
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
