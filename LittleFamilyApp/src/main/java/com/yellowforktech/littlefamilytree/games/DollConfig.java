package com.yellowforktech.littlefamilytree.games;

import android.content.Context;
import android.util.Log;

import com.yellowforktech.littlefamilytree.data.DollClothing;

import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

/**
 * Created by jfinlay on 4/16/2015.
 */
public class DollConfig implements Serializable{
    private String folderName;
    private String boygirl;
    private String skinTone;
    private String originalPlace;
    private DollClothing[] boyclothing;
    private DollClothing[] girlclothing;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
        boyclothing = null;
        girlclothing = null;
    }

    public String getBoygirl() {
        return boygirl;
    }

    public void setBoygirl(String boygirl) {
        this.boygirl = boygirl;
        boyclothing = null;
        girlclothing = null;
    }

    public String getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(String skinTone) {
        this.skinTone = skinTone;
    }

    public String getOriginalPlace() {
        return originalPlace;
    }

    public void setOriginalPlace(String originalPlace) {
        String[] parts = originalPlace.split(" +");
        this.originalPlace = "";
        for(String w : parts) {
            if (!w.isEmpty()) {
                this.originalPlace += w.substring(0, 1).toUpperCase() + w.substring(1) + " ";
            }
        }
        this.originalPlace = this.originalPlace.trim();
    }

    public String getThumbnail() {
        return "dolls/"+folderName+"/"+boygirl+"_thumb.png";
    }

    public String getDoll() {
        if (skinTone==null || skinTone.isEmpty() || skinTone.equals("light")) {
            return "dolls/"+boygirl+".png";
        } else {
            return "dolls/"+boygirl+"_"+skinTone+".png";
        }
    }

    public DollClothing[] getClothing(Context context) {
        if (boyclothing==null) {
            try {
                Scanner in = new Scanner(context.getAssets().open("dolls/"+folderName+"/clothing.dat"));
                int boycount = in.nextInt();
                boyclothing = new DollClothing[boycount];
                for(int b=0; b<boycount; b++) {
                    String clothingName = in.next();
                    int left = in.nextInt();
                    int top = in.nextInt();
                    DollClothing dc = new DollClothing(left, top, "dolls/"+folderName+"/"+clothingName+".png");
                    boyclothing[b] = dc;
                }
                int girlcount = in.nextInt();
                girlclothing = new DollClothing[girlcount];
                for(int b=0; b<girlcount; b++) {
                    String clothingName = in.next();
                    int left = in.nextInt();
                    int top = in.nextInt();
                    DollClothing dc = new DollClothing(left, top, "dolls/"+folderName+"/"+clothingName+".png");
                    girlclothing[b] = dc;
                }
                in.close();
            } catch (IOException e) {
                Log.e("DollConfig", "Error opening clothing.dat", e);
            }
        }
        if ("boy".equals(boygirl)) {
            return boyclothing;
        } else {
            return girlclothing;
        }
    }
}
