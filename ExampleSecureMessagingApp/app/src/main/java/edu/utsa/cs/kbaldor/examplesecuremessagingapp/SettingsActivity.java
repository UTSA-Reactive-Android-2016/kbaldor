package edu.utsa.cs.kbaldor.examplesecuremessagingapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.utsa.cs.kbaldor.examplesecuremessagingapp.util.Engine;

public class SettingsActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    static final String LOG = "Settings";

    EditText myServerSpec;
    EditText myUsername;

    String user_image_filename;

    Engine myEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user_image_filename =  getResources().getString(R.string.user_image_filename);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(myToolbar);
        myEngine = Engine.getInstance(getApplicationContext());

        File imFile = myEngine.getImageFileObject(user_image_filename);
        if(imFile.exists()){
            ImageView im = (ImageView)findViewById(R.id.user_image);
            im.setImageURI(Uri.fromFile(myEngine.getImageFileObject(user_image_filename)));
        }

        myServerSpec = (EditText)findViewById(R.id.server_spec);
        myServerSpec.setText(myEngine.getServerURL());
        myServerSpec.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                myEngine.parseServerString(myServerSpec.getText().toString());
                myServerSpec.setText(myEngine.getServerURL());
            }
        });
        myUsername = (EditText)findViewById(R.id.username);
        myUsername.setText(myEngine.getUsername());
        myUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                myEngine.setUsername(myUsername.getText().toString());
                myServerSpec.setText(myEngine.getServerURL());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.camera:
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if(permissionCheck == -1){
                    giveBunny();
                }else{
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void giveBunny(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        AlertDialog.Builder builder1 = builder.setView(inflater.inflate(R.layout.no_camera_popup, null)).setPositiveButton(R.string.bunny_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                InputStream is;
                byte[] buffer;
                try {
                    is = getAssets().open("images/bunny_pancake.png");
                    buffer = new byte[is.available()];
                    is.read(buffer);
                    myEngine.writeUserImage(user_image_filename,buffer);
                    ImageView im = (ImageView)findViewById(R.id.user_image);
                    im.setImageURI(Uri.fromFile(myEngine.getImageFileObject(user_image_filename)));
                    Log.d(LOG,"SetImage");
                } catch (IOException e) {
                    Log.d(LOG,"Failed to set image",e);
                }
            }
        });
        builder.create().show();
    }


    /*
     * This is where I handle the image from the camera. I resize the image to a central
     * 300x300 square. I attempted to make use of the rotation information in the
     * image, but it always returned zero, so you will have to find the right rotation
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                File outFile = myEngine.getImageFileObject("user_image.png");
                OutputStream out = new FileOutputStream(outFile);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if (width > height){
                    int x = (width-height)/2;
                    bitmap = Bitmap.createBitmap(bitmap, x,0,height, height);
                } else {
                    int y = (height-width)/2;
                    bitmap = Bitmap.createBitmap(bitmap, 0, y,width, width);
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);

                ImageView im = (ImageView) findViewById(R.id.user_image);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                im.setImageURI(null);
                im.setImageURI(Uri.fromFile(myEngine.getImageFileObject(user_image_filename)));


            } catch (Exception e) {
                e.printStackTrace();
                giveBunny();
            }
        }
    }

    public void doRegister(View view){
        myEngine.register();
    }

    public void doLogin(View view){
        myEngine.logIn();
        myEngine.registerContacts();
    }

    public void doLogout(View view){
        myEngine.logOut();
    }

}
