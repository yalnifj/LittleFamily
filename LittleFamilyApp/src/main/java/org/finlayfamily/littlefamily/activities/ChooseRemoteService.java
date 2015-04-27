package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.finlayfamily.littlefamily.R;

public class ChooseRemoteService extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_remote_service);
    }

    public void chooseFamilySearch(View view) {
        Intent intent = new Intent( this, FSLoginActivity.class );
        startActivity(intent);
    }

    public void choosePGV(View view) {
        Intent intent = new Intent( this, FSLoginActivity.class );
        startActivity(intent);
    }
}
