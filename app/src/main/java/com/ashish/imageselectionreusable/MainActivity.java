package com.ashish.imageselectionreusable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int PROFILE_PHOTO = 2;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent profile = new Intent(this, ImageSelectionActivity.class);
        profile.putExtra("isCoverPhoto", false);
        startActivityForResult(profile, PROFILE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PROFILE_PHOTO) {

                Bitmap b = data.getParcelableExtra("bitmap");
                Log.w(TAG, "bitmap: " + b);
                File f = (File) data.getExtras().get("image");
                Log.w(TAG, "file: " + f);


            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "No Image selected", Toast.LENGTH_SHORT).show();
        }

    }

}
