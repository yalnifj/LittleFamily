package org.finlayfamily.littlefamily.data;

/**
 * Created by jfinlay on 2/19/2015.
 */
public class Media {
    private int id;
    private String familySearchId;
    private String localPath;
    private String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFamilySearchId() {
        return familySearchId;
    }

    public void setFamilySearchId(String familySearchId) {
        this.familySearchId = familySearchId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Media media = (Media) o;

        if (id != media.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
