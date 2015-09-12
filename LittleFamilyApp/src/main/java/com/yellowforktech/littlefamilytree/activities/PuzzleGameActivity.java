package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;
import com.yellowforktech.littlefamilytree.games.PuzzleGame;
import com.yellowforktech.littlefamilytree.games.RandomMediaChooser;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.PuzzleSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class PuzzleGameActivity extends LittleFamilyActivity implements RandomMediaChooser.RandomMediaListener, PuzzleSurfaceView.PuzzleCompleteListener {
    private List<LittlePerson> people;
    private PuzzleSurfaceView puzzleSurfaceView;
    private PuzzleGame puzzleGame;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;
    private RandomMediaChooser mediaChooser;

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

        if (people==null) {
            people = new ArrayList<>();
            people.add(selectedPerson);
        }

        mediaChooser = new RandomMediaChooser(people, this, this);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
		Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
		puzzleSurfaceView.setStarBitmap(starBitmap);
        DataService.getInstance().registerNetworkStateListener(this);

        if (people.size()<2) {
            mediaChooser.loadMoreFamilyMembers();
        } else {
            mediaChooser.loadRandomImage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
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
        puzzleSurfaceView.setBitmap(imageBitmap, selectedPerson.getName());
    }

    @Override
    public void onMediaLoaded(Media media) {
        photo = media;
        selectedPerson = mediaChooser.getSelectedPerson();
        int width = puzzleSurfaceView.getWidth() / 2;
        int height = puzzleSurfaceView.getHeight() / 2;
        if (width < 5) width = getScreenWidth() / 2;
        if (height < 5) height = getScreenHeight() / 2 - 25;
        if (imageBitmap != null && !imageBitmap.isRecycled()) {
            synchronized (imageBitmap) {
                imageBitmap.recycle();
            }
        }
        if (photo==null) {
            //-- could not find any images, fallback to a default image
            imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, width, height);
            setupGame();
        } else {
            imagePath = photo.getLocalPath();
            if (imagePath != null) {
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, width, height, true);
                setupGame();
            } else {
                mediaChooser.loadRandomImage();
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
                mediaChooser.loadMoreFamilyMembers();
            }
        });
        waiter.execute(3000L);
    }
}
