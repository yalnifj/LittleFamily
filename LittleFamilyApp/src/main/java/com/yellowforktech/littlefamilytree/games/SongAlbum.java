package com.yellowforktech.littlefamilytree.games;

import android.content.Context;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Parents on 2/15/2016.
 */
public class SongAlbum {
    private List<Song> songs;
    private int currentSong = 0;
    private LittlePerson selectedPerson;
    private Context context;

    public SongAlbum(LittlePerson person, Context context) {
        songs = new ArrayList<>();
        this.selectedPerson = person;
        this.context = context;

        addFamilyTreeSong();
        addMyHistorySong();
        addThisIsMyHistorySong();
    }

    public Song nextSong() {
        Song song = songs.get(currentSong);
        currentSong++;
        if (currentSong >= songs.size()) {
            currentSong = 0;
        }
        return song;
    }

    private void addFamilyTreeSong() {
        Song song = new Song();
        song.setDrumTrack(R.raw.drums_allinourfamilytree);
        song.setFluteTrack(R.raw.flute_allinourfamilytree);
        song.setPianoTrack(R.raw.piano_allinourfamilytree);
        song.setViolinTrack(R.raw.violin_allinourfamilytree);
        song.setVoiceTrack(R.raw.voice_allinourfamilytree);
        song.setWords("We are a fam -i -ly. We are a fam -i -ly. We have _.  We have _. We have _. We have _. They're all in our fami -ly tree. They're all in our fami -ly tree.");
        List<Long> wordTimings = new ArrayList<>();
        wordTimings.add(1500L);//are
        wordTimings.add(1700L);//a
        wordTimings.add(2200L);//fam
        wordTimings.add(2500L);//i
        wordTimings.add(2900L);//ly
        wordTimings.add(3330L);//we
        wordTimings.add(4200L);//are
        wordTimings.add(4900L);//a
        wordTimings.add(5100L);//fam
        wordTimings.add(5400L);//i
        wordTimings.add(5600L);//ly
        wordTimings.add(7000L);//we
        wordTimings.add(7400L);//have
        wordTimings.add(7900L);//_
        wordTimings.add(9000L);//we
        wordTimings.add(9600L);//have
        wordTimings.add(10080L);//_
        wordTimings.add(11100L);//we
        wordTimings.add(11900L);//have
        wordTimings.add(12100L);//_
        wordTimings.add(13400L);//we
        wordTimings.add(13800L);//have
        wordTimings.add(14300L);//_
        wordTimings.add(15800L);//Theyre
        wordTimings.add(16100L);//all
        wordTimings.add(16600L);//in
        wordTimings.add(16900L);//our
        wordTimings.add(17200L);//fam
        wordTimings.add(18000L);//ly
        wordTimings.add(18500L);//tree
        wordTimings.add(19300L);//theyre
        wordTimings.add(19700L);//all
        wordTimings.add(20200L);//in
        wordTimings.add(20800L);//our
        wordTimings.add(21100L);//fam
        wordTimings.add(21500L);//ly
        wordTimings.add(22000L);//tree
        wordTimings.add(23500L);
        wordTimings.add(24500L);
        song.setWordTimings(wordTimings);

        List<Long> timings = new ArrayList<>(12);
        timings.add(6900L);
        timings.add(9000L);
        timings.add(11000L);
        timings.add(13200L);
        timings.add(15400L);
        timings.add(23000L);
        timings.add(24000L);

        song.setDanceTimings(timings);

        song.setAttributor(new SongNameAttributor());

        List<InstrumentType> instruments = new ArrayList<>(4);
        instruments.add(InstrumentType.Drums);
        instruments.add(InstrumentType.Flute);
        instruments.add(InstrumentType.Violin);
        instruments.add(InstrumentType.Piano);
        song.setInstruments(instruments);

        songs.add(song);
    }

