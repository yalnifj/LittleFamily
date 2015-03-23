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
import org.finlayfamily.littlefamily.views.ColoringView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ColoringGameActivity extends Activity implements TextToSpeech.OnInitListener,
            MemoriesLoaderTask.Listener, ColoringView.ColoringCompleteListener {

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;

    private ProgressDialog pd;
    private ColoringView layeredImage;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;

    private int backgroundLoadIndex = 1;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloring_game);

        layeredImage = (ColoringView) findViewById(R.id.layeredImage);
        layeredImage.registerListener(this);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);

        tts = new TextToSpeech(this, this);
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
    protected void onStart() {
        super.onStart();

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
            photo = photos.get(rand.nextInt(photos.size()));
            imagePath = photo.getLocalPath();
            if (imagePath!=null) {
                if (pd!=null) {
                    pd.dismiss();
                    pd = null;
                }
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, layeredImage.getWidth(), layeredImage.getHeight(), true);
                setupCanvas();
            } else {
                loadRandomImage();
            }
        }
    }

    @Override
    public void onColoringComplete() {
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
