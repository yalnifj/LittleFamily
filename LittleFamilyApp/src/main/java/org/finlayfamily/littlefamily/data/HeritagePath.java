package org.finlayfamily.littlefamily.data;

import java.util.List;

/**
 * Created by Parents on 3/24/2015.
 */
public class HeritagePath {
    private List<LittlePerson> treePath;
    private Double percent;
    private String place;

    public List<LittlePerson> getTreePath() {
        return treePath;
    }

    public void setTreePath(List<LittlePerson> treePath) {
        this.treePath = treePath;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}
