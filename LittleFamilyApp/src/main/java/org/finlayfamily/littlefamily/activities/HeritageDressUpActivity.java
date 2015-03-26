package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import java.util.*;
import org.finlayfamily.littlefamily.data.*;
import org.finlayfamily.littlefamily.util.*;

public class HeritageDressUpActivity extends ActionBarActivity implements HeritageCalculatorTask.Listener {

    private LittlePerson person;
	private SortedMap<String, Double> cultures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
    }

	@Override
	public void onComplete(ArrayList<HeritagePath> paths)
	{
		ValueComparator vc = new ValueComparator();
		cultures = new TreeMap<>(vc);
		
		Double total = 0.0;
		for(HeritagePath path : paths) {
			String place = path.getPlace();
			total += path.getPercent();
			if (cultures.get(place)==null) {
				cultures.put(place, path.getPercent());
			} else {
				cultures.put(place, cultures.get(place) + path.getPercent());
			}
		}
		if (total < 1.0) {
			if (cultures.get("Unknown")==null) {
				cultures.put("Unknown", 1.0 - total);
			} else {
				cultures.put("Unknown", cultures.get("Unknown") + 1.0 - total);
			}
		}
	}

}
