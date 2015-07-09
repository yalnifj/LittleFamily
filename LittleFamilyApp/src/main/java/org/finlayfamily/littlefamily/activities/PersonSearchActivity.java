package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.PersonSearchListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.SearchLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PersonSearchActivity extends Activity implements SearchLoaderTask.Listener {

    private EditText txtGivenName;
    private EditText txtSurname;
    private EditText txtRemoteId;
    private ListView personList;
    private PersonSearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_search);

        txtGivenName = (EditText) findViewById(R.id.txtGivenName);
        txtSurname = (EditText) findViewById(R.id.txtSurname);
        txtRemoteId = (EditText) findViewById(R.id.txtRemoteId);
        personList = (ListView) findViewById(R.id.personList);

        adapter = new PersonSearchListAdapter(this);
        personList.setAdapter(adapter);
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

    @Override
    public void onSearchComplete(ArrayList<LittlePerson> people) {
        adapter.setPeople(people);
    }
}
