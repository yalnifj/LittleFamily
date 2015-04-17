package org.finlayfamily.littlefamily.games;

import android.content.Context;
import android.util.Log;

import org.finlayfamily.littlefamily.data.DollClothing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Scanner;

/**
 * Created by jfinlay on 4/16/2015.
 */
public class DollConfig implements Serializable{
    private String folderName;
    private String boygirl;
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

    public String getThumbnail() {
        return "dolls/"+folderName+"/"+boygirl+"_thumb.png";
    }

    public String getDoll() {
        return "dolls/"+boygirl+".png";
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
                    DollClothing dc = new DollClothing(left, top, clothingName);
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
