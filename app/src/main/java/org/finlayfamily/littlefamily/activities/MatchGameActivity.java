package org.finlayfamily.littlefamily.activities;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.MatchGameListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.data.DataService;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.MatchPerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.games.MatchingGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchGameActivity extends Activity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener, FamilyLoaderTask.Listener {

    private static long FLIP_OVER_DELAY = 2000;
    private static long FLIP_TIME = 700;
    private MatchingGame game;
    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
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
        updateColumns();

        tts = new TextToSpeech(this, this);

        if (people!=null && people.size()>1) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            task.execute(people.get(backgroundLoadIndex));
        }
    }

    @Override
    public void onInit(int code) {
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(0.5f);
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColumns();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (flipCount>=2) {
			// user clicked before the previous images flipped so 
			// speed up the flipover by clearing the handler and
			// flipping in the same thread. then proceed to flip the next card
			flipHandler.removeCallbacksAndMessages(null);
			new flipOverHandler().run();
		}
		MatchPerson person = (MatchPerson) adapter.getItem(position);
		if (tts != null) {
			String name = person.getPerson().getGivenName();
			//-- TODO get relationship name
			if (name != null) {
				if (Build.VERSION.SDK_INT > 20) {
					tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
				}
				else {
					tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		}
		if (!person.isFlipped()){
			if (flip1 < 0) flip1 = position;
			else flip2 = position;
			person.setFlipped(true);
			flipCount++;
			if (flipCount == 2) {
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
            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.flipping);
            anim.setTarget(view);
            anim.setDuration(FLIP_TIME);
            anim.start();
			adapter.notifyDataSetChanged();
		}
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> family) {
        for(LittlePerson p : family) {
            if (!people.contains(p)) people.add(p);
        }

        backgroundLoadIndex++;
        if (backgroundLoadIndex < game.getBoard().size()/2) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            task.execute(people.get(backgroundLoadIndex));
        }
    }

    public class flipOverHandler implements Runnable {
        @Override
        public void run() {
            if (game.allMatched()) {
                game.levelUp();
                adapter.setFamily(game.getBoard());
                updateColumns();
            }
            int pos = 0;
            for(MatchPerson p : game.getBoard()) {
                if (!p.isMatched()) {
                    if (p.isFlipped()) {
                        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(MatchGameActivity.this, R.animator.flipping);
                        View view = gridView.getChildAt(pos);
                        anim.setTarget(view);
                        anim.setDuration(FLIP_TIME);
                        anim.start();
                        p.setFlipped(false);
                    }
                }
                pos++;
            }
            flipCount = 0;
            adapter.notifyDataSetChanged();
        }
    }

    private void updateColumns() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int cols = 2;
        while(cols < 12 && (width / cols) * Math.ceil(((double)adapter.getCount()) / cols) > height) cols++;
        if (adapter.getCount() % cols > 0) cols--;
        gridView.setNumColumns(cols);
    }
}
