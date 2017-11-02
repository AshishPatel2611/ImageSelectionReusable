package com.ashish.imageselectionreusable;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSelectionActivity extends AppCompatActivity {
    private static final String TAG = "ImageSelectionActivity";
    private static final int ACTION_REQUEST_CAMERA = 1;
    private static final int ACTION_REQUEST_GALLERY = 2;
    private static final int ACTION_REQUEST_CROP_IMAGE = 3;
    Bitmap selectedImageBitmap = null;
    File imageFile = null;
    File imageFileCropped = null;
    Boolean isCoverPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* todo add this style and set as this activity theme
        <style name="theme" parent="Theme.AppCompat.Light.Dialog.Alert">
        <item name="android:windowFullscreen">true</item>
        <item name="windowNoTitle">true</item>
        </style>
       */

        if (getIntent() != null)
            isCoverPhoto = getIntent().getBooleanExtra("isCoverPhoto", false);
        selectImageSource();

    }

    private void selectImageSource() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (PermissionUtils.requestPermission(ImageSelectionActivity.this, ACTION_REQUEST_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACTION_REQUEST_GALLERY);
                                }
                                break;

                            case 1:
                                if (PermissionUtils.requestPermission(ImageSelectionActivity.this, ACTION_REQUEST_CAMERA, Manifest.permission.CAMERA,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Intent getCameraImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(getCameraImage, ACTION_REQUEST_CAMERA);
                                }
                                break;

                            default:
                                break;
                        }
                    }
                });

        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                        keyEvent.getAction() == KeyEvent.ACTION_UP && !keyEvent.isCanceled()) {
                    dialogInterface.dismiss();
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish(); // ends current activity
                    return true;
                }
                return false;
            }
        });
        alertDialog.show();
        Window window = alertDialog.getWindow();
        assert window != null;
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL); // to finish activity on touching outside dialog

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode + " " + resultCode + "  " + data);

        Bitmap profileImageBitmap = null;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTION_REQUEST_CAMERA:
                    profileImageBitmap = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    imageFile = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                    FileOutputStream fo;
                    try {
                        imageFile.createNewFile();
                        fo = new FileOutputStream(imageFile);
                        fo.write(bytes.toByteArray());
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // git remote add origin https://github.com/AshishPatel2611/Simple-Image-Selection.gitTODO: put path.xml in xml package in resources and update menifest with file-provider if getting error in this line...

                    ImageCropFunction(FileProvider.getUriForFile(ImageSelectionActivity.this, BuildConfig.APPLICATION_ID, imageFile), imageFile);
                    imageFile = null;

                    break;

                case ACTION_REQUEST_GALLERY:
                    ImageCropFunction(data.getData(), new File((data.getData().getPath())));
                    break;
                case ACTION_REQUEST_CROP_IMAGE:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            if (profileImageBitmap != null) {
                                profileImageBitmap.recycle();
                            }
                            profileImageBitmap = extras.getParcelable("data");
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            profileImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            imageFileCropped = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + "_cropped" + ".jpg");
                            FileOutputStream fileOutputStream;
                            try {
                                imageFileCropped.createNewFile();
                                fileOutputStream = new FileOutputStream(imageFileCropped);
                                fileOutputStream.write(stream.toByteArray());
                                fileOutputStream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "onActivityResult:profileImageBitmap =  " + profileImageBitmap);
                        selectedImageBitmap = profileImageBitmap;
                        Intent intent = new Intent();
                        intent.putExtra("bitmap", selectedImageBitmap);
                        intent.putExtra("image", imageFileCropped);
                        setResult(RESULT_OK, intent);
                        finish(); // ends current activity
                    }

                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish(); // ends current activity
        } else {
            finish();
        }
    }

    public void ImageCropFunction(Uri imageUri, File imageFile) {
        // Image Crop Code

        try {
            Intent CropIntent = new Intent("com.android.camera.action.CROP");
            CropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            CropIntent.setDataAndType(imageUri, "image/*");
            CropIntent.putExtra("crop", "true");

            if (isCoverPhoto) {
                CropIntent.putExtra("aspectX", 2);
            } else {
                CropIntent.putExtra("aspectX", 1);
            }
            CropIntent.putExtra("aspectY", 1);
            CropIntent.putExtra("return-data", true);
            startActivityForResult(CropIntent, ACTION_REQUEST_CROP_IMAGE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                selectedImageBitmap = (MediaStore.Images.Media.getBitmap(ImageSelectionActivity.this.getContentResolver(), imageUri));
                imageFileCropped = imageFile;
                Intent intent = new Intent();
                intent.putExtra("bitmap", selectedImageBitmap);
                intent.putExtra("image", imageFileCropped);
                setResult(RESULT_OK, intent);
                finish();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACTION_REQUEST_GALLERY:
                if (PermissionUtils.permissionGranted(ImageSelectionActivity.this, requestCode, permissions, grantResults, ACTION_REQUEST_GALLERY)) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), ACTION_REQUEST_GALLERY);

                }
                break;

            case ACTION_REQUEST_CAMERA:
                if (PermissionUtils.permissionGranted(ImageSelectionActivity.this, requestCode, permissions, grantResults, ACTION_REQUEST_CAMERA)) {
                    Intent getCameraImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(getCameraImage, ACTION_REQUEST_CAMERA);
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish(); // ends current activity

    }


}
