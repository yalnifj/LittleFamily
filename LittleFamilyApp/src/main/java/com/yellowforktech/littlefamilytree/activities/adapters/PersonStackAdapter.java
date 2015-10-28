package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 10/27/2015.
 */
public class PersonStackAdapter extends BaseAdapter {

    private List<LittlePerson> people;
    private Context context;

    public PersonStackAdapter(Context context) {
        this.context = context;
    }

    public List<LittlePerson> getPeople() {
        return people;
    }

    public void setPeople(List<LittlePerson> people) {
        this.people = people;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (people!=null) return people.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (people!=null && people.size()>position) {
            return people.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        if (people!=null && people.size()>position) {
            return people.get(position).getId();
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view==null) {
            LayoutInflater vi = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.person_name_relationship, null);
        }

        LittlePerson person = people.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.personName);
        nameView.setText(person.getName());
        ImageView portrait = (ImageView) view.findViewById(R.id.personPortrait);
        Bitmap bm = null;
        if (person.getPhotoPath() != null) {
            bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), parent.getWidth(), parent.getWidth(), false);
        } else {
            bm = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, parent.getWidth(), parent.getWidth());
        }
        portrait.setImageBitmap(bm);

        return view;
    }
}
