package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.MatchGameListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.MatchPerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.games.MatchingGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchGameActivity extends Activity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener, FamilyLoaderTask.Listener {

    private static long FLIP_OVER_DELAY = 1500;
    private MatchingGame game;
    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private FamilySearchService service;
    private MatchGameListAdapter adapter;
    private GridView gridView;
    private int flipCount;
    private Handler flipHandler;
	private int flip1 = -1;
	private int flip2 = -1;

    private int backgroundLoadIndex = 1;

    private TextToSpeech tts;

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

        tts = new TextToSpeech(this, this);

        service = FamilySearchService.getInstance();

        try {
            if (people!=null && people.size()>1) {
                FamilyLoaderTask task = new FamilyLoaderTask(service.getPerson(people.get(backgroundLoadIndex).getFamilySearchId()), this, this);
                task.execute();
            }
        } catch (FamilySearchException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(1.5f);
        } else {
            tts = null;
            //Toast.makeText(this, "Failed to initialize TTS engine.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (flipCount<2) {
            MatchPerson person = (MatchPerson) adapter.getItem(position);
            if (tts!=null) {
                String name = person.getPerson().getGivenName();
                //-- TODO get relationship name
                if (name!=null) {
                    if (Build.VERSION.SDK_INT > 20) {
                        tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            }
            if (!person.isFlipped()) {
				if (flip1<0) flip1 = position;
				else flip2 = position;
                person.setFlipped(true);
                flipCount++;
                if (flipCount==2) {
					if (game.isMatch(flip1, flip2)) {
						MatchPerson person1 = (MatchPerson) adapter.getItem(flip1);
						MatchPerson person2 = (MatchPerson) adapter.getItem(flip2);
						person1.setMatched(true);
						person2.setMatched(true);
					}
					flip1 = -1;
					flip2 = -1;
                    flipHandler = new Handler();
                    flipHandler.postDelayed(new flipOverHandler(), FLIP_OVER_DELAY);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> family) {
        for(LittlePerson p : family) {
            if (!people.contains(p)) people.add(p);
        }

        backgroundLoadIndex++;
        if (backgroundLoadIndex < people.size()) {
            try {
                FamilyLoaderTask task = new FamilyLoaderTask(service.getPerson(people.get(backgroundLoadIndex).getFamilySearchId()), this, this);
                task.execute();
            } catch (FamilySearchException e) {
                e.printStackTrace();
            }
        }
    }

    public class flipOverHandler implements Runnable {
        @Override
        public void run() {
            if (game.allMatched()) {
                game.levelUp();
                adapter.setFamily(game.getBoard());
            }
            for(MatchPerson p : game.getBoard()) {
                if (!p.isMatched()) p.setFlipped(false);
            }
            flipCount = 0;
            adapter.notifyDataSetChanged();
        }
    }
}
