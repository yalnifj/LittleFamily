package org.finlayfamily.littlefamily.activities;

import android.app.DialogFragment;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class LoadingDialog extends DialogFragment {
    private AnimationDrawable plantAnimation;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_loading, container, false);
        ImageView iv = (ImageView)v.findViewById(R.id.growingPlant);
        plantAnimation = (AnimationDrawable) iv.getDrawable();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        plantAnimation.start();
    }
}
