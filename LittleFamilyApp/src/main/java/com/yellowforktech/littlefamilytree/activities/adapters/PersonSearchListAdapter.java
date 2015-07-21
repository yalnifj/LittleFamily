package com.yellowforktech.littlefamilytree.activities.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import org.gedcomx.types.GenderType;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class PersonSearchListAdapter extends BaseAdapter {
    private List<LittlePerson> people;
    private Context context;
    private DateFormat df = DateFormat.getDateInstance();

    public PersonSearchListAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setPeople(List<LittlePerson> people) {
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
            return people.get(index).getId();
        }
        return 0;
    }

    static class ViewHolder {
        TextView name;
        TextView birth;
        TextView remoteId;
        TextView gender;
        TextView psc;
        CheckBox active;
    }

    @Override
    public View getView(final int index, View convertView, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.person_search_details, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.lblName);
            holder.birth = (TextView) convertView.findViewById(R.id.lblBirth);
            holder.remoteId = (TextView) convertView.findViewById(R.id.lblRemoteId);
            holder.gender = (TextView) convertView.findViewById(R.id.lblGender);
            holder.psc = (TextView) convertView.findViewById(R.id.lblPSC);
            holder.active = (CheckBox) convertView.findViewById(R.id.chkActive);
            holder.active.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LittlePerson person = (LittlePerson) getItem(index);
                    CheckBox cb = (CheckBox) v;
                    person.setActive(cb.isChecked());
                    try {
                        DataService.getInstance().getDBHelper().persistLittlePerson(person);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            final ListView parentList = (ListView) parent;
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentList.showContextMenuForChild(v);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LittlePerson person = (LittlePerson) getItem(index);
        if (person!=null) {
            holder.name.setText(person.getName());
            if (person.getGender()== GenderType.Female) {
                holder.gender.setText("F");
            } else if (person.getGender()== GenderType.Male) {
                holder.gender.setText("M");
            }
            else {
                holder.gender.setText("U");
            }
            holder.remoteId.setText(person.getFamilySearchId());
            holder.active.setChecked(person.isActive());
            String birthText = "";
            if (person.getBirthDate()!=null) birthText += df.format(person.getBirthDate())+" ";
            if (person.getBirthPlace()!=null) birthText += person.getBirthPlace();
            holder.birth.setText( birthText );
            String psc = "";
            if (person.isHasParents()==null) psc += "-";
            else if (person.isHasParents()) psc += "P";
            else psc += "X";

            if (person.isHasSpouses()==null) psc += "-";
            else if (person.isHasSpouses()) psc += "S";
            else psc += "X";

            if (person.isHasChildren()==null) psc += "-";
            else if (person.isHasChildren()) psc += "C";
            else psc += "X";
            holder.psc.setText(psc);
        }

        return convertView;
    }
}
