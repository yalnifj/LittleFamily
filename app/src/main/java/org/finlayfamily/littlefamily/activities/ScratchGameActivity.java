package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.FamilyLoaderTask;
import org.finlayfamily.littlefamily.activities.tasks.FileDownloaderTask;
import org.finlayfamily.littlefamily.activities.tasks.MemoriesLoaderTask;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.conclusion.Person;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScratchGameActivity extends Activity implements MemoriesLoaderTask.Listener, FileDownloaderTask.Listener {

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;
    private FamilySearchService service;

    private ProgressDialog pd;
    private ImageView layeredImage;
    private String imagePath;
    private Bitmap imageBitmap;

    private int backgroundLoadIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_game);

        service = FamilySearchService.getInstance();
        layeredImage = (ImageView) findViewById(R.id.layeredImage);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
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

    private void loadMoreFamilyMembers() {
        if (backgroundLoadIndex < people.size() && backgroundLoadIndex < 10) {
            try {
                FamilyLoaderTask task = new FamilyLoaderTask(service.getPerson(people.get(backgroundLoadIndex).getFamilySearchId()), new FamilyLoaderListener(), this);
                if (pd==null) pd = ProgressDialog.show(this, "Please wait...", "Loading data from FamilySearch", true, false);
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
            //-- could not find any images, fallback to a default image
            imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, layeredImage.getWidth(), layeredImage.getHeight());
            setupCanvas();
        }
    }

    @Override
    public void onComplete(ArrayList<SourceDescription> photos) {
        if (photos==null || photos.size()==0) {
            loadMoreFamilyMembers();
        } else {
            Random rand = new Random();
            SourceDescription photoSd = photos.get(rand.nextInt(photos.size()));

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
