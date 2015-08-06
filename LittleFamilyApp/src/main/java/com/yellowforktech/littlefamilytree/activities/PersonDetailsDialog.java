package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.ForceSynceTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.text.SimpleDateFormat;

public class PersonDetailsDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, ForceSynceTask.Listener {
    private LittlePerson person;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy");
	private View view;
	private ProgressDialog pd;

	public PersonDetailsDialog() {
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		person = (LittlePerson) args.getSerializable(ChooseFamilyMember.SELECTED_PERSON);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_person_details, container, false);
		updatePerson();

		return view;
    }

	public void updatePerson() {
		ImageView portrait = (ImageView) view.findViewById(R.id.portraitImage);
		int width = portrait.getWidth();
		if (width==0) width = 100;
		int height = portrait.getHeight();
		if (height==0) height = 100;
		if (person.getPhotoPath() != null) {
			Bitmap bm = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width, height, false);
			portrait.setImageBitmap(bm);
		} else {
			Bitmap bm = ImageHelper.loadBitmapFromResource(this.getActivity(), person.getDefaultPhotoResource(), 0, width, height);
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
		activeBox.setOnCheckedChangeListener(this);

		TextView birthDateView = (TextView) view.findViewById(R.id.txtBirthDate);
		if (person.getBirthDate()!=null) birthDateView.setText(df.format(person.getBirthDate()));
		else birthDateView.setText("");

		TextView birthPlaceView = (TextView) view.findViewById(R.id.txtBirthPlace);
		birthPlaceView.setText(person.getBirthPlace());

		TextView livingView = (TextView) view.findViewById(R.id.txtLiving);
		livingView.setText(person.isAlive()?"Yes":"No");

		TextView parentsView = (TextView) view.findViewById(R.id.txtHasParents);
		if (person.isHasParents()==null) parentsView.setText("Not synced");
		else parentsView.setText(person.isHasParents()?"Yes":"No");

		TextView spousesView = (TextView) view.findViewById(R.id.txtHasSpouses);
		if (person.isHasSpouses()==null) spousesView.setText("Not synced");
		else spousesView.setText(person.isHasSpouses()?"Yes":"No");

		TextView childrenView = (TextView) view.findViewById(R.id.txtHasChildren);
		if (person.isHasChildren()==null) childrenView.setText("Not synced");
		else childrenView.setText(person.isHasChildren() ? "Yes" : "No");

		TextView lastSyncView = (TextView) view.findViewById(R.id.txtLastSync);
		lastSyncView.setText(person.getLastSync().toString());

		TableRow nationalityRow = (TableRow) view.findViewById(R.id.nationalityRow);
		if (person.getNationality()!=null) {
			nationalityRow.setVisibility(View.VISIBLE);
			TextView nationalityView = (TextView) view.findViewById(R.id.txtNationality);
			nationalityView.setText(person.getNationality());
		}
		else nationalityRow.setVisibility(View.GONE);

		/*
		MediaGridAdapter adapter = new MediaGridAdapter(getActivity());
		try {
			List<Media> mediaList = DataService.getInstance().getMediaForPerson(person);
			adapter.setMediaList(mediaList);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error getting person media for "+person.getName(), e);
		}

		GridView mediaGrid = (GridView) view.findViewById(R.id.mediaGrid);
		mediaGrid.setAdapter(adapter);
		*/
		ImageButton syncButton = (ImageButton) view.findViewById(R.id.btnSync);
		syncButton.setOnClickListener(this);

		/*
		ImageButton captureButton = (ImageButton) view.findViewById(R.id.btnAttach);
		captureButton.setOnClickListener(this);
		*/

		Button closeButton = (Button) view.findViewById(R.id.btnClose);
		closeButton.setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		person.setActive(isChecked);
		try {
			DataService.getInstance().getDBHelper().persistLittlePerson(person);
		} catch (Exception e) {
			Log.e("PersonDetailsDialog", "onCheckedChanged error persisting person "+person, e);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnClose:
				this.dismissAllowingStateLoss();
				break;
			case R.id.btnSync:
				pd = ProgressDialog.show(getActivity(), "Please wait...", "Synchronizing "+person.getName(), true, false);
				ForceSynceTask task = new ForceSynceTask(this, this.getActivity());
				task.execute(person);
				break;
			/*
			case R.id.btnAttach:
				break;
			*/
		}
	}

	@Override
	public void onComplete(LittlePerson person) {
		this.person = person;
		if (pd!=null) pd.dismiss();
		updatePerson();
		Toast.makeText(this.getActivity(), "Succefully synced person "+person.getName(), Toast.LENGTH_LONG);
	}

	@Override
	public void onStatusUpdate(String message) {
		if (pd!=null) pd.setMessage(message);
	}
}
