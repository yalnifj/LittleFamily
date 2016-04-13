package com.yellowforktech.littlefamilytree.activities;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.MatchGameListAdapter;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.MatchPerson;
import com.yellowforktech.littlefamilytree.games.MatchingGame;
import com.yellowforktech.littlefamilytree.games.RecentPersonTracker;

import java.util.ArrayList;
import java.util.List;

public class MatchGameActivity extends LittleFamilyActivity implements AdapterView.OnItemClickListener, FamilyLoaderTask.Listener {

    private static long FLIP_OVER_DELAY = 2000;
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
    private boolean flipping;
    private RecentPersonTracker personTracker;

    private int backgroundLoadIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_game);

        Intent intent = getIntent();
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        people = new ArrayList<>();
        flipCount = 0;

        personTracker = RecentPersonTracker.getInstance();
        if (!personTracker.personRecentlyUsed(selectedPerson)) {
            people.add(selectedPerson);
            personTracker.addPerson(selectedPerson);
        }

        gridView = (GridView) findViewById(R.id.gridViewMatch);
        adapter = new MatchGameListAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setEnabled(true);
        gridView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        gridView.setOnItemClickListener(this);

        setupTopBar();
        updateColumns();
    }

    @Override
    protected void onStart() {
        super.onStart();
        flipping = false;
        DataService.getInstance().registerNetworkStateListener(this);
        FamilyLoaderTask task = new FamilyLoaderTask(this, this);
        if (people.size()>0) {
            task.execute(people.get(backgroundLoadIndex));
        } else {
            task.execute(selectedPerson);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateColumns();
    }

    @Override
    public void onComplete(ArrayList<LittlePerson> family) {
        for(LittlePerson p : family) {
            if (!people.contains(p) && !personTracker.personRecentlyUsed(p)) {
                people.add(p);
            }
        }

        if (people.size() < 2) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            LittlePerson[] arrayPeople = new LittlePerson[family.size()];
            family.toArray(arrayPeople);
            task.execute(arrayPeople);
            return;
        }

        if (game==null) {
            game = new MatchingGame(1, people);
            game.setupLevel();
            adapter.setFamily(game.getBoard());
        }

        backgroundLoadIndex++;
        if (people.size() < game.getBoard().size()/2) {
            FamilyLoaderTask task = new FamilyLoaderTask(this, this);
            task.execute(people.get(backgroundLoadIndex % people.size()));
        }
    }

    @Override
    public void onStatusUpdate(String message) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        if (flipCount>=2) {
            // user clicked before the previous images flipped so
            // speed up the flipover by clearing the handler and
            // flipping in the same thread. then proceed to flip the next card
            flipHandler.removeCallbacksAndMessages(null);
            new flipOverHandler().run();
        }
        MatchPerson person = (MatchPerson) adapter.getItem(position);
        personTracker.addPerson(person.getPerson());

        if (!person.isFlipped()){
            if (flip1 < 0) flip1 = position;
            else flip2 = position;
            person.setFlipped(true);
            //-- TODO get relationship name
            sayGivenNameForPerson(person.getPerson());
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
            if (Build.VERSION.SDK_INT > 17) {
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.flipping);
                anim.setTarget(view);
                anim.setDuration(FLIP_TIME);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        flipping = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        adapter.notifyDataSetChanged();
                        view.clearAnimation();
                        view.invalidate();
                        gridView.invalidate();
                        flipping = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                anim.start();
            }
            adapter.notifyDataSetChanged();
        }
    }

    public class flipOverHandler implements Runnable {

        public flipOverHandler() {
        }

        @Override
        public void run() {
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
                        if (Build.VERSION.SDK_INT > 17) {
                            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(MatchGameActivity.this, R.animator.flipping);
                            final View view = gridView.getChildAt(pos);
                            anim.setTarget(view);
                            anim.setDuration(FLIP_TIME);
                            anim.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    flipping = true;
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    adapter.notifyDataSetChanged();
                                    view.clearAnimation();
                                    view.invalidate();
                                    gridView.invalidate();
                                    flipping = false;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            });
                            anim.reverse();
                        }
                        p.setFlipped(false);
                    }
                }
                pos++;
            }
            flipCount = 0;
            adapter.notifyDataSetChanged();
            gridView.invalidate();
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
