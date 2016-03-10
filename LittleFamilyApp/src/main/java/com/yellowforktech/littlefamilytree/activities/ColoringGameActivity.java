package com.yellowforktech.littlefamilytree.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.tasks.WaitTask;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Media;
import com.yellowforktech.littlefamilytree.games.RandomMediaChooser;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.views.BrushSizeView;
import com.yellowforktech.littlefamilytree.views.ColoringView;
import com.yellowforktech.littlefamilytree.views.WaterColorImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ColoringGameActivity extends LittleFamilyActivity implements RandomMediaChooser.RandomMediaListener, ColoringView.ColoringCompleteListener, SeekBar.OnSeekBarChangeListener {
    public static int maxBrushSize = 50;
    public static int minBrushSize = 15;

    private List<LittlePerson> people;
    private LittlePerson selectedPerson;

    private ColoringView layeredImage;
    private WaterColorImageView colorPicker;
    private String imagePath;
    private Bitmap imageBitmap;
    private Media photo;
    private RandomMediaChooser mediaChooser;
	private ShareAction shareAction = new ShareAction();
    private SeekBar seekBar;
    private BrushSizeView brushSizeView;
    private ImageButton toggleOutlineBtn;

    private boolean showOutline = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coloring_game);

        layeredImage = (ColoringView) findViewById(R.id.layeredImage);
        layeredImage.registerListener(this);
		layeredImage.setZOrderOnTop(true);    // necessary
		SurfaceHolder sfhTrackHolder = layeredImage.getHolder();
		sfhTrackHolder.setFormat(PixelFormat.TRANSPARENT);
		

        colorPicker = (WaterColorImageView) findViewById(R.id.colorPicker);
        colorPicker.setActivity(this);
        colorPicker.registerListener(layeredImage);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
		maxBrushSize = Math.min(size.x, size.y) / 8;

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(maxBrushSize);

        brushSizeView = (BrushSizeView) findViewById(R.id.brushSize);
        brushSizeView.setMaxSize(maxBrushSize);
        brushSizeView.setMinSize(minBrushSize);
        brushSizeView.setBrushSize(maxBrushSize / 2);
        seekBar.setProgress(maxBrushSize / 2);

        toggleOutlineBtn = (ImageButton) findViewById(R.id.outlineBtn);

        Intent intent = getIntent();
        people = (List<LittlePerson>) intent.getSerializableExtra(ChooseFamilyMember.FAMILY);
        selectedPerson = (LittlePerson) intent.getSerializableExtra(ChooseFamilyMember.SELECTED_PERSON);

        if (people==null) {
            people = new ArrayList<>();
            people.add(selectedPerson);
        }

        mediaChooser = RandomMediaChooser.getInstance();
		mediaChooser.setActivity(this);
		mediaChooser.setListener(this);
		mediaChooser.addPeople(people);

        setupTopBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
		Bitmap starBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star1);
		layeredImage.setStarBitmap(starBitmap);
        layeredImage.setBrushSize(maxBrushSize/2);
        DataService.getInstance().registerNetworkStateListener(this);

        showLoadingDialog();
        if (people.size()<2) {
            mediaChooser.loadMoreFamilyMembers();
        } else {
            mediaChooser.loadRandomImage();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        DataService.getInstance().unregisterNetworkStateListener(this);
    }

    @Override
    public void setupTopBar() {
        if (findViewById(R.id.topBarFragment)!=null) {
            topBar = (TopBarFragment) getSupportFragmentManager().findFragmentById(R.id.topBarFragment);
            if (topBar == null) {
                topBar = TopBarFragment.newInstance(selectedPerson, R.layout.fragment_top_bar_coloring);
                getSupportFragmentManager().beginTransaction().replace(R.id.topBarFragment, topBar).commit();
            } else {
                if (selectedPerson != null) {
                    topBar.getArguments().putSerializable(TopBarFragment.ARG_PERSON, selectedPerson);
                }
            }
        }
    }

    public void nextImage(View view) {
        showLoadingDialog();
        mediaChooser.loadRandomImage();
    }

    public void shareImage(View view) {
        showAdultAuthDialog(shareAction);
    }

    public void setupCanvas() {
        showLoadingDialog();
        layeredImage.setImageBitmap(imageBitmap);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (brushSizeView!=null) {
            brushSizeView.setBrushSize(progress);
            if (layeredImage!=null) layeredImage.setBrushSize(brushSizeView.getBrushSize());
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onColoringComplete() {
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
            }
        });
        waiter.execute(3000L);
    }

    public void toggleOutline(View view) {
        showOutline = !showOutline;
        layeredImage.setShowOutline(showOutline);
        if (showOutline) {
            toggleOutlineBtn.setImageResource(R.drawable.grandma_outline);
        } else {
            toggleOutlineBtn.setImageResource(R.drawable.grandma);
        }
    }

    @Override
    public void onColoringReady() {
        hideLoadingDialog();
    }

    @Override
    public void onMediaLoaded(Media media) {
        photo = media;
        if (!showOutline) toggleOutline(toggleOutlineBtn);
        selectedPerson = mediaChooser.getSelectedPerson();
        int width = layeredImage.getWidth() / 2;
        int height = layeredImage.getHeight() / 2;
        if (width < 5) width = getScreenWidth() / 2;
        if (height < 5) height = getScreenHeight() / 2 - 25;
        if (imageBitmap != null && !imageBitmap.isRecycled()) {
            imageBitmap.recycle();
        }
        if (photo==null) {
            //-- could not find any images, fallback to a default image
            imageBitmap = ImageHelper.loadBitmapFromResource(this, selectedPerson.getDefaultPhotoResource(), 0, width, height);
            setupCanvas();
        } else {
            imagePath = photo.getLocalPath();
            if (imagePath != null) {
                imageBitmap = ImageHelper.loadBitmapFromFile(imagePath, 0, width, height, true);
                if (imageBitmap != null) {
                    setupCanvas();
                } else {
                    mediaChooser.loadRandomImage();
                }
            } else {
                mediaChooser.loadRandomImage();
            }
        }
    }
	
	public class ShareAction implements AdultsAuthDialog.AuthCompleteAction {
		public void doAction(boolean success) {
			if (success) {
				Bitmap sharing = layeredImage.getSharingBitmap();
				File dir = ImageHelper.getDataFolder(ColoringGameActivity.this);
				File file = new File(dir, "tempImage.jpg");
				if (file.exists()) {
					file.delete();
				}
				try {
					FileOutputStream out = new FileOutputStream(file);
					sharing.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
					out.close();
				} catch (Exception e) {
					Log.e(this.getClass().getName(), "Error sharing file", e);
					Toast.makeText(ColoringGameActivity.this, "Unable to share image "+e, Toast.LENGTH_LONG).show();
					return;
				}
				Uri screenshotUri = Uri.fromFile(file);
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("image/*");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
				startActivity(Intent.createChooser(sharingIntent, "Share image using"));
			} else {
				Toast.makeText(ColoringGameActivity.this, "Unable to verify password", Toast.LENGTH_LONG).show();
			}
            hideAdultAuthDialog();
		}
	}
}
