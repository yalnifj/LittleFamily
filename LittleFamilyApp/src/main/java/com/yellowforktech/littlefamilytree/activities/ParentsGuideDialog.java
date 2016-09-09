package com.yellowforktech.littlefamilytree.activities;

import android.graphics.Point;
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

    private int[] pages = {
            R.layout.fragment_pg_welcome,
            R.layout.fragment_pg_photos,
            R.layout.fragment_pg_playtogether,
            R.layout.fragment_pg_chooseplayer,
            R.layout.fragment_pg_home,
            R.layout.fragment_pg_stars,
            R.layout.fragment_pg_settings,
            R.layout.fragment_pg_kidheritage
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_parentsguide, container, false);
        mPager = (ViewPager) v.findViewById(R.id.pgPager);
        mPager.setAdapter(new ParentsGuidPagerAdapter(getChildFragmentManager()));
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

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParentsGuideActivity activity = (ParentsGuideActivity)getActivity();
                activity.dismiss(okBtn);
            }
        });

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    prevBtn.setVisibility(View.INVISIBLE);
                } else {
                    prevBtn.setVisibility(View.VISIBLE);
                }
                if (position == pages.length - 1) {
                    nextBtn.setVisibility(View.INVISIBLE);
                    okBtn.setVisibility(View.VISIBLE);
                } else {
                    nextBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int width = Math.min(size.x, size.y);
        int height = (int) (width * 1.3);
        if (height > size.y) height = size.y;
        getDialog().getWindow().setLayout(width, height);
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
