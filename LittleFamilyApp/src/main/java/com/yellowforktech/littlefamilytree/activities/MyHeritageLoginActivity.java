package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.familygraph.android.DialogError;
import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.remote.familygraph.MyHeritageService;

import org.json.JSONObject;

public class MyHeritageLoginActivity extends Activity {

    private MyHeritageService service;

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_heritage_login);

        welcomeText = (TextView) findViewById(R.id.welcomeTxt);
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
                JSONObject user = service.getCurrentUser();
                Log.d("MyHeritageLoginActivity", user.toString());
                /*
                {"id":"user-586269521","name":"John Finlay","first_name":"John","last_name":"Finlay","nickname":"Member","gender":"M","preferred_display_language":"EN","preferred_email_language":"EN","link":"https:\/\/www.myheritage.com\/member-586269521_1\/john-finlay","birth_date":{"text":"1976","date":"1976","structured_date":{"first_date":{"year":1976,"type":"exact","class_name":"SingleDate"},"type":"exact","class_name":"StructuredDate"},"class_name":"EventDate"},"country_code":"US","country":"USA","created_time":"2015-10-07T03:40:05+0000","last_visit_time":"2016-06-01T20:06:00+0000","is_public":true,"show_age":true,"allow_posting_comments":true,"notify_on_comment":true,"show_real_name":true,
                "default_site":{"id":"site-309415061","name":"Finlay Web Site"},
                "default_tree":{"id":"tree-309415061-1","name":"Finlay Family Tree"},
                "default_individual":{"id":"individual-309415061-1500003","name":"John Finlay"},
                "mailbox":{"id":"mailbox-586269521"},"class_name":"User"}
                */
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
                welcomeText.setText("Authorization cancelled.");
            }
        });
    }
}
