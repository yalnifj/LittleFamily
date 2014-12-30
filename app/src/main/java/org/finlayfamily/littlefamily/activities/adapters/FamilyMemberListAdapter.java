package org.finlayfamily.littlefamily.activities.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.finlayfamily.littlefamily.data.LittlePerson;

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
        TextView display;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = new TextView(context);
            holder = new ViewHolder();
            holder.display = (TextView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LittlePerson person = (LittlePerson) getItem(index);
        if (person!=null) {
            holder.display.setText(person.getName());
        }

        return convertView;
    }
}
