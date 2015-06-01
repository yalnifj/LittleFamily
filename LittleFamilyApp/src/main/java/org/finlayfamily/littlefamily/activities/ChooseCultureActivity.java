package org.finlayfamily.littlefamily.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.activities.tasks.HeritageCalculatorTask;
import org.finlayfamily.littlefamily.data.HeritagePath;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.games.DollConfig;
import org.finlayfamily.littlefamily.games.DressUpDolls;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.finlayfamily.littlefamily.views.PersonHeritageChartView;
import org.gedcomx.types.GenderType;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseCultureActivity extends LittleFamilyActivity implements HeritageCalculatorTask.Listener, PersonHeritageChartView.SelectedPathListener {
    public static final String DOLL_CONFIG = "dollConfig";
    private LittlePerson person;
    private Map<String, HeritagePath> cultures;
    private PersonHeritageChartView chartView;
    private TextView titleView;
    private TextView personNameView;
    private TextView cultureNameView;
    private ImageView portraitImage;
    private ImageView dollImage;
    private Button playButton;
    private HeritagePath selectedPath;
    private DressUpDolls dressUpDolls;

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
        portraitImage.setVisibility(View.INVISIBLE);
        dollImage = (ImageView) findViewById(R.id.dollImage);
        dollImage.setVisibility(View.INVISIBLE);

        playButton = (Button) findViewById(R.id.play_button);

        Intent intent = getIntent();
        person = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);
        chartView.setPerson(person);
        chartView.addListener(this);

        dressUpDolls = new DressUpDolls();

        setupTopBar();
    }

    public void startDressUpActivity(View view) {
        if (selectedPath!=null) {
            Intent intent = new Intent(this, HeritageDressUpActivity.class);
            intent.putExtra(ChooseFamilyMember.SELECTED_PERSON, selectedPerson);
            DollConfig dollConfig = dressUpDolls.getDollConfig(selectedPath.getPlace(), person);
            intent.putExtra(DOLL_CONFIG, dollConfig);
            startActivity(intent);
        }
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
        portraitImage.setImageBitmap(bm);
        portraitImage.setVisibility(View.VISIBLE);
		
		String relationship = "";
		int greats = selectedPath.getTreePath().size() - 2;
		for(int g=0; g<greats; g++) {
			relationship = relationship + getResources().getString(R.string.great)+", ";
		}
		if (selectedPath.getTreePath().size()>1) {
			relationship = relationship + getResources().getString(R.string.grand)+", ";
		}
		if (relative.getGender()==GenderType.Female) {
			relationship += getResources().getString(R.string.mother);
		} else {
            relationship += getResources().getString(R.string.father);
		}

        this.cultureNameView.setText(selectedPath.getPlace());
        double percent = selectedPath.getPercent()*100;
        DecimalFormat decf = new DecimalFormat("#.#");
        String percString = decf.format(percent);
		String text = String.format(getResources().getString(R.string.you_are_percent),
                percString, selectedPath.getPlace(),
                relationship);
        if (relative.getBirthDate()!=null) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
            text += " " + String.format(getResources().getString(R.string.name_born_in_date),
                    relative.getName(), relative.getBirthPlace(), df.format(relative.getBirthDate()));
        } else {
            text += " " + relative.getName();
        }
		speak(text);

        DollConfig dollConfig = dressUpDolls.getDollConfig(selectedPath.getPlace(), person);
        String thumbnailFile = dollConfig.getThumbnail();
        try {
            InputStream is = getAssets().open(thumbnailFile);
            Bitmap thumbnail = BitmapFactory.decodeStream(is);
            dollImage.setImageBitmap(thumbnail);
            dollImage.setVisibility(View.VISIBLE);
            is.close();
        } catch (IOException e) {
            Log.e("ChooseCultureActivity", "Error opening asset file", e);
        }

        playButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete(ArrayList<HeritagePath> paths)
    {
        cultures = new HashMap<String, HeritagePath>();
		titleView.setText(R.string.choose_culture);
		speak(getResources().getString(R.string.choose_culture));

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
        
    }
	
	@Override
    public void onInit(int code) {
        super.onInit(code);

        speak(getResources().getString(R.string.calculating_heritage));

        HeritageCalculatorTask task = new HeritageCalculatorTask(this, this);
        task.execute(person);
    }


    @Override
    public void onSelectedPath(HeritagePath path) {
        setSelectedPath(path);
    }
}
