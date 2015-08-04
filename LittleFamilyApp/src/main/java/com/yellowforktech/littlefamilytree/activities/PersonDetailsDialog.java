package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import android.graphics.Bitmap;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import java.text.SimpleDateFormat;

public class PersonDetailsDialog extends DialogFragment {
    private LittlePerson person;
	private SimpleDateFormat df = new SimpleDateFormat("YYYY");

    public PersonDetailsDialog(LittlePerson person) {
        this.person = person;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_person_details, container, false);
		ImageView portrait = (ImageView) view.findViewById(R.id.portraitImage);
		if (person.getPhotoPath() != null) {
			Bitmap bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), portrait.getWidth(), portrait.getHeight(), false);
			portrait.setImageBitmap(bm);
		} else {
			Bitmap bm = ImageHelper.loadBitmapFromResource(this.getActivity(), person.getDefaultPhotoResource(), 0, portrait.getWidth(), portrait.getHeight());
			portrait.setImageBitmap(bm);
		}
		
		TextView nameView = (TextView) view.findViewById(R.id.personName);
		nameView.setText(person.getName());
		
		TextView remoteIdView = (TextView) view.findViewById(R.id.remoteId);
		remoteIdView.setText(person.getFamilySearchId());
		
		TextView genderView = (TextView) view.findViewById(R.id.txtGender);
		genderView.setText(person.getGender().toString());
		
		CheckBox activeBox = (CheckBox) view.findViewById(R.id.chkActive);
		activeBox.setChecked(person.isActive());
		
		TextView birthDateView = (TextView) view.findViewById(R.id.txtBirthDate);
		birthDateView.setText(df.format(person.getBirthDate()));
		
		TextView birthPlaceView = (TextView) view.findViewById(R.id.txtBirthPlace);
		birthPlaceView.setText(person.getBirthPlace());
		
		return view;
    }

}
