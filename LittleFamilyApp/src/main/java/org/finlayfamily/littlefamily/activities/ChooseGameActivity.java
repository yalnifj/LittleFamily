package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;

public class ChooseGameActivity extends LittleFamilyActivity {

    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game);

        Intent intent = getIntent();
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (selectedPerson==null && people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }
    }

    @Override
    public void onInit(int code) {
        super.onInit(code);
        String message = String.format(getResources().getString(R.string.player_greeting), selectedPerson.getGivenName());
        message += " "+getResources().getString(R.string.what_game);
        speak(message);
    }

    public void startMatchGame(View view) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startScratchGame(View view) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startColoringGame(View view) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startHeritageDressUpGame(View view) {
        Intent intent = new Intent( this, ChooseCultureActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }
}
