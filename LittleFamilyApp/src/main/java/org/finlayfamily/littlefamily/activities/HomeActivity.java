package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.sprites.ClippedAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.sprites.MovingAnimatedBitmapSprite;
import org.finlayfamily.littlefamily.views.SpritedClippedSurfaceView;

import java.util.ArrayList;

public class HomeActivity extends LittleFamilyActivity {

    private LittlePerson selectedPerson;
    private ArrayList<LittlePerson> people;

    private SpritedClippedSurfaceView homeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeView = (SpritedClippedSurfaceView) findViewById(R.id.homeView);

        Intent intent = getIntent();
        people = (ArrayList<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        if (selectedPerson==null && people!=null && people.size()>0) {
            selectedPerson = people.get(0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (homeView.getSprites().size()==0) {
            setupHomeViewSprites();
        }
    }

    private void setupHomeViewSprites() {
        //-- background
        Bitmap backBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.house_background);
        float scale = 1.0f;
        int maxWidth = (int) (backBitmap.getWidth() * scale);
        int maxHeight = (int) (backBitmap.getHeight() * scale);
        ClippedAnimatedBitmapSprite homeBackground = new ClippedAnimatedBitmapSprite(backBitmap, backBitmap.getWidth(), backBitmap.getHeight());
        homeBackground.setWidth(homeView.getWidth());
        homeBackground.setHeight(homeView.getHeight());
        homeBackground.setScale(scale);
        homeView.setBackgroundSprite(homeBackground);

        Bitmap cloudBm1 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud1);
        MovingAnimatedBitmapSprite cloud1 = new MovingAnimatedBitmapSprite(cloudBm1, maxWidth, maxHeight);
        cloud1.setWidth((int) (cloudBm1.getWidth()*scale));
        cloud1.setHeight((int) (cloudBm1.getHeight() * scale));
        cloud1.setSlope(0);
        cloud1.setSpeed(0.5f);
        cloud1.setY(30);
        homeView.addSprite(cloud1);

        Bitmap cloudBm2 = BitmapFactory.decodeResource(getResources(), R.drawable.house_cloud2);
        MovingAnimatedBitmapSprite cloud2 = new MovingAnimatedBitmapSprite(cloudBm2, maxWidth, maxHeight);
        cloud2.setWidth((int) (cloudBm2.getWidth()*scale));
        cloud2.setHeight((int) (cloudBm2.getHeight() * scale));
        cloud2.setSlope(0);
        cloud2.setSpeed(0.5f);
        cloud2.setX((int) (backBitmap.getWidth()*0.75));
        cloud2.setY(50);
        homeView.addSprite(cloud2);
    }

    public void startMatchGame(View view) {
        Intent intent = new Intent( this, MatchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startScratchGame(View view) {
        Intent intent = new Intent( this, ScratchGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startColoringGame(View view) {
        Intent intent = new Intent( this, ColoringGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startHeritageDressUpGame(View view) {
        Intent intent = new Intent( this, ChooseCultureActivity.class );
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }

    public void startPuzzleGame(View view) {
        Intent intent = new Intent( this, PuzzleGameActivity.class );
        intent.putExtra(ChooseFamilyMember.FAMILY, people);
        intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
        startActivity(intent);
    }
}
