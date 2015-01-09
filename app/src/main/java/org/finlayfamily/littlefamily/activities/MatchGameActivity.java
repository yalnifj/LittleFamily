package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.games.MatchingGame;
import org.gedcomx.conclusion.Person;

import java.util.List;

public class MatchGameActivity extends ActionBarActivity implements AdapterView.OnItemClickListener  {

    private MatchingGame game;
    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private FamilySearchService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        try {
            Person fsPerson = service.getPerson(selectedPerson.getFamilySearchId());
            fsPerson.getLinks();
        } catch (FamilySearchException e) {
            e.printStackTrace();
        }


        game = new MatchingGame(1, people);
        game.setupLevel();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
