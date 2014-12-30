package org.finlayfamily.littlefamily.data;

import org.gedcomx.conclusion.Gender;
import org.gedcomx.conclusion.Person;
import org.gedcomx.types.GenderType;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class LittlePerson {
    private long id;
    private String name;
    private String relationship;
    private String familySearchId;
    private String photoPath;
    private GenderType gender;

    public LittlePerson() {
    }

    public LittlePerson(Person fsPerson) {
        setName(fsPerson.getFullName());
        setFamilySearchId(fsPerson.getId());
        setGender(fsPerson.getGender().getKnownType());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getFamilySearchId() {
        return familySearchId;
    }

    public void setFamilySearchId(String familySearchId) {
        this.familySearchId = familySearchId;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }
}
