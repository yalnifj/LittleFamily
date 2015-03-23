package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;

public class ChooseGameActivity extends Activity {

    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game);

        Intent intent = getIntent();
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        if (people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }

    }

    public void startMatchGame(View view) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        startActivity(intent);
    }

    public void startScratchGame(View view) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        startActivity(intent);
    }

    public void startColoringGame(View view) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        startActivity(intent);
    }
}
