package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.HeritageCalculatorTask;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.HeritagePath;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.PersonHeritageChartView;

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
    private long starttime;

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

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        chartView.setDensity(dm.density);

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
		for(int g=1; g<greats; g++) {
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
    public void onComplete(final ArrayList<HeritagePath> paths)
    {
        WaitTask waiter = new WaitTask(new WaitTask.WaitTaskListener() {
            @Override
            public void onProgressUpdate(Integer progress) { }

            @Override
            public void onComplete(Integer progress) {
                cultures = new HashMap<String, HeritagePath>();
                titleView.setText(R.string.choose_culture);

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

                List<HeritagePath> uniquepaths = new ArrayList<>(cultures.size());
                if (cultures.size() < 10 ) {
                    uniquepaths.addAll(cultures.values());
                } else {
                    int count = 0;
                    for (HeritagePath path : cultures.values()) {
                        if (count<12 && path.getPercent() > 0.0009) {
                            uniquepaths.add(path);
                            LittlePerson lastInPath = path.getTreePath().get(path.getTreePath().size()-1);
                            try {
                                DataService.getInstance().addToSyncQ(lastInPath, path.getTreePath().size());
                            } catch (Exception e) {
                                Log.e(this.getClass().getName(), "Error adding person to sync Q " + lastInPath, e);
                            }
                            count++;
                        }
                    }
                }
                Collections.sort(uniquepaths);
                chartView.setHeritageMap(uniquepaths);

                if (uniquepaths.size()>0) {
                    setSelectedPath(uniquepaths.get(0));
                }
            }
        });
        waiter.execute(3000 - (System.currentTimeMillis() - starttime));
    }
	
	@Override
    public void onInit(int code) {
        super.onInit(code);

        speak(getResources().getString(R.string.calculating_heritage));

        starttime = System.currentTimeMillis();
        HeritageCalculatorTask task = new HeritageCalculatorTask(this, this);
        task.execute(person);
    }


    @Override
    public void onSelectedPath(HeritagePath path) {
        setSelectedPath(path);
    }
}
