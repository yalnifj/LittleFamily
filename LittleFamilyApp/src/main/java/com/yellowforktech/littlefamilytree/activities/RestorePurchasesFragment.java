package com.yellowforktech.littlefamilytree.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yellowforktech.littlefamilytree.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class RestorePurchasesFragment extends Fragment {

    public RestorePurchasesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_restore_purchases, container, false);
    }
}
