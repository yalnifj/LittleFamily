package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;

public class ChooseRemoteService extends LittleFamilyActivity {
    public static final int REMOTE_SERVICE_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_remote_service);
    }

    public void chooseFamilySearch(View view) {
        Intent intent = new Intent( this, FSLoginActivity.class );
        startActivityForResult(intent, REMOTE_SERVICE_LOGIN);
    }

    public void choosePGV(View view) {
        Intent intent = new Intent( this, PGVLoginActivity.class );
        startActivityForResult(intent, REMOTE_SERVICE_LOGIN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (REMOTE_SERVICE_LOGIN) : {
                if (resultCode == Activity.RESULT_OK) {
                    LittlePerson currentPerson = (LittlePerson) data.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
                    if (currentPerson!=null) {
                        Intent intent = new Intent( this, ChooseFamilyMember.class );
                        startActivity(intent);
                    }
                }
                break;
            }
        }
    }
}
