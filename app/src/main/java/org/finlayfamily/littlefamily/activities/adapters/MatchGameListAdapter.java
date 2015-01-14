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

        int width = (int) ((parent.getWidth()/2)-parent.getWidth()*.05);
        int height = (int) ((parent.getHeight()/2)-parent.getHeight()*.05);
        int rotate = 0;
        if (height < width) {
            width = height;
            rotate = 90;
        }
        else {
            height = width;
            rotate = 0;
        }
        holder.framedPortrait.setRotation(rotate);
        MatchPerson person = (MatchPerson) getItem(index);
		Resources r = context.getResources();
        if (person!=null) {
            if (person.isFlipped()) {
                Bitmap bm = null;
                if (person.getPerson().getPhotoPath() != null) {
                    bm = ImageHelper.loadBitmapFromFile(person.getPerson().getPhotoPath(), ImageHelper.getOrientation(person.getPerson().getPhotoPath()), width, height);
                } else {
              		bm = BitmapFactory.decodeResource(r, person.getPerson().getDefaultPhotoResource());
				}
				
                Bitmap frame = BitmapFactory.decodeResource(r, person.getFrame());
                Bitmap overlayed = ImageHelper.overlay(bm, frame, width, height);

                holder.framedPortrait.setImageBitmap(overlayed);
            } else {
                Bitmap bm = ImageHelper.loadBitmapFromResource(context, person.getFrame(), 0, width, height);
                holder.framedPortrait.setImageBitmap(bm);
            }
        }

        return convertView;
    }
}
