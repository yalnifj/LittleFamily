package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class FamilyMemberListAdapter extends BaseAdapter {
    private List<LittlePerson> family;
    private Map<Integer, Bitmap> bitmaps;
    private Context context;

    public FamilyMemberListAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setFamily(List<LittlePerson> family) {
        this.family = family;
        bitmaps = new HashMap<>(family.size());
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (family!=null)
            return family.size();
        return 0;
    }

    @Override
    public Object getItem(int index) {
        if (family!=null && family.size()>index) {
            return family.get(index);
        }
        return null;
    }

    @Override
    public long getItemId(int index) {
        if (family!=null && family.size()>index) {
            return family.get(index).getId();
        }
        return 0;
    }

    static class ViewHolder {
        ImageView portrait;
        TextView name;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView!=null) {
            holder = (ViewHolder) convertView.getTag();
        }

        if (convertView == null || holder==null) {
            convertView = inflater.inflate(R.layout.person_portait_name, null);
            holder = new ViewHolder();
            holder.portrait = (ImageView) convertView.findViewById(R.id.person_portrait);
            holder.name = (TextView) convertView.findViewById(R.id.person_name);
            convertView.setTag(holder);
        }

        LittlePerson person = (LittlePerson) getItem(index);
        if (person != null) {
            if (person.getGivenName() != null) {
                holder.name.setText(person.getGivenName());
            } else {
                holder.name.setText(person.getName());
            }

            if (bitmaps.get(person.getId())==null) {
                GridView gridView = (GridView) parent;
                int parentWidth = parent.getWidth();
                if (parentWidth == 0) {
                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                    parentWidth = (int) (metrics.widthPixels - 35 * metrics.density);
                }
                int width = (int) ((parentWidth / gridView.getNumColumns()) - parent.getWidth() * .05);
                if (width == 0) width = 200;
                int height = width;

                Bitmap bm = null;
                if (person.getPhotoPath() != null) {
                    bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, height, false);
                    bitmaps.put(person.getId(), bm);
                }
                if (bm==null) {
                    bm = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, width, height);
                    bitmaps.put(person.getId(), bm);
                }
            }
            holder.portrait.setImageBitmap(bitmaps.get(person.getId()));
        }


        return convertView;
    }
}
