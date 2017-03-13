package com.yellowforktech.littlefamilytree.activities;

import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.LocalResource;
import com.yellowforktech.littlefamilytree.db.DBHelper;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by jfinlay on 4/15/2016.
 */
public class RecordAudioDialog extends DialogFragment implements View.OnClickListener {
    private LittlePerson person;
    private LocalResource localResource;
    private View view;

    private TextView personNameLabel;
    private TextView audioTypeLabel;
    private ImageButton playButton;
    private ImageButton recordButton;
    private ImageButton deleteButton;
    private Button closeButton;

    private boolean playing = false;
    private boolean recording = false;

    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;

    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO"};

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        person = (LittlePerson) args.getSerializable("person");

        try {
            List<LocalResource> resources = DataService.getInstance().getDBHelper().getLocalResourcesForPerson(person.getId());
            for(LocalResource r : resources) {
                if (r.getType().equals(DBHelper.TYPE_GIVEN_AUDIO)) {
                    localResource = r;
                }
            }
        } catch (Exception e) {
            Log.e("RecordAudioDialog", "Error getting local resources.", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_audio_recorder, container, false);

        personNameLabel = (TextView) view.findViewById(R.id.personNameLbl);
        personNameLabel.setText(person.getName());
        audioTypeLabel = (TextView) view.findViewById(R.id.audioTypeLbl);
        audioTypeLabel.setText("Given Name Audio");

        playButton = (ImageButton) view.findViewById(R.id.playAudioButton);
        playButton.setOnClickListener(this);
        recordButton = (ImageButton) view.findViewById(R.id.recordAudioButton);
        recordButton.setOnClickListener(this);
        deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);

        closeButton = (Button) view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        setButtonStates();

        Bundle logBundle = new Bundle();
        logBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, this.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(getActivity()).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, logBundle);

        // Add the following code to your onCreate
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) this.dismissAllowingStateLoss();

    }

    public void setButtonStates() {
        if (person.getGivenNameAudioPath() == null) {
            playButton.setEnabled(false);
            deleteButton.setVisibility(View.GONE);
        } else {
            playButton.setEnabled(true);
            deleteButton.setVisibility(View.VISIBLE);
        }
        recordButton.setImageResource(android.R.drawable.presence_audio_busy);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.playAudioButton:
                playAudio();
                break;
            case R.id.recordAudioButton:
                recordAudio();
                break;
            case R.id.deleteButton:
                deleteRecording();
                break;
            case R.id.closeButton:
                this.dismissAllowingStateLoss();
                break;
        }
    }

    public void playAudio() {
        if (mPlayer!=null && mPlayer.isPlaying()) {
            mPlayer.pause();
            playButton.setImageResource(android.R.drawable.ic_media_play);
        }
        else if (mPlayer!=null) {
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            mPlayer.start();
        } else {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(person.getGivenNameAudioPath());
                mPlayer.setVolume(3.0f, 3.0f);
                mPlayer.prepare();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayer.release();
                        mPlayer = null;
                        playing = false;
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                    }
                });
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                playing = true;
                mPlayer.start();
            } catch (IOException e) {
                Log.e("RecordAudioDialog", "prepare() failed", e);
            }
        }
    }

    public void recordAudio() {
        if (!recording) {
            if (localResource==null) {
                localResource = new LocalResource();
            }
            localResource.setPersonId(person.getId());
            File dataFolder = ImageHelper.getDataFolder(getActivity());
            File personFolder = new File(dataFolder, person.getFamilySearchId());
            if (!personFolder.exists()) {
                personFolder.mkdirs();
            }
            File audioFile = new File(personFolder, "given.3gp");
            localResource.setLocalPath(audioFile.getAbsolutePath());
            person.setGivenNameAudioPath(audioFile.getAbsolutePath());
            localResource.setType(DBHelper.TYPE_GIVEN_AUDIO);


            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(localResource.getLocalPath());
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            mRecorder.setAudioSamplingRate(48000);
            mRecorder.setMaxDuration(2500);
            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                        mRecorder.release();
                        mRecorder = null;
                        recording = false;
                        setButtonStates();
                        try {
                            DataService.getInstance().getDBHelper().persistLocalResource(localResource);
                        } catch (Exception e) {
                            Log.e("RecordAudioDialog", "failed to save local resource", e);
                        }
                    }
                }
            });

            try {
                mRecorder.prepare();

                playButton.setEnabled(false);
                deleteButton.setVisibility(View.GONE);
                recordButton.setImageResource(android.R.drawable.ic_media_pause);

                recording = true;
                mRecorder.start();
            } catch (IOException e) {
                Log.e("RecordAudioDialog", "prepare() failed", e);
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
            }

        } else {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            recording = false;
            setButtonStates();
            try {
                DataService.getInstance().getDBHelper().persistLocalResource(localResource);
            } catch (Exception e) {
                Log.e("RecordAudioDialog", "failed to save local resource", e);
            }
        }
    }

    public void deleteRecording() {
        try {
            DataService.getInstance().getDBHelper().deleteLocalResourceById(localResource.getId());
            person.setGivenNameAudioPath(null);
            File file = new File(localResource.getLocalPath());
            file.delete();
            localResource = null;
            setButtonStates();
        } catch (Exception e) {
            Log.e("RecordAudioDialog", "Error deleting local resource", e);
        }
    }
}
