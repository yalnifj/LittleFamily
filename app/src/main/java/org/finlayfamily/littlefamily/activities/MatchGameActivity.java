package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.MatchGameListAdapter;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.MatchPerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.games.MatchingGame;

import java.util.List;

public class MatchGameActivity extends Activity implements AdapterView.OnItemClickListener  {

    private static long FLIP_OVER_DELAY = 1500;
    private MatchingGame game;
    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private FamilySearchService service;
    private MatchGameListAdapter adapter;
    private GridView gridView;
    private int flipCount;
    private Handler flipHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        if (people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }

        game = new MatchingGame(1, people);
        game.setupLevel();
        flipCount = 0;

        gridView = (GridView) findViewById(R.id.gridViewMatch);
        adapter = new MatchGameListAdapter(this);
        adapter.setFamily(game.getBoard());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (flipCount<2) {
            MatchPerson person = (MatchPerson) adapter.getItem(position);
            if (!person.isFlipped()) {
                person.setFlipped(true);
                flipCount++;
                if (flipCount==2) {
                    flipHandler = new Handler();
                    flipHandler.postDelayed(new flipOverHandler(), FLIP_OVER_DELAY);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    public class flipOverHandler implements Runnable {
        @Override
        public void run() {
            for(MatchPerson p : game.getBoard()) {
                if (!p.isMatched()) p.setFlipped(false);
            }
            flipCount = 0;
            adapter.notifyDataSetChanged();
        }
    }
}
