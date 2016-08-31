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
import com.yellowforktech.littlefamilytree.db.FireHelper;
import com.yellowforktech.littlefamilytree.remote.familygraph.MyHeritageService;

import java.util.ArrayList;

public class MyHeritageLoginActivity extends Activity implements PersonLoaderTask.Listener, InitialDataLoaderTask.Listener {

    private MyHeritageService service;
    private DataService dataService;

    private TextView welcomeText;
    private TextView detailText;
    private ProgressBar progressBar;

    private FireHelper fireHelper;

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

        fireHelper = FireHelper.getInstance();
        fireHelper.authenticate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        service.getFamilyGraph().authorizeCallback(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String[] perms = {"basic", "offline_access"};
        service = new MyHeritageService();
        service.getFamilyGraph().authorize(this, perms, new FamilyGraph.DialogListener() {
            @Override
            public void onComplete(Bundle values) {
                Log.d("MyHeritageLoginActivity", "onComplete: "+values);

                try {
                    String token = values.getString("access_token");
                    dataService.saveEncryptedProperty(DataService.SERVICE_TYPE_MYHERITAGE + DataService.SERVICE_TOKEN, token);
                    service.authWithToken(token);
                } catch (Exception e) {
                    Log.e("MyHeritageLoginActivity", "error saving token", e);
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
                Log.e("MyHeritageLoginActivity", "onFamilyGraphError: ", e);
                welcomeText.setText(e.getMessage());
            }

            @Override
            public void onError(DialogError e) {
                Log.e("MyHeritageLoginActivity", "onError: ",e);
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
            dataService.getDBHelper().saveProperty(DataService.SERVICE_USERNAME, service.getUserId());
            fireHelper.createOrUpdateUser(service.getUserId(),
                    dataService.getServiceType(), false);
        } catch (Exception e) {
            Log.e("MyHeritageLoginActivity", "Error saving to firebase", e);
        }
        try {
            dataService.getDBHelper().saveProperty(DataService.ROOT_PERSON_ID, String.valueOf(person.getId()));
        } catch (Exception e) {
            Log.e("MyHeritageLoginActivity", "Error saving property", e);
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
