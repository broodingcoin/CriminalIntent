package com.google.android.gms.samples.vision.face.facetracker;

import android.support.v4.app.Fragment;


//TODO: Implement an Activity to host our CameraFragment


public class CameraActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CameraFragment();
    }
}
