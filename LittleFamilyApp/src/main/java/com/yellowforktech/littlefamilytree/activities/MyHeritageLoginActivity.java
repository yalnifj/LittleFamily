package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.familygraph.android.DialogError;
import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.InitialDataLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.PersonLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.remote.familygraph.MyHeritageService;

import java.util.ArrayList;

public class MyHeritageLoginActivity extends Activity implements PersonLoaderTask.Listener, InitialDataLoaderTask.Listener {

    private MyHeritageService service;
    private DataService dataService;

    private TextView welcomeText;
    private TextView detailText;
    private ProgressBar progressBar;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_heritage_login);

        welcomeText = (TextView) findViewById(R.id.welcomeTxt);
        detailText = (TextView) findViewById(R.id.detailsTxt);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);

        progressBar.animate();

        dataService = DataService.getInstance();
        dataService.setContext(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        service.getFamilyGraph().authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        service = new MyHeritageService();
        service.getFamilyGraph().authorize(this, new FamilyGraph.DialogListener() {
            @Override
            public void onComplete(Bundle values) {
                Log.d("MyHeritageLoginActivity", "onComplete: "+values);

                try {
                    service.authWithToken(values.getString("access_token"));
                } catch (RemoteServiceSearchException e) {
                    e.printStackTrace();
                }
                dataService.setRemoteService(DataService.SERVICE_TYPE_MYHERITAGE, service);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyHeritageLoginActivity.this).edit();
                editor.putString(DataService.SERVICE_TYPE, dataService.getRemoteService().getClass().getSimpleName());
                editor.commit();

                intent = new Intent();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        detailText.setText(getResources().getString(R.string.loading_person));
                        PersonLoaderTask task = new PersonLoaderTask(MyHeritageLoginActivity.this, MyHeritageLoginActivity.this);
                        task.setIgnoreLocal(true);
                        task.execute();
                    }
                });
            }

            @Override
            public void onFamilyGraphError(FamilyGraphError e) {
                Log.d("MyHeritageLoginActivity", "onFamilyGraphError: "+e);
                welcomeText.setText(e.getMessage());
            }

            @Override
            public void onError(DialogError e) {
                Log.d("MyHeritageLoginActivity", "onError: "+e);
                welcomeText.setText(e.getMessage());
            }

            @Override
            public void onCancel() {
                Log.d("MyHeritageLoginActivity", "onCancel: ");
                welcomeText.setText(getResources().getString(R.string.auth_cancelled));
            }
        });
    }

    @Override
    public void onComplete(LittlePerson person) {
        detailText.setText(getResources().getString(R.string.loading_close));
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, person);
        try {
            dataService.getDBHelper().saveProperty(DataService.ROOT_PERSON_ID, String.valueOf(person.getId()));
        } catch (Exception e) {
            Log.e("PGVLoginActivity", "Error saving property", e);
        }
        InitialDataLoaderTask task = new InitialDataLoaderTask(this, this);
        task.execute(person);
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> family) {
        dataService.resumeSync();
        intent.putExtra(ChooseFamilyMember.FAMILY, family);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onStatusUpdate(String message) {
        detailText.setText(message);
    }
}
