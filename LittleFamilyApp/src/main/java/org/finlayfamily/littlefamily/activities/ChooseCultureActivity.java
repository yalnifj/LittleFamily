package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.views.PersonHeritageChartView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseCultureActivity extends Activity implements HeritageCalculatorTask.Listener {

    private LittlePerson person;
    private Map<String, HeritagePath> cultures;
    private PersonHeritageChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_culture);

        chartView = (PersonHeritageChartView) findViewById(R.id.personChart);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        chartView.setPerson(person);

        HeritageCalculatorTask task = new HeritageCalculatorTask(this, this);
        task.execute(person);
    }

    @Override
    public void onComplete(ArrayList<HeritagePath> paths)
    {
        cultures = new HashMap<String, HeritagePath>();

        for(HeritagePath path : paths) {
            String place = path.getPlace();
            if (cultures.get(place)==null) {
                cultures.put(place, path);
            } else {
                Double percent = cultures.get(place).getPercent() + path.getPercent();
                if (cultures.get(place).getTreePath().size() <= path.getTreePath().size()) {
                    cultures.get(place).setPercent(percent);
                } else {
                    path.setPercent(percent);
                    cultures.put(place, path);
                }
            }
        }
        chartView.setHeritageMap(cultures);
    }
}
