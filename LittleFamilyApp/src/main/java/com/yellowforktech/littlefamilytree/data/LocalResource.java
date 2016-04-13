package com.yellowforktech.littlefamilytree.data;

/**
 * Created by jfinlay on 2/19/2015.
 */
public class LocalResource {
    private int id;
    private int personId;
    private String localPath;
    private String type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
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

        LocalResource that = (LocalResource) o;

        if (personId != that.personId) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        return id;
    }
}
