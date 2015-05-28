package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.MemoriesLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.WaitTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.Media;
import org.finlayfamily.littlefamily.games.PuzzleGame;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.PuzzleSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PuzzleGameActivity extends LittleFamilyActivity implements MemoriesLoaderTask.Listener, PuzzleSurfaceView.PuzzleCompleteListener {
    private List<LittlePerson> people;
    private PuzzleSurfaceView puzzleSurfaceView;
    private PuzzleGame puzzleGame;
    private int backgroundLoadIndex = 1;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;
    private List<Media> usedPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_game);
        setupTopBar();

        puzzleSurfaceView =(PuzzleSurfaceView) findViewById(R.id.puzzleSurfaceView);
        puzzleGame = new PuzzleGame(2, 2);
        puzzleSurfaceView.setGame(puzzleGame);
        puzzleSurfaceView.registerListener(this);
        puzzleSurfaceView.setAnimationDelay(66);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);

        usedPhotos = new ArrayList<>(3);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoadingDialog();
        loadRandomImage();
    }

    private void loadRandomImage() {
        if (people!=null && people.size()>0) {
            Random rand = new Random();
            selectedPerson = people.get(rand.nextInt(people.size()));
            MemoriesLoaderTask task = new MemoriesLoaderTask(this, this);
            task.execute(selectedPerson);
        }
    }

    private void loadMoreFamilyMembers() {
        showLoadingDialog();
        if (backgroundLoadIndex < people.size() && backgroundLoadIndex < 20) {
            FamilyLoaderTask task = new FamilyLoaderTask(new FamilyLoaderListener(), this);
            task.execute(people.get(backgroundLoadIndex));
        }
        else {
            loadRandomImage();
        }
    }

    public void setupGame() {
        int rows = puzzleGame.getRows();
        int cols = puzzleGame.getCols();
        if (puzzleGame.isCompleted()) {
            if (rows < cols) rows++;
            else if (cols < rows) cols++;
            else {
                if (puzzleSurfaceView.getWidth() > puzzleSurfaceView.getHeight()) cols++;
                else rows++;
            }
            puzzleGame.setupLevel(rows, cols);
        }
        hideLoadingDialog();
        puzzleSurfaceView.setBitmap(imageBitmap);
    }

    public class FamilyLoaderListener implements FamilyLoaderTask.Listener {
        @Override
        public void onComplete(ArrayList<LittlePerson> family) {
            for(LittlePerson p : family) {
                if (!people.contains(p)) people.add(p);
            }

            backgroundLoadIndex++;
            loadRandomImage();
        }
    }

    @Override
    public void onComplete(ArrayList<Media> photos) {
        if (photos==null || photos.size()==0) {
            if (backgroundLoadIndex < 20) loadMoreFamilyMembers();
            else {
                //-- could not find any images, fallback to a default image
                int width = puzzleSurfaceView.getWidth();
                int height = puzzleSurfaceView.getHeight();
                if (width<5) width = 300;
                if (height<5) height = 300;
                imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, width, height);
                setupGame();
            }
        } else {
            Random rand = new Random();
            int index = rand.nextInt(photos.size());
            int origIndex = index;
            photo = photos.get(index);
            while(usedPhotos.contains(photo)) {
                index++;
                if (index >= photos.size()) index = 0;
                photo = photos.get(index);
                //-- stop if we've used all of these images
                if (index==origIndex) {
                    loadRandomImage();
                    return;
                }
            }
            imagePath = photo.getLocalPath();
            if (imagePath!=null) {
                if (usedPhotos.size()>=5) {
                    usedPhotos.remove(0);
                }
                usedPhotos.add(photo);
                int width = puzzleSurfaceView.getWidth();
                int height = puzzleSurfaceView.getHeight();
                if (width<5) width = 300;
                if (height<5) height = 300;
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, width, height, true);
                setupGame();
            } else {
                loadRandomImage();
            }
        }
    }

    @Override
    public void onPuzzleComplete() {
        playCompleteSound();

        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
                if (progress==40) {
                    String name = selectedPerson.getGivenName();
                    speak(name);
                }
            }

            @Override
            public void onComplete(Integer progress) {
                loadMoreFamilyMembers();
            }
        });
        waiter.execute(3000L);
    }
}
