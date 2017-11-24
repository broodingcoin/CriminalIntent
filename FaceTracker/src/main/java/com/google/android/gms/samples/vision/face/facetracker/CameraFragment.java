package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


// TODO: create a Fragment class that allows us to start the FaceTrackerActivity
public class CameraFragment extends Fragment{

    private ImageButton mPhotoButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mainView = inflater.inflate(R.layout.fragment_camera, container, false);


        // Load photo button
        mPhotoButton = (ImageButton) mainView.findViewById(R.id.crime_camera);

        // Check if can take photo
        mPhotoButton.setEnabled(true);

        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If device can take a photo, send the intent
                final Intent ftActivity = new Intent(getActivity(), FaceTrackerActivity.class);
                startActivity(ftActivity);
            }
        });

        return mainView;
    }
}
