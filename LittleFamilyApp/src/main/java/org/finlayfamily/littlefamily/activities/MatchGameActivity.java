package org.finlayfamily.littlefamily.activities;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.adapters.MatchGameListAdapter;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.MatchPerson;
import org.finlayfamily.littlefamily.games.MatchingGame;

import java.util.ArrayList;
import java.util.List;

public class MatchGameActivity extends LittleFamilyActivity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener {

    private static long FLIP_OVER_DELAY = 2500;
    private static long FLIP_TIME = 800;
    private MatchingGame game;
    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private MatchGameListAdapter adapter;
    private GridView gridView;
    private int flipCount;
    private Handler flipHandler;
	private int flip1 = -1;
	private int flip2 = -1;

    private int backgroundLoadIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        game = new MatchingGame(1, people);
        game.setupLevel();
        flipCount = 0;

        gridView = (GridView) findViewById(R.id.gridViewMatch);
        adapter = new MatchGameListAdapter(this);
        adapter.setFamily(game.getBoard());
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        updateColumns();

        setupTopBar();

        if (people==null) {
            people = new ArrayList<>();
            people.add(selectedPerson);
        }
        if (people.size()<2) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            task.execute(people.get(backgroundLoadIndex));
        }
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
			new flipOverHandler(null).run();
		}
		MatchPerson person = (MatchPerson) adapter.getItem(position);
        String name = person.getPerson().getGivenName();

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
				flipHandler.postDelayed(new flipOverHandler(name), FLIP_OVER_DELAY);
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
        if (people.size() < game.getBoard().size()/2) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            task.execute(people.get(backgroundLoadIndex % people.size()));
        }
    }

    public class flipOverHandler implements Runnable {
        String name;

        public flipOverHandler(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            //-- TODO get relationship name
            if (name != null) {
                speak(name);
            }
            if (game.allMatched()) {
                if (backgroundLoadIndex<people.size()) {
                    FamilyLoaderTask task = new FamilyLoaderTask(MatchGameActivity.this, MatchGameActivity.this);
                    task.execute(people.get(backgroundLoadIndex));
                }
                playCompleteSound();
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
