package com.yellowforktech.littlefamilytree.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.adapters.PersonPagerAdapter;
import com.yellowforktech.littlefamilytree.activities.tasks.HeritageCalculatorTask;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.HeritagePath;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.games.DressUpDolls;
import com.yellowforktech.littlefamilytree.util.RelationshipCalculator;
import com.yellowforktech.littlefamilytree.views.PagerContainer;
import com.yellowforktech.littlefamilytree.views.PersonHeritageChartView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseCultureActivity extends LittleFamilyBillingActivity implements HeritageCalculatorTask.Listener, PersonHeritageChartView.SelectedPathListener {
    public static final String DOLL_CONFIG = "dollConfig";
    private LittlePerson person;
    private Map<String, HeritagePath> cultures;
    private Map<String, List<LittlePerson>> culturePeople;
    private PersonHeritageChartView chartView;
    private TextView titleView;
    private TextView cultureNameView;
    private ImageView dollImage;
    private Button playButton;
    private HeritagePath selectedPath;
    private DressUpDolls dressUpDolls;
    private long starttime;
    private PagerContainer personContainer;
    private PersonPagerAdapter adapter;
    private MyPageTransformer transformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_culture);

        chartView = (PersonHeritageChartView) findViewById(R.id.personChart);
        titleView = (TextView) findViewById(R.id.titleText);

        cultureNameView = (TextView) findViewById(R.id.cultureNameTextView);
        cultureNameView.setText("");

        dollImage = (ImageView) findViewById(R.id.dollImage);
        dollImage.setVisibility(View.INVISIBLE);

        personContainer = (PagerContainer) findViewById(R.id.personPagerContainer);
        ViewPager pager = personContainer.getViewPager();
        adapter = new PersonPagerAdapter(this);
        pager.setAdapter(adapter);
        //Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        pager.setOffscreenPageLimit(3);
        //A little space between pages
        pager.setPageMargin(0);

        //If hardware acceleration is enabled, you should also remove
        // clipping on the pager for its children.
        pager.setClipChildren(false);
        transformer = new MyPageTransformer();
        pager.setPageTransformer(true, transformer);

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

    public void personTapped(View view) {
        LittlePerson relative = (LittlePerson) view.getTag();
        speakDetails(relative);
    }

    public void speakDetails(LittlePerson relative) {
        String relationship = RelationshipCalculator.getAncestralRelationship(selectedPath.getTreePath().size(), relative, person,
                false, false, false, this);

        relative.setRelationship(relationship);
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
    }

    public void setSelectedPath(HeritagePath selectedPath) {
        this.selectedPath = selectedPath;
        LittlePerson relative = selectedPath.getTreePath().get(selectedPath.getTreePath().size() - 1);
		speakDetails(relative);

        List<LittlePerson> people = culturePeople.get(selectedPath.getPlace());
        transformer.setPageWidth(personContainer.getViewPager().getWidth());
        adapter.setPeople(people);
        personContainer.getViewPager().invalidate();
        personContainer.getViewPager().setCurrentItem(0);
        personContainer.invalidate();

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
                cultures = new HashMap<>();
                culturePeople = new HashMap<>();
                titleView.setText(R.string.choose_culture);

                double totalLevel = 0;
                double countLevel = 0;
                for(HeritagePath path : paths) {
                    String place = path.getPlace();
                    if (cultures.get(place)==null) {
                        cultures.put(place, path);
                        List<LittlePerson> pl = new ArrayList<>();
                        LittlePerson ancestor = path.getTreePath().get(path.getTreePath().size() - 1);
                        if (ancestor.getTreeLevel()!=null && ancestor.isHasParents()==null) {
                            totalLevel += ancestor.getTreeLevel();
                            countLevel++;
                        }
                        pl.add(ancestor);
                        culturePeople.put(place, pl);
                    } else {
                        Double percent = cultures.get(place).getPercent() + path.getPercent();
                        if (cultures.get(place).getTreePath().size() <= path.getTreePath().size()) {
                            cultures.get(place).setPercent(percent);
                            LittlePerson ancestor = path.getTreePath().get(path.getTreePath().size()-1);
                            if (ancestor.getTreeLevel()!=null && ancestor.isHasParents()==null) {
                                totalLevel += ancestor.getTreeLevel();
                                countLevel++;
                            }
                            culturePeople.get(place).add(ancestor);
                        } else {
                            path.setPercent(percent);
                            cultures.put(place, path);
                            LittlePerson ancestor = path.getTreePath().get(path.getTreePath().size()-1);
                            if (ancestor.getTreeLevel()!=null && ancestor.isHasParents()==null) {
                                totalLevel += ancestor.getTreeLevel();
                                countLevel++;
                            }
                            culturePeople.get(place).add(0, ancestor);
                        }
                    }
                }

                List<HeritagePath> uniquepaths = new ArrayList<>(cultures.size());
                uniquepaths.addAll(cultures.values());
                Collections.sort(uniquepaths);
                if (uniquepaths.size()>13) {
                    uniquepaths = uniquepaths.subList(0, 13);
                }
                for (HeritagePath path : uniquepaths) {
                    LittlePerson lastInPath = path.getTreePath().get(path.getTreePath().size()-1);
                    try {
                        DataService.getInstance().addToSyncQ(lastInPath, path.getTreePath().size());
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), "Error adding person to sync Q " + lastInPath, e);
                    }
                }

                chartView.setHeritageMap(uniquepaths);

                if (uniquepaths.size()>0) {
                    setSelectedPath(uniquepaths.get(0));
                }

                if (countLevel > 0 && cultures.size() < 3 && totalLevel / countLevel < 6) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChooseCultureActivity.this);
                    builder.setMessage(R.string.low_tree_level);
                    builder.setPositiveButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        waiter.execute(3000 - (System.currentTimeMillis() - starttime));

        checkPremium("heritage_calc");
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

    public class MyPageTransformer implements ViewPager.PageTransformer {
        private float pageWidth;

        public float getPageWidth() {
            return pageWidth;
        }

        public void setPageWidth(float pageWidth) {
            this.pageWidth = pageWidth;
        }

        @Override
        public void transformPage(View page, float position) {
            if (position < -2 || position > 2) {
                page.setAlpha(0);
            } else {
                page.setAlpha(1.0f - (0.45f * Math.abs(position)));
                page.setRotationY(position * 40);
                page.setScaleX(1.0f - (0.30f * Math.abs(position)));
                page.setScaleY(1.0f - (0.30f * Math.abs(position)));
                if (pageWidth>0) {
                    page.setTranslationX(pageWidth / 4 + (-1 * position * Math.abs(position) * pageWidth / 2.5f));
                }
            }
        }
    }
}
