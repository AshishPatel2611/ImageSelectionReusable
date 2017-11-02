package com.ashish.imageselectionreusable;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by codexalters on 8/9/17.
 */

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    public static boolean requestPermission(Activity activity, int requestCode, String... permissions) {

        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            for (String s : permissions) {
                int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
                boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
                granted &= hasPermission;
                if (!hasPermission) {
                    permissionsNeeded.add(s);
                }
            }


            if (granted) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity,
                        permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                        requestCode);
                return false;

            }
        } else {
            return true;
            // do something for phones running an SDK before M
        }


    }


    public static boolean permissionGranted(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int permissionCode) {
        if (requestCode == permissionCode) {

            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    //denied
                    Log.w("denied", permission);

                    Toast.makeText(activity, "Required permission : " + permission + "", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.w("allowed", permission);

                        return true;
                    } else {
                        //set to never ask again
                        Log.w("set to never ask again", permission);
                        //do something here.
                        Toast.makeText(activity, "Enable permission in Settings :" + permission + "", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            }

        }
        return false;
    }


}
