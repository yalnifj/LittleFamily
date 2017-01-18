package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yellowforktech.littlefamilytree.R;

/**
 * Created by john on 1/17/2017.
 */
public class SkinListAdapter extends BaseAdapter {

    private Context context;

    public SkinListAdapter(Context context) {
        this.context = context;
    }

    int[] items = {R.drawable.boy, R.drawable.boy_mid, R.drawable.boy_dark};
    String[] prefs = {"light", "mid", "dark"};

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return prefs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = null;
        if (convertView != null) {
            view = (ImageView) convertView;
        }

        if (view == null) {
            view = new ImageView(context);
        }

        int resourceId = items[position];
        view.setImageResource(resourceId);

        return view;
    }
}
