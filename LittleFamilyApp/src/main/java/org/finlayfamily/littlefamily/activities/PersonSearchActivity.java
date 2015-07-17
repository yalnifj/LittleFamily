package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.PersonSearchListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.ForceSynceTask;
import org.finlayfamily.littlefamily.activities.tasks.SearchLoaderTask;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PersonSearchActivity extends Activity implements SearchLoaderTask.Listener, ForceSynceTask.Listener, AdapterView.OnItemClickListener {

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

        registerForContextMenu(personList);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.person_list_context_menu, menu);

        MenuItem item = menu.findItem(R.id.hide_show);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        long itemID = info.position;
        LittlePerson person = (LittlePerson) adapter.getItem((int) itemID);
        if (person.isActive()) item.setTitle(getResources().getString(R.string.hide_person));
        else item.setTitle(getResources().getString(R.string.show_person));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long itemID = info.position;
        LittlePerson person = (LittlePerson) adapter.getItem((int) itemID);
        switch (item.getItemId()) {
            case R.id.force_sync:
                ForceSynceTask task = new ForceSynceTask(PersonSearchActivity.this, PersonSearchActivity.this);
                task.execute();
                return true;
            case R.id.hide_show:
                person.setActive(!person.isActive());
                try {
                    DataService.getInstance().getDBHelper().persistLittlePerson(person);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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

    @Override
    public void onComplete(LittlePerson person) {
        Toast.makeText(this, person.getName()+" synced succesfully", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        parent.showContextMenuForChild(view);
    }
}
