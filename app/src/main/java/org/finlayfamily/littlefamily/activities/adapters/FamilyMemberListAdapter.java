package org.finlayfamily.littlefamily.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class FamilyMemberListAdapter extends BaseAdapter {
    private List<LittlePerson> family;
    private Context context;

    public FamilyMemberListAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setFamily(List<LittlePerson> family) {
        this.family = family;
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
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.person_portait_name, null);
            holder = new ViewHolder();
            holder.portrait = (ImageView) convertView.findViewById(R.id.person_portrait);
            holder.name = (TextView) convertView.findViewById(R.id.person_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int width = (int) ((parent.getWidth() / 2)-parent.getWidth()*.05);
        int height = width;
        LittlePerson person = (LittlePerson) getItem(index);
        if (person!=null) {
            holder.name.setText(person.getName());
            if (person.getPhotoPath()!=null) {
                Bitmap bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, height);
                holder.portrait.setImageBitmap(bm);
            } else {
                Bitmap bm = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, width, height);
                holder.portrait.setImageBitmap(bm);
            }
        }

        return convertView;
    }
}
