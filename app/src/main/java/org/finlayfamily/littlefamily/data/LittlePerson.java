package org.finlayfamily.littlefamily.data;

import org.gedcomx.conclusion.Name;
import org.gedcomx.conclusion.NameForm;
import org.gedcomx.conclusion.NamePart;
import org.finlayfamily.littlefamily.R;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.Person;
import org.gedcomx.types.FactType;
import org.gedcomx.types.GenderType;
import org.gedcomx.types.NamePartType;
import org.gedcomx.types.NamePartType;

import java.io.Serializable;
import java.util.List;

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
    private long id;
    private String name;
    private String givenName;
    private String relationship;
    private String familySearchId;
    private String photoPath;
    private GenderType gender;
	private Date birthDate;
    private Integer age;

    public LittlePerson() {
    }

    public LittlePerson(Person fsPerson) {
        setName(fsPerson.getFullName());
        setFamilySearchId(fsPerson.getId());
        setGender(fsPerson.getGender().getKnownType());
        Name name = null;
        for(Name n : fsPerson.getNames()) {
            if (n.getPreferred()) {
                name = n;
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
                if (b.getDate()!=null && (birth==null || b.getPrimary())) birth = b;
            }
            if (birth!=null && birth.getDate()!=null) {
                String birthDateStr = birth.getDate().getFormal();
                if (birthDateStr==null) birthDateStr = birth.getDate().getOriginal();
                if (birthDateStr!=null) {
                    DateFormat df = new SimpleDateFormat("dd MMM yyyy");
                    try {
                        this.birthDate = df.parse(birthDateStr);
                        Date today = new Date();
                        age = (int) (today.getTime() - birthDate.getTime()) / (1000 * 60 * 60 * 24 * 365);
                    } catch (ParseException e) {
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

	public void setBirthDate(Date birthDate)
	{
		this.birthDate = birthDate;
	}

	public Date getBirthDate()
	{
		return birthDate;
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
            if (age < 2) return R.drawable.baby;
            if (age < 18) {
                if (gender==GenderType.Female) return R.drawable.girl;
                return R.drawable.boy;
            }
            if (age < 45) {
                if (gender==GenderType.Female) return R.drawable.mom;
                return R.drawable.dad;
            }
            if (gender==GenderType.Female) return R.drawable.grandma;
            return R.drawable.grandpa;
        } else {
            if (gender==GenderType.Female) return R.drawable.mom;
            return R.drawable.dad;
        }
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
