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

    private List<Long> timings;

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

    public List<Long> getTimings() {
        return timings;
    }

    public void setTimings(List<Long> timings) {
        this.timings = timings;
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
}
