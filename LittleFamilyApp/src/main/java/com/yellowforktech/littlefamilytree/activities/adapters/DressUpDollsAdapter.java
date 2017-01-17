package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jfinlay on 5/29/2015.
 */
public class DressUpDollsAdapter extends BaseAdapter {

    private DressUpDolls dressUpDolls;
    private List<DollConfig> allDolls;
    private List<String> allPlaces;
    private Context context;

    public DressUpDollsAdapter(Context context, LittlePerson person) {
        this.context = context;
        dressUpDolls = new DressUpDolls();
        Set<String> places = dressUpDolls.getDollPlaces();
        allPlaces = new ArrayList<>(places.size());
        allDolls = new ArrayList<>(places.size());
        for(String place : places) {
            String[] parts = place.split(" ");
            String upPlace = "";
            for(String part : parts) {
                if (part!=null && !part.isEmpty()) {
                    if (!upPlace.isEmpty()) upPlace+=" ";
                    upPlace += part.substring(0, 1).toUpperCase() + part.substring(1);
                }
            }
            allPlaces.add(upPlace);
            allDolls.add(dressUpDolls.getDollConfig(place, person, context));
        }
    }

    @Override
    public int getCount() {
        return allDolls.size();
    }

    @Override
    public Object getItem(int position) {
        DollConfig dc = allDolls.get(position);
        return dc;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView dollImage;
        TextView place;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.VERTICAL);
            holder = new ViewHolder();
            holder.dollImage = new ImageView(context);
            ll.addView(holder.dollImage);
            holder.place = new TextView(context);
            holder.place.setGravity(Gravity.CENTER_HORIZONTAL);
            if (Build.VERSION.SDK_INT > 16) {
                holder.place.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            ll.addView(holder.place);
            convertView = ll;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String place = allPlaces.get(index);
        DollConfig dc = (DollConfig) getItem(index);
        if (dc!=null) {
            holder.place.setText(place);
            String thumbnailFile = dc.getThumbnail();
            try {
                InputStream is = context.getAssets().open(thumbnailFile);
                Bitmap thumbnail = BitmapFactory.decodeStream(is);
                holder.dollImage.setImageBitmap(thumbnail);
                is.close();
            } catch (IOException e) {
                Log.e("DressUpDollsAdapter", "Error opening asset file", e);
            }
        }

        return convertView;
    }
}
