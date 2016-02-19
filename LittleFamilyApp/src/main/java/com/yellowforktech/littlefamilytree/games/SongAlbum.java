package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Parents on 2/15/2016.
 */
public class SongAlbum {
    private List<Song> songs;

    public SongAlbum() {
        songs = new ArrayList<>();

        Song song = new Song();
        song.setDrumTrack(R.raw.drums_allinourfamilytree);
        song.setFluteTrack(R.raw.flute_allinourfamilytree);
        song.setPianoTrack(R.raw.piano_allinourfamilytree);
        song.setViolinTrack(R.raw.violin_allinourfamilytree);
        song.setWords("We are a fam -i -ly. We are a fam -i -ly. We have _.  We have _. We have _. We have _. They're all in our fami -ly tree. They're all in our fam -i -ly.");
        List<Long> wordTimings = new ArrayList<>();
        wordTimings.add(500L);
        wordTimings.add(1170L);
        wordTimings.add(1780L);
        wordTimings.add(2000L);
        wordTimings.add(2400L);
        wordTimings.add(2830L);
        wordTimings.add(3640L);
        wordTimings.add(4400L);
        wordTimings.add(5230L);
        wordTimings.add(5670L);
        wordTimings.add(6100L);
        wordTimings.add(6900L);
        wordTimings.add(7200L);
        wordTimings.add(7600L);
        wordTimings.add(8100L);
        wordTimings.add(9080L);
        wordTimings.add(9380L);
        wordTimings.add(9800L);
        wordTimings.add(10200L);
        wordTimings.add(11200L);
        wordTimings.add(11500L);
        wordTimings.add(11900L);
        wordTimings.add(12300L);
        wordTimings.add(13300L);
        wordTimings.add(13600L);
        wordTimings.add(13900L);
        wordTimings.add(14400L);
        wordTimings.add(15600L);
        wordTimings.add(16000L);
        wordTimings.add(16400L);
        wordTimings.add(17200L);
        wordTimings.add(17500L);
        wordTimings.add(17900L);
        wordTimings.add(18300L);
        wordTimings.add(19200L);
        wordTimings.add(19600L);
        wordTimings.add(20000L);
        wordTimings.add(20800L);
        wordTimings.add(21100L);
        wordTimings.add(21500L);
        wordTimings.add(22000L);
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

        songs.add(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
