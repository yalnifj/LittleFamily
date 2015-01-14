package org.finlayfamily.littlefamily.activities.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.MatchPerson;
import org.finlayfamily.littlefamily.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class MatchGameListAdapter extends BaseAdapter {
    private List<MatchPerson> people;
    private Context context;

    public MatchGameListAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setFamily(List<MatchPerson> people) {
        this.people = people;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (people!=null)
            return people.size();
        return 0;
    }

    @Override
    public Object getItem(int index) {
        if (people!=null && people.size()>index) {
            return people.get(index);
        }
        return null;
    }

    @Override
    public long getItemId(int index) {
        if (people!=null && people.size()>index) {
            return people.get(index).getPerson().getId();
        }
        return 0;
    }

    static class ViewHolder {
        ImageView framedPortrait;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = new ImageView(context);
            holder = new ViewHolder();
            holder.framedPortrait = (ImageView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int items = getCount();
        int width = (int) ((parent.getWidth()/1.5)-parent.getWidth()*.05);
        int rowcount = items/2;
        if (rowcount * width < parent.getHeight()) {
            rowcount = items / 3;
            width = (int) ((parent.getWidth() / 2)-parent.getWidth()*.05);
        }

        int height = width;
        MatchPerson person = (MatchPerson) getItem(index);
        if (person!=null) {
            if (person.isFlipped()) {
                Bitmap bm = null;
                if (person.getPerson().getPhotoPath() != null) {
                    bm = ImageHelper.loadBitmapFromFile(person.getPerson().getPhotoPath(), ImageHelper.getOrientation(person.getPerson().getPhotoPath()), width, height);
                } else {
                    bm = ImageHelper.loadBitmapFromResource(context, person.getPerson().getDefaultPhotoResource(), 0, width, height);
                }

                Resources r = context.getResources();
                Bitmap frame = BitmapFactory.decodeResource(r, R.drawable.frame1);
                Bitmap overlayed = ImageHelper.overlay(bm, frame);

                holder.framedPortrait.setImageBitmap(overlayed);
            } else {
                Resources r = context.getResources();
                Drawable d = r.getDrawable(R.drawable.frame1);
                holder.framedPortrait.setImageDrawable(d);
            }
        }

        return convertView;
    }
}
