package com.example.jose.cameralocation.Util;

import android.Manifest;

/**
 * Created by Jose on 11/25/2016.
 */

public class Constants {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String TAG = "App-Camera";
    public static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final String NAME_IMAGE = "CacaoteraImage";
    public static final String PICTURES_DIRECTORY = "Cacaotera";
}
