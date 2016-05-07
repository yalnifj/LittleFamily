package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.PersonSearchListAdapter;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.SearchLoaderTask;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PersonSearchActivity extends Activity implements SearchLoaderTask.Listener, AdapterView.OnItemClickListener {

    private EditText txtGivenName;
    private EditText txtSurname;
    private EditText txtRemoteId;
    private ListView personList;
    private PersonSearchListAdapter adapter;
    private LittlePerson selectedPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_search);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        txtGivenName = (EditText) findViewById(R.id.txtGivenName);
        txtSurname = (EditText) findViewById(R.id.txtSurname);
        txtRemoteId = (EditText) findViewById(R.id.txtRemoteId);
        personList = (ListView) findViewById(R.id.personList);

        adapter = new PersonSearchListAdapter(this);
        personList.setAdapter(adapter);
        personList.setOnItemClickListener(this);
    }

    public void search(View v) {
        Map<String, String> params = new HashMap<>();
        String remoteId = txtRemoteId.getText().toString();
        if (remoteId!=null && !remoteId.isEmpty()) {
            params.put("remoteId", remoteId);
            SearchLoaderTask task = new SearchLoaderTask(this, this);
            task.execute(params);
        } else {
            String given = txtGivenName.getText().toString();
            String surname = txtSurname.getText().toString();
            if ((given!=null && !given.isEmpty()) || (surname!=null && !surname.isEmpty())) {
                params.put("givenName", given);
                params.put("surname", surname);
                SearchLoaderTask task = new SearchLoaderTask(this, this);
                task.execute(params);
            }
        }
    }

    public void showMyFamily(View v) {
        FamilyLoaderTask task = new FamilyLoaderTask(new FamilyLoaderTask.Listener() {
            @Override
            public void onComplete(ArrayList<LittlePerson> family) {
                adapter.setPeople(family);
            }

            @Override
            public void onStatusUpdate(String message) {

            }
        }, this);
        task.equals(selectedPerson);
    }

    @Override
    public void onSearchComplete(ArrayList<LittlePerson> people) {
        adapter.setPeople(people);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LittlePerson person = (LittlePerson) personList.getItemAtPosition(position);
        PersonDetailsDialog dialog = new PersonDetailsDialog();
        Bundle args = new Bundle();
        args.putSerializable(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        args.putSerializable("person", person);
        dialog.setArguments(args);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
        dialog.show(getFragmentManager(), "Perosn Details");
    }
}
