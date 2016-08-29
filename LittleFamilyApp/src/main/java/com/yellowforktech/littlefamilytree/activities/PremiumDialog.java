package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;

import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class PremiumDialog extends DialogFragment {

    private int tries;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        View v = inflater.inflate(R.layout.fragment_lock_dialog, container, false);

        TextView triesText = (TextView) v.findViewById(R.id.tries_left_text);
        if (tries > 0) {
            String tryStr = "tries";
            if (tries==1) {
                tryStr = "try";
            }
            String triesStr = getResources().getString(R.string.tries_left, tries, tryStr);
            triesText.setText(triesStr);
        } else {
            triesText.setVisibility(View.GONE);
        }

        Button closeBtn = (Button) v.findViewById(R.id.close_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LittleFamilyActivity activity = (LittleFamilyActivity) getActivity();
                activity.finish();
            }
        });



        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            Log.e(getClass().getName(), "error closing loadingdialog", e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "error closing loadingdialog", e);
        }
    }

}
