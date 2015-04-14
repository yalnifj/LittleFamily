package org.finlayfamily.littlefamily.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.PersonHeritageChartView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseCultureActivity extends Activity implements HeritageCalculatorTask.Listener {

    private LittlePerson person;
    private Map<String, HeritagePath> cultures;
    private PersonHeritageChartView chartView;
    private TextView titleView;
    private TextView personNameView;
    private TextView cultureNameView;
    private ImageView portraitImage;
    private ImageView dollImage;
    private HeritagePath selectedPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_culture);

        chartView = (PersonHeritageChartView) findViewById(R.id.personChart);
        titleView = (TextView) findViewById(R.id.titleText);

        personNameView = (TextView) findViewById(R.id.personNameTextView);
        personNameView.setText("");
        cultureNameView = (TextView) findViewById(R.id.cultureNameTextView);
        cultureNameView.setText("");

        portraitImage = (ImageView) findViewById(R.id.portraitImage);
        dollImage = (ImageView) findViewById(R.id.dollImage);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        chartView.setPerson(person);

        HeritageCalculatorTask task = new HeritageCalculatorTask(this, this);
        task.execute(person);
    }

    public void setSelectedPath(HeritagePath selectedPath) {
        this.selectedPath = selectedPath;
        LittlePerson relative = selectedPath.getTreePath().get(selectedPath.getTreePath().size() - 1);
        this.personNameView.setText(relative.getName());
        Bitmap bm = null;
        if (relative.getPhotoPath() != null) {
            bm = ImageHelper.loadBitmapFromFile(relative.getPhotoPath(), ImageHelper.getOrientation(relative.getPhotoPath()), this.portraitImage.getWidth(), this.portraitImage.getHeight(), false);
        } else {
            bm = ImageHelper.loadBitmapFromResource(this, relative.getDefaultPhotoResource(), 0, this.portraitImage.getWidth(), this.portraitImage.getHeight());
        }
        this.portraitImage.setImageBitmap(bm);

        this.cultureNameView.setText(selectedPath.getPlace());
    }

    @Override
    public void onComplete(ArrayList<HeritagePath> paths)
    {
        cultures = new HashMap<String, HeritagePath>();

        for(HeritagePath path : paths) {
            String place = path.getPlace();
            if (cultures.get(place)==null) {
                cultures.put(place, path);
            } else {
                Double percent = cultures.get(place).getPercent() + path.getPercent();
                if (cultures.get(place).getTreePath().size() <= path.getTreePath().size()) {
                    cultures.get(place).setPercent(percent);
                } else {
                    path.setPercent(percent);
                    cultures.put(place, path);
                }
            }
        }

        List<HeritagePath> uniquepaths = new ArrayList<>(cultures.values());
        Collections.sort(uniquepaths);
        chartView.setHeritageMap(uniquepaths);

        if (uniquepaths.size()>0) {
            setSelectedPath(uniquepaths.get(0));
        }
        titleView.setText(R.string.choose_culture);
    }
}
