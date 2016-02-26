package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yellowforktech.littlefamilytree.R;

import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class ParentsGuideDialog extends DialogFragment {
    private static final int NUM_PAGES = 7;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_parentsguide, container, false);
        mPager = (ViewPager) v.findViewById(R.id.pgPager);

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
            //throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "error closing loadingdialog", e);
            //throw new RuntimeException(e);
        }
    }
}
