package com.yellowforktech.littlefamilytree.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jfinlay on 2/29/2016.
 */
public class ParentsGuideFragment extends Fragment {
    public static final String ARG_PAGE = "page";

    private int layoutId;

    public static ParentsGuideFragment create(int pageNumber) {
        ParentsGuideFragment fragment = new ParentsGuideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutId = getArguments().getInt(ARG_PAGE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(layoutId, container, false);

        return rootView;
    }
}
