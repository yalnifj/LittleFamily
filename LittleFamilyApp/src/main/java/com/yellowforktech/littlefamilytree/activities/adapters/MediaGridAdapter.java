package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.yellowforktech.littlefamilytree.data.Media;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class MediaGridAdapter extends BaseAdapter {
    private List<Media> mediaList;
    private Context context;

    public MediaGridAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setMediaList(List<Media> media) {
        this.mediaList = media;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mediaList!=null)
            return mediaList.size();
        return 0;
    }

    @Override
    public Object getItem(int index) {
        if (mediaList!=null && mediaList.size()>index) {
            return mediaList.get(index);
        }
        return null;
    }

    @Override
    public long getItemId(int index) {
        if (mediaList!=null && mediaList.size()>index) {
            return mediaList.get(index).getId();
        }
        return 0;
    }

    static class ViewHolder {
        ImageView image;
        Bitmap bitmap;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = new ImageView(context);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GridView gridView = (GridView) parent;
        int width = (int) ((parent.getWidth() / gridView.getNumColumns())-parent.getWidth()*.05);
        int height = width;
        Media media = (Media) getItem(index);
        if (media!=null) {
            if (holder.bitmap==null) {
                if (media.getLocalPath() != null) {
                    Bitmap bm = ImageHelper.loadBitmapFromFile(media.getLocalPath(), ImageHelper.getOrientation(media.getLocalPath()), width, height, false);
                    holder.bitmap = bm;
                }
            }
            holder.image.setImageBitmap(holder.bitmap);
        }

        return convertView;
    }
}
