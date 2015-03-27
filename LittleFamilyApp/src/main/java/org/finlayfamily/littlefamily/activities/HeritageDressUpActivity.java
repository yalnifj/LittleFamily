package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ValueComparator;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class HeritageDressUpActivity extends Activity {

    private LittlePerson person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
    }
}
