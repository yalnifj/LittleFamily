package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.util.List;

/**
 * Created by jfinlay on 10/27/2015.
 */
public class PersonPagerAdapter extends PagerAdapter {

    private List<LittlePerson> people;
    private Context context;

    public PersonPagerAdapter(Context context) {
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
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup parent, int position) {
        LayoutInflater vi = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.person_name_relationship, null);

        LittlePerson person = people.get(position);

        TextView nameView = (TextView) view.findViewById(R.id.personName);
        nameView.setText(person.getName());
        ImageView portrait = (ImageView) view.findViewById(R.id.personPortrait);
        Bitmap bm = null;
        if (person.getPhotoPath() != null) {
            bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), parent.getHeight(), parent.getHeight(), false);
        } else {
            bm = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, parent.getHeight(), parent.getHeight());
        }
        portrait.setImageBitmap(bm);

        parent.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
