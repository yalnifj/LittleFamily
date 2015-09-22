package com.yellowforktech.littlefamilytree.data;

import com.yellowforktech.littlefamilytree.util.PlaceHelper;

import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NameForm;
import org.gedcomx.conclusion.NamePart;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.PlaceReference;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jfinlay on 12/30/2014.
 */
public class LittlePerson implements Serializable {
    private int id;
    private String name;
    private String givenName;
    private String relationship;
    private String familySearchId;
    private String photoPath;
    private GenderType gender;
	private Date birthDate;
    private Boolean hasParents;
    private Boolean hasChildren;
    private Boolean hasSpouses;
    private Boolean hasMedia;

    private Integer treeLevel;

    @Override
    public String toString() {
        return "LittlePerson{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", familySearchId='" + familySearchId + '\'' +
                ", birthDate=" + birthDate +
                ", birthPlace='" + birthPlace + '\'' +
                ", gender=" + gender +
                '}';
    }

    private String birthPlace;
    private Integer age;
    private boolean alive;
    private Date lastSync;
    private boolean active;
    private String nationality;

    public LittlePerson() {
    }

    public LittlePerson(Person fsPerson) {
        setName(fsPerson.getFullName());
        setFamilySearchId(fsPerson.getId());
        if (fsPerson.getGender()!=null) {
            setGender(fsPerson.getGender().getKnownType());
        } else {
            setGender(GenderType.Unknown);
        }
        Name name = null;
        if (fsPerson.getNames()!=null) {
            for (Name n : fsPerson.getNames()) {
                if (n!=null) {
                    if (name == null || (n.getPreferred() != null && n.getPreferred())) {
                        name = n;
                    }
                }
            }
        }
        //-- get preferred given name
        if (name!=null) {
            List<NameForm> forms = name.getNameForms();
            if (forms!=null && forms.size()>0) {
                List<NamePart> parts = forms.get(0).getParts();
                if (parts!=null) {
                    for (NamePart p : parts) {
                        if (p.getKnownType()== NamePartType.Given) {
                            givenName = p.getValue();
                            String[] gparts = givenName.split(" ");
                            if (gparts.length > 1) givenName = gparts[0];
                            break;
                        }
                    }
                }
            }
        }
        if (givenName==null && this.name!=null) {
            String[] parts = this.name.split(" ");
            givenName = parts[0];
        }
        
        //-- calculate age from birth year
        List<Fact> births = fsPerson.getFacts(FactType.Birth);
        if (births!=null) {
            Fact birth = null;
            for(Fact b : births) {
                if (b!=null) {
                    if (birth==null) birth = b;
                    else if (b.getPrimary()!=null && b.getPrimary()) birth = b;
                    else if (b.getDate()!=null && birth.getDate()==null) birth = b;
                }
            }
            if (birth!=null) {
                birthPlace = null;
                if (birth.getPlace()!=null) {
                    PlaceReference place = birth.getPlace();
                    if (place.getNormalized()!=null && place.getNormalized().size()>0) {
                        birthPlace = place.getNormalized().get(0).getValue();
                        if (PlaceHelper.countPlaceLevels(birthPlace) < PlaceHelper.countPlaceLevels(birth.getPlace().getOriginal())) {
                            birthPlace = birth.getPlace().getOriginal();
                        }
                    } else {
                        birthPlace = birth.getPlace().getOriginal();
                    }
                }
                if (birth.getDate()!=null) {
                    String birthDateStr = birth.getDate().getFormal();
                    if (birthDateStr == null) birthDateStr = birth.getDate().getOriginal();
                    if (birthDateStr != null) {
                        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
                        try {
                            this.birthDate = df.parse(birthDateStr);
                            updateAge();
                        } catch (ParseException e) {
                            DateFormat df2 = new SimpleDateFormat("+yyyy-MM-dd");
                            try {
                                this.birthDate = df2.parse(birthDateStr);
                                updateAge();
                            } catch (ParseException e2) {
                                Pattern p = Pattern.compile("\\d\\d\\d\\d");
                                Matcher m = p.matcher(birthDateStr);
                                if (m.find()) {
                                    String birthYearStr = m.group();
                                    int birthYear = Integer.parseInt(birthYearStr);
                                    Calendar cal = Calendar.getInstance();
                                    age = cal.get(Calendar.YEAR) - birthYear;
                                }
                            }
                        }
                    }
                }
            }
        }

        Boolean living = fsPerson.getLiving();
        if (living==null && age!=null && age > 105) {
            living = false;
            fsPerson.setLiving(false);
        }
        if (living==null || living==true) {
            setAlive(true);
        }

        setActive(true);

        Fact nFact = fsPerson.getFirstFactOfType(FactType.Nationality);
        if (nFact!=null) {
            nationality = nFact.getValue();
        }
    }

    public void updateAge() {
        if (birthDate!=null) {
            Calendar today = Calendar.getInstance();
            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);
            age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < birthCal.get(Calendar.MONTH)) age--;
            else if (today.get(Calendar.MONTH) == birthCal.get(Calendar.MONTH)
                    && today.get(Calendar.DATE) < birthCal.get(Calendar.DATE)) age--;
        }
    }

	public void setBirthDate(Date birthDate)
	{
		this.birthDate = birthDate;
	}

	public Date getBirthDate()
	{
		return birthDate;
	}

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public int getDefaultPhotoResource() {
        if (age!=null) {
            if (age < 2) return com.yellowforktech.littlefamilytree.R.drawable.baby;
            if (age < 18) {
                if (gender==GenderType.Female) return com.yellowforktech.littlefamilytree.R.drawable.girl;
                return com.yellowforktech.littlefamilytree.R.drawable.boy;
            }
            if (age < 45) {
                if (gender==GenderType.Female) return com.yellowforktech.littlefamilytree.R.drawable.mom;
                return com.yellowforktech.littlefamilytree.R.drawable.dad;
            }
            if (gender==GenderType.Female) return com.yellowforktech.littlefamilytree.R.drawable.grandma;
            return com.yellowforktech.littlefamilytree.R.drawable.grandpa;
        } else {
            if (gender==GenderType.Female) return com.yellowforktech.littlefamilytree.R.drawable.mom;
            return com.yellowforktech.littlefamilytree.R.drawable.dad;
        }
    }

    public Date getLastSync() {
        return lastSync;
    }

    public void setLastSync(Date lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Boolean isHasParents() {
        return hasParents;
    }

    public void setHasParents(Boolean hasParents) {
        this.hasParents = hasParents;
    }

    public Boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
        if (this.age==null || this.age < 16) this.age = 25;
    }

    public Boolean isHasSpouses() {
        return hasSpouses;
    }

    public void setHasSpouses(Boolean hasSpouses) {
        this.hasSpouses = hasSpouses;
        if (this.age==null || this.age < 16) age = 25;
    }

    public Boolean isHasMedia() {
        return hasMedia;
    }

    public void setHasMedia(Boolean hasMedia) {
        this.hasMedia = hasMedia;
    }

    /**
     * The level in the tree from the root person
     * @return
     */
    public Integer getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(Integer treeLevel) {
        this.treeLevel = treeLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LittlePerson that = (LittlePerson) o;

        if (!familySearchId.equals(that.familySearchId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return familySearchId.hashCode();
    }
}
