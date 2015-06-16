package org.finlayfamily.littlefamily.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.MemoriesLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.WaitTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.Media;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.ScratchView;

import java.util.ArrayList;
import java.util.List;
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

        layeredImage = (ScratchView) findViewById(R.id.layeredImage);
        layeredImage.registerListener(this);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        usedPhotos = new ArrayList<>(5);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    public void setupCanvas() {
        hideLoadingDialog();
        layeredImage.setImageBitmap(imageBitmap);
    }

    private void loadRandomImage() {
        if (people!=null && people.size()>0) {
            Random rand = new Random();
            selectedPerson = people.get(rand.nextInt(people.size()));
            showLoadingDialog();
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
                if (usedPhotos.size()>=5) {
                    usedPhotos.remove(0);
                }
                usedPhotos.add(photo);
                int width = layeredImage.getWidth()/2;
                int height = layeredImage.getHeight()/2;
                if (width<5) width = getScreenWidth()/2;
                if (height<5) height = getScreenHeight()/2 - 25;
                if (imageBitmap!=null) {
                    imageBitmap.recycle();
                }
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, width, height, true);
                setupCanvas();
            } else {
                loadRandomImage();
            }
        }
    }

    @Override
    public void onScratchComplete() {
        playCompleteSound();

        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) {
                if (progress==50) {
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
