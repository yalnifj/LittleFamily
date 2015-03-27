package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ValueComparator;
import org.finlayfamily.littlefamily.views.PersonHeritageChartView;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ChooseCultureActivity extends Activity implements HeritageCalculatorTask.Listener {

    private LittlePerson person;
    private SortedMap<String, Double> cultures;
    private PersonHeritageChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_culture);

        chartView = (PersonHeritageChartView) findViewById(R.id.personChart);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        chartView.setPerson(person);
    }

    @Override
    public void onComplete(ArrayList<HeritagePath> paths)
    {
        ValueComparator vc = new ValueComparator();
        cultures = new TreeMap<String, Double>(vc);

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

        chartView.setHeritageMap(cultures);
    }
}
