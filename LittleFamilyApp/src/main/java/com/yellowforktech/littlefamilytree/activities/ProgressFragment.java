package com.yellowforktech.littlefamilytree.activities;

import android.app.Fragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ProgressFragment extends Fragment {

    private AnimationDrawable plantAnimation;

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(com.yellowforktech.littlefamilytree.R.layout.fragment_progress, container, false);
        ImageView iv = (ImageView) v.findViewById(com.yellowforktech.littlefamilytree.R.id.growingPlant);
        plantAnimation = (AnimationDrawable) iv.getDrawable();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        plantAnimation.start();
    }
}
