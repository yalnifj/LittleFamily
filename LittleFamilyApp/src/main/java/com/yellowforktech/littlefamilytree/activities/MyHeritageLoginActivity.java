package com.yellowforktech.littlefamilytree.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.familygraph.android.DialogError;
import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.remote.familygraph.MyHeritageService;

public class MyHeritageLoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_heritage_login);
    }

    @Override
    protected void onStart() {
        super.onStart();

        MyHeritageService service = new MyHeritageService();
        service.getFamilyGraph().authorize(this, new FamilyGraph.DialogListener() {
            @Override
            public void onComplete(Bundle values) {
                Log.d("MyHeritageLoginActivity", "onComplete: "+values);
            }

            @Override
            public void onFamilyGraphError(FamilyGraphError e) {
                Log.d("MyHeritageLoginActivity", "onFamilyGraphError: "+e);
            }

            @Override
            public void onError(DialogError e) {
                Log.d("MyHeritageLoginActivity", "onError: "+e);
            }

            @Override
            public void onCancel() {
                Log.d("MyHeritageLoginActivity", "onCancel: ");
            }
        });
    }
}
