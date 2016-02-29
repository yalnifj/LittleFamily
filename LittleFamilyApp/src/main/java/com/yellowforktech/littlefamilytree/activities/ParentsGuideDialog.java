package com.yellowforktech.littlefamilytree.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.yellowforktech.littlefamilytree.R;

import java.lang.reflect.Field;

/**
 * Created by jfinlay on 5/27/2015.
 */
public class ParentsGuideDialog extends DialogFragment {

    private ViewPager mPager;
    private ImageButton nextBtn;
    private ImageButton prevBtn;
    private Button okBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_parentsguide, container, false);
        mPager = (ViewPager) v.findViewById(R.id.pgPager);
        mPager.setAdapter(new ParentsGuidPagerAdapter(getFragmentManager()));
        nextBtn = (ImageButton) v.findViewById(R.id.nextBtn);
        prevBtn = (ImageButton) v.findViewById(R.id.prevBtn);
        okBtn = (Button) v.findViewById(R.id.okBtn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem()+1, true);
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
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
            //throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            Log.e(getClass().getName(), "error closing loadingdialog", e);
            //throw new RuntimeException(e);
        }
    }

    /**
     * A simple pager adapter that represents 5 {@link ParentsGuideFragment} objects, in
     * sequence.
     */
    private class ParentsGuidPagerAdapter extends FragmentStatePagerAdapter {
        private int[] pages = {
                R.layout.fragment_pg_welcome,
                R.layout.fragment_pg_photos,
                R.layout.fragment_pg_playtogether,
                R.layout.fragment_pg_chooseplayer,
                R.layout.fragment_pg_home,
                R.layout.fragment_pg_settings
        };

        public ParentsGuidPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ParentsGuideFragment.create(pages[position]);
        }

        @Override
        public int getCount() {
            return pages.length;
        }
    }
}
