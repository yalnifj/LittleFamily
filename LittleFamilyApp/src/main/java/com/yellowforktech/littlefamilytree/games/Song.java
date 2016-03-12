package com.yellowforktech.littlefamilytree.games;

import java.util.List;

/**
 * Created by jfinlay on 2/8/2016.
 */
public class Song {
    private int pianoTrack;
    private int drumTrack;
    private int fluteTrack;
    private int violinTrack;
    private int voiceTrack;
    private String words;

    private List<Long> danceTimings;
    private List<Long> wordTimings;
    private SongPersonAttribute attributor;

    private List<InstrumentType> instruments;

    public int getDrumTrack() {
        return drumTrack;
    }

    public void setDrumTrack(int drumTrack) {
        this.drumTrack = drumTrack;
    }

    public int getFluteTrack() {
        return fluteTrack;
    }

    public void setFluteTrack(int fluteTrack) {
        this.fluteTrack = fluteTrack;
    }

    public int getPianoTrack() {
        return pianoTrack;
    }

    public void setPianoTrack(int pianoTrack) {
        this.pianoTrack = pianoTrack;
    }

    public int getViolinTrack() {
        return violinTrack;
    }

    public void setViolinTrack(int violinTrack) {
        this.violinTrack = violinTrack;
    }

    public int getVoiceTrack() {
        return voiceTrack;
    }

    public void setVoiceTrack(int voiceTrack) {
        this.voiceTrack = voiceTrack;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public List<Long> getDanceTimings() {
        return danceTimings;
    }

    public void setDanceTimings(List<Long> danceTimings) {
        this.danceTimings = danceTimings;
    }

    public List<Long> getWordTimings() {
        return wordTimings;
    }

    public void setWordTimings(List<Long> wordTimings) {
        this.wordTimings = wordTimings;
    }

    public SongPersonAttribute getAttributor() {
        return attributor;
    }

    public void setAttributor(SongPersonAttribute attributor) {
        this.attributor = attributor;
    }

    public List<InstrumentType> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<InstrumentType> instruments) {
        this.instruments = instruments;
    }
}
