package org.finlayfamily.littlefamily.activities.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
        Bitmap front;
        Bitmap back;
        MatchPerson person;
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

        GridView grid = (GridView) parent;
        int width = grid.getColumnWidth();
        int height = width;
        int rows = getCount() / grid.getNumColumns();
        if (rows * (height+12) > grid.getHeight()) {
            height = (grid.getHeight() / rows)-12;
            width = height;
        }

        if (width>0) {
            MatchPerson person = (MatchPerson) getItem(index);
            Resources r = context.getResources();
            if (person != null) {
                if (person!=holder.person) {
                    holder.back = null;
                    holder.front = null;
                    holder.person = person;
                }
                Bitmap toDraw = null;
                if (person.isFlipped()) {
                    if (holder.back!=null) toDraw = holder.back;
                    else {
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#d99f9f"));
                        paint.setStyle(Paint.Style.FILL);
                        Bitmap bm = null;
                        Bitmap frame = BitmapFactory.decodeResource(r, person.getFrame());
                        if (person.getPerson().getPhotoPath() != null) {
                            bm = ImageHelper.loadBitmapFromFile(person.getPerson().getPhotoPath(), ImageHelper.getOrientation(person.getPerson().getPhotoPath()), width, height, false);
                        } else {
                            bm = ImageHelper.loadBitmapFromResource(context, person.getPerson().getDefaultPhotoResource(), 0, width, height);
                        }
                        toDraw = ImageHelper.overlay(bm, frame, width, height, paint);
                        holder.back = toDraw;
                    }
                } else {
                    if (holder.front!=null) toDraw = holder.front;
                    else {
                        Paint paint = new Paint();
                        paint.setColor(Color.parseColor("#d99f9f"));
                        paint.setStyle(Paint.Style.FILL);
                        Bitmap frame = BitmapFactory.decodeResource(r, person.getFrame());
                        Bitmap bm = ImageHelper.fill(frame, paint);
                        toDraw = Bitmap.createBitmap(width, height, frame.getConfig());
                        Canvas canvas = new Canvas(toDraw);
                        Rect rect = new Rect();
                        rect.set(0, 0, width, height);
                        canvas.drawBitmap(bm, null, rect, null);
                        canvas.drawBitmap(frame, null, rect, null);
                        holder.front = toDraw;
                    }
                }

                holder.framedPortrait.setImageBitmap(toDraw);
            }
        }

        return convertView;
    }
}
