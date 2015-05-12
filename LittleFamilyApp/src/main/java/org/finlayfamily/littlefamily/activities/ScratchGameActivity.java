package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.MemoriesLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.Media;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.ScratchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ScratchGameActivity extends LittleFamilyActivity implements MemoriesLoaderTask.Listener, ScratchView.ScratchCompleteListener {

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;

    private ProgressDialog pd;
    private ScratchView layeredImage;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;
    private List<Media> usedPhotos;

    private int backgroundLoadIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_game);
        setupTopBar();

        layeredImage = (ScratchView) findViewById(R.id.layeredImage);
        layeredImage.registerListener(this);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);

        usedPhotos = new ArrayList<>(3);

        loadRandomImage();
    }

    public void setupCanvas() {
        layeredImage.setImageBitmap(imageBitmap);
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
            if (pd!=null) {
                pd.dismiss();
                pd = null;
            }
            loadRandomImage();
        }
    }

    @Override
    public void onComplete(ArrayList<Media> photos) {
        if (photos==null || photos.size()==0) {
            if (backgroundLoadIndex < 20) loadMoreFamilyMembers();
            else {
                //-- could not find any images, fallback to a default image
                imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, layeredImage.getWidth(), layeredImage.getHeight());
                setupCanvas();
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
                if (pd!=null) {
                    pd.dismiss();
                    pd = null;
                }
                if (usedPhotos.size()>=3) {
                    usedPhotos.remove(0);
                }
                usedPhotos.add(photo);
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, layeredImage.getWidth(), layeredImage.getHeight(), true);
                setupCanvas();
            } else {
                loadRandomImage();
            }
        }
    }

    @Override
    public void onScratchComplete() {
        loadMoreFamilyMembers();
        if (tts != null) {
            String name = selectedPerson.getGivenName();
            if (name != null) {
                if (Build.VERSION.SDK_INT > 20) {
                    tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
                }
                else {
                    tts.speak(name, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }
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
}