    private void addMyHistorySong() {
        Song song = new Song();
        song.setDrumTrack(R.raw.drums_myhistory);
        song.setFluteTrack(R.raw.flute_myhistory);
        song.setPianoTrack(R.raw.piano_myhistory);
        song.setViolinTrack(R.raw.violin_myhistory);
        song.setVoiceTrack(R.raw.voice_myhistory);
        song.setWords("Fami -ly his -tor -y, is my his -tor -y. My an -cest -or was born in _. This rel -a -tive lived in _. My an -cest -or was born in _. This rel -a -tive lived in _. That's my his -tor -y.");
        List<Long> wordTimings = new ArrayList<>();
        wordTimings.add(500L);//-ly
        wordTimings.add(1200L);//his
        wordTimings.add(1800L);//-tor
        wordTimings.add(2300L);//-y
        wordTimings.add(2500L);//is
        wordTimings.add(3100L);//my
        wordTimings.add(3700L);//his
        wordTimings.add(4500L);//-tor
        wordTimings.add(4900L);//-ry
        wordTimings.add(5200L);//my
        wordTimings.add(5800L);//an
        wordTimings.add(6400L);//-cest
        wordTimings.add(6700L);//-or
        wordTimings.add(6900L);//was
        wordTimings.add(7300L);//born
        wordTimings.add(7700L);//in
        wordTimings.add(8300L);//_
        wordTimings.add(10800L);//this
        wordTimings.add(11200L);//rel
        wordTimings.add(11700L);//-a
        wordTimings.add(12300L);//-tive
        wordTimings.add(12800L);//lived
        wordTimings.add(13300L);//in
        wordTimings.add(13500L);//_
        wordTimings.add(14800L);//My
        wordTimings.add(15100L);//An
        wordTimings.add(15700L);//-cest
        wordTimings.add(16000L);//-or
        wordTimings.add(16300L);//was
        wordTimings.add(16500L);//born
        wordTimings.add(16900L);//in
        wordTimings.add(17300L);//_
        wordTimings.add(19500L);//this
        wordTimings.add(20000L);//rel
        wordTimings.add(20500L);//-a
        wordTimings.add(20900L);//-tive
        wordTimings.add(21400L);//lived
        wordTimings.add(21900L);//in
        wordTimings.add(22100L);//_
        wordTimings.add(23700L);//thats
        wordTimings.add(24200L);//my
        wordTimings.add(24800L);//his
        wordTimings.add(25400L);//-tor
        wordTimings.add(26000L);//-y
        wordTimings.add(27000L);

        song.setWordTimings(wordTimings);

        List<Long> timings = new ArrayList<>(12);
        timings.add(6000L);
        timings.add(10000L);
        timings.add(14600L);
        timings.add(19000L);
        timings.add(23000L);
        timings.add(27000L);
        timings.add(28000L);

        song.setDanceTimings(timings);

        song.setAttributor(new SongDatePlaceAttributor());

        List<InstrumentType> instruments = new ArrayList<>(4);
        instruments.add(InstrumentType.Drums);
        instruments.add(InstrumentType.Bass);
        instruments.add(InstrumentType.Violin);
        instruments.add(InstrumentType.Piano);
        song.setInstruments(instruments);

        songs.add(song);
    }

    private void addThisIsMyHistorySong() {
        Song song = new Song();
        song.setDrumTrack(R.raw.drums_thisismyfamily);
        song.setFluteTrack(R.raw.flute_thisismyfamily);
        song.setPianoTrack(R.raw.piano_thisismyfamily);
        song.setViolinTrack(R.raw.guitar_thisismyfamily);
        song.setVoiceTrack(R.raw.voice_thisismyfamily);
        song.setWords("This is my fam -i -ly. They mean so much to me. Here is my _. My _ is here too. Here is my _. My _ is here too. This is my fam -i -ly.");
        List<Long> wordTimings = new ArrayList<>();
        //wordTimings.add(100L);//this
        //wordTimings.add(300L);//this
        wordTimings.add(700L);//is
        wordTimings.add(1170L);//my
        wordTimings.add(1750L);//fam
        wordTimings.add(2360L);//i
        wordTimings.add(2980L);//ly
        wordTimings.add(4400L);//they
        wordTimings.add(5000L);//mean
        wordTimings.add(5450L);//so
        wordTimings.add(6000L);//much
        wordTimings.add(6600L);//to
        wordTimings.add(7100L);//me
        wordTimings.add(8700L);//here
        wordTimings.add(9270L);//is
        wordTimings.add(9770L);//my
        wordTimings.add(10400L);//_
        wordTimings.add(13100L);//my
        wordTimings.add(13800L);//_
        wordTimings.add(15200L);//is
        wordTimings.add(15800L);//here
        wordTimings.add(16300L);//too.

        wordTimings.add(17900L);//here
        wordTimings.add(18400L);//is
        wordTimings.add(18900L);//my
        wordTimings.add(19600L);//_
        wordTimings.add(22300L);//my
        wordTimings.add(22600L);//_
        wordTimings.add(24400L);//is
        wordTimings.add(24950L);//here
        wordTimings.add(25500L);//too.

        wordTimings.add(27170L);//this
        wordTimings.add(27760L);//is
        wordTimings.add(28380L);//my
        wordTimings.add(28770L);//fam
        wordTimings.add(29500L);//i
        wordTimings.add(30070L);//ly
        wordTimings.add(31300L);//

        song.setWordTimings(wordTimings);

        List<Long> timings = new ArrayList<>(12);
        timings.add(8700L);
        timings.add(13300L);
        timings.add(18300L);
        timings.add(22300L);
        timings.add(27500L);
        timings.add(31500L);
        timings.add(33000L);

        song.setDanceTimings(timings);

        song.setAttributor(new SongRelationshipAttributor(selectedPerson, context));

        List<InstrumentType> instruments = new ArrayList<>(4);
        instruments.add(InstrumentType.Drums);
        instruments.add(InstrumentType.Flute);
        instruments.add(InstrumentType.Guitar);
        instruments.add(InstrumentType.Piano);
        song.setInstruments(instruments);

        songs.add(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

}
