package org.finlayfamily.littlefamily.activities;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.speech.tts.*;
import android.widget.*;
import java.util.*;
import org.finlayfamily.littlefamily.*;
import org.finlayfamily.littlefamily.activities.tasks.*;
import org.finlayfamily.littlefamily.data.*;
import org.finlayfamily.littlefamily.util.*;
import org.finlayfamily.littlefamily.views.*;
import org.gedcomx.types.*;

public class ChooseCultureActivity extends Activity implements HeritageCalculatorTask.Listener, TextToSpeech.OnInitListener {

    private LittlePerson person;
    private Map<String, HeritagePath> cultures;
    private PersonHeritageChartView chartView;
    private TextView titleView;
    private TextView personNameView;
    private TextView cultureNameView;
    private ImageView portraitImage;
    private ImageView dollImage;
    private HeritagePath selectedPath;
	
	private TextToSpeech tts;

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
		
		tts = new TextToSpeech(this, this);

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
		
		String relationship = "";
		int greats = selectedPath.getTreePath().size() - 2;
		for(int g=0; g<greats; g++) {
			relationship = relationship + getResources().getString(R.string.great)+", ";
		}
		if (selectedPath.getTreePath().size()>1) {
			relationship = relationship + getResources().getString(R.string.grand)+", ";
		}
		if (relative.getGender()==GenderType.Female) {
			getResources().getString(R.string.mother);
		} else {
			getResources().getString(R.string.father);
		}

        this.cultureNameView.setText(selectedPath.getPlace());
		String text = String.format(getResources().getString(R.string.you_are_percent),
				selectedPath.getPercent()*100, selectedPath.getPlace(),
				relationship, relative.getName());
		speak(text);
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
        if (code == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            tts.setSpeechRate(0.5f);
			speak(getResources().getString(R.string.calculating_heritage));
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
	
	private void speak(String message) {
		if (tts!=null) {
			if (Build.VERSION.SDK_INT > 20) {
				tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
			}
			else {
				tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}
}
