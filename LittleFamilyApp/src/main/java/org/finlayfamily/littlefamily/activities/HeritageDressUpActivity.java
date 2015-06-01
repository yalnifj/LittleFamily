package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.DressUpDollsAdapter;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.games.DollConfig;
import org.finlayfamily.littlefamily.views.DressUpView;

public class HeritageDressUpActivity extends LittleFamilyActivity implements DressUpView.DressedListener, AdapterView.OnItemClickListener {

    private DollConfig dollConfig;
    private DressUpView dressUpView;
    private DressUpDollsAdapter adapter;
    private GridView dollGrid;
    private LittlePerson person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heritage_dress_up);

        Intent intent = getIntent();
        dollConfig = (DollConfig) intent.getSerializableExtra(ChooseCultureActivity.DOLL_CONFIG);
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        dressUpView = (DressUpView) findViewById(R.id.dress_up_view);
        dollGrid = (GridView) findViewById(R.id.dollGrid);
        adapter = new DressUpDollsAdapter(this, person);
        dollGrid.setAdapter(adapter);
        dollGrid.setNumColumns(adapter.getCount());
        dollGrid.setOnItemClickListener(this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dressUpView.setDollConfig(dollConfig);
        dressUpView.addListener(this);
        dollGrid.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDressed() {
        playCompleteSound();
        dollGrid.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dollConfig = (DollConfig) adapter.getItem(position);
        dressUpView.setDollConfig(dollConfig);
        dollGrid.setVisibility(View.INVISIBLE);
    }
}
