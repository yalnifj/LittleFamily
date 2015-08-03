package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.FamilyLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.MemoriesLoaderTask;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;
import com.yellowforktech.littlefamilytree.games.PuzzleGame;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.PuzzleSurfaceView;

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

        puzzleSurfaceView =(PuzzleSurfaceView) findViewById(R.id.puzzleSurfaceView);
        puzzleGame = new PuzzleGame(2, 2);
        puzzleSurfaceView.setGame(puzzleGame);
        puzzleSurfaceView.registerListener(this);
        puzzleSurfaceView.setAnimationDelay(66);
		puzzleSurfaceView.setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = puzzleSurfaceView.getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        usedPhotos = new ArrayList<>(3);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
		Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
		puzzleSurfaceView.setStarBitmap(starBitmap);
        DataService.getInstance().registerNetworkStateListener(this);
        if (people==null) {
            people = new ArrayList<>();
            people.add(selectedPerson);
        }
        if (people.size()<2) {
            loadMoreFamilyMembers();
        } else {
            loadRandomImage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
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

        @Override
        public void onStatusUpdate(String message) {

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
                    loadMoreFamilyMembers();
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