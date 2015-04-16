package org.finlayfamily.littlefamily.games;

/**
 * Created by jfinlay on 4/16/2015.
 */
public class DollConfig {
    private String folderName;
    private String boygirl;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getBoygirl() {
        return boygirl;
    }

    public void setBoygirl(String boygirl) {
        this.boygirl = boygirl;
    }

    public String getThumbnail() {
        return "dolls/"+folderName+"/"+boygirl+"_thumb.png";
    }

    public class DollClothing {
        private int top;
        private int left;
        private String filename;
    }
}
