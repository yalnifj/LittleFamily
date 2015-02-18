package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.FileDownloaderTask;
import org.finlayfamily.littlefamily.activities.tasks.MemoriesLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.ScratchView;
import org.gedcomx.conclusion.Person;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ScratchGameActivity extends Activity implements TextToSpeech.OnInitListener,
            MemoriesLoaderTask.Listener, FileDownloaderTask.Listener, ScratchView.ScratchCompleteListener {

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private FamilySearchService service;

    private ProgressDialog pd;
    private ScratchView layeredImage;
    private String imagePath;
    private Bitmap imageBitmap;
    private SourceDescription photoSd;

    private int backgroundLoadIndex = 1;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_game);

        service = FamilySearchService.getInstance();
        layeredImage = (ScratchView) findViewById(R.id.layeredImage);
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

            if (pd==null) pd = ProgressDialog.show(this, "Please wait...", "Loading data from FamilySearch", true, false);
            try {
                Person person = service.getPerson(selectedPerson.getFamilySearchId());
                if (person!=null) {
                    MemoriesLoaderTask task = new MemoriesLoaderTask(person, false, this, this);
                    task.execute();
                }
            } catch (FamilySearchException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMoreFamilyMembers(boolean showdialog) {
        if (backgroundLoadIndex < people.size() && backgroundLoadIndex < 20) {
            try {
                FamilyLoaderTask task = new FamilyLoaderTask(service.getPerson(people.get(backgroundLoadIndex).getFamilySearchId()), new FamilyLoaderListener(), this);
                if (pd==null && showdialog) pd = ProgressDialog.show(this, "Please wait...", "Loading data from FamilySearch", true, false);
                task.execute();
            } catch (FamilySearchException e) {
                e.printStackTrace();
            }
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
    public void onComplete(ArrayList<SourceDescription> photos) {
        if (photos==null || photos.size()==0) {
            if (backgroundLoadIndex < 20) loadMoreFamilyMembers(true);
            else {
                //-- could not find any images, fallback to a default image
                imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, layeredImage.getWidth(), layeredImage.getHeight());
                setupCanvas();
            }
        } else {
            Random rand = new Random();
            photoSd = photos.get(rand.nextInt(photos.size()));

            List<Link> links = photoSd.getLinks();
            if (links!=null) {
                for (Link photoLink : links) {
                    if (photoLink.getRel() != null && photoLink.getRel().equals("image")) {
                        FileDownloaderTask task = new FileDownloaderTask(photoLink.getHref().toString(), selectedPerson.getFamilySearchId(), this, this);
                        task.execute();
                    }
                }
            }
        }
    }

    @Override
    public void onScratchComplete() {
        loadMoreFamilyMembers(false);
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

    @Override
    public void onComplete(String photoPath) {
        imagePath = photoPath;
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
