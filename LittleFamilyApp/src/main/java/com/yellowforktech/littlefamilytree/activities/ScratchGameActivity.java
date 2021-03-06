package com.yellowforktech.littlefamilytree.activities;

import android.app.AlertDialog;
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
import com.yellowforktech.littlefamilytree.games.RandomMediaChooser;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.util.RelationshipCalculator;
import com.yellowforktech.littlefamilytree.views.ScratchView;

import java.util.ArrayList;
import java.util.List;

public class ScratchGameActivity extends LittleFamilyActivity implements ScratchView.ScratchCompleteListener, RandomMediaChooser.RandomMediaListener {

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private LittlePerson player;

    private ScratchView layeredImage;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;

    private RandomMediaChooser mediaChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_game);

        layeredImage = (ScratchView) findViewById(R.id.layeredImage);
        layeredImage.registerListener(this);
		layeredImage.setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = layeredImage.getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        player = selectedPerson;

        if (people==null) {
            people = new ArrayList<>();
            people.add(selectedPerson);
        }

        mediaChooser = RandomMediaChooser.getInstance();
		mediaChooser.setActivity(this);
		mediaChooser.setListener(this);
		mediaChooser.addPeople(people);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
		Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
		layeredImage.setStarBitmap(starBitmap);
		
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
        DataService.getInstance().registerNetworkStateListener(this);
    }

    public void setupCanvas() {
        String relationship = RelationshipCalculator.getRelationship(player, selectedPerson, this);
        layeredImage.setImageBitmap(imageBitmap, selectedPerson.getName(), relationship);
    }

    @Override
    public void onScratchComplete() {
        playCompleteSound();

        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
                if (progress==50) {
                    sayGivenNameForPerson(selectedPerson);
                }
            }

            @Override
            public void onComplete(Integer progress) {
                mediaChooser.loadMoreFamilyMembers();
            }
        });
        waiter.execute(3000L);
    }

    @Override
    public void onMediaLoaded(Media media) {
        photo = media;
        selectedPerson = mediaChooser.getSelectedPerson();
        int width = layeredImage.getWidth() / 2;
        int height = layeredImage.getHeight() / 2;
        if (width < 5) width = getScreenWidth() / 2;
        if (height < 5) height = getScreenHeight() / 2 - 25;
        if (imageBitmap != null && !imageBitmap.isRecycled()) {
            imageBitmap.recycle();
        }
        if (photo==null) {
            //-- could not find any images, fallback to a default image
            imageBitmap = null;
            if (selectedPerson!=null) {
                imageBitmap = ImageHelper.loadBitmapFromResource(this, ImageHelper.getPersonDefaultImage(this, selectedPerson), 0, width, height);
            }
            if (imageBitmap != null) {
                setupCanvas();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.low_media);
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();

                mediaChooser.loadRandomImage();
            }
        } else {
            imagePath = photo.getLocalPath();
            if (imagePath != null) {
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, width, height, true);
                setupCanvas();
            } else {
                mediaChooser.loadRandomImage();
            }
        }
    }
}
