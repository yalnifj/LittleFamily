package org.finlayfamily.littlefamily.data;

import android.content.Context;

import org.finlayfamily.littlefamily.db.DBHelper;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.conclusion.*;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.types.*;
import org.gedcomx.types.RelationshipType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 2/18/2015.
 */
public class DataService {

    private FamilySearchService fsService;
    private DBHelper dbHelper = null;
    private Context context = null;

    private static DataService ourInstance = new DataService();

    public static DataService getInstance() {
        return ourInstance;
    }

    private DataService() {
        fsService = FamilySearchService.getInstance();
    }

    private DBHelper getDBHelper() throws Exception {
        if (dbHelper==null) {
            if (this.context==null) {
                throw new Exception("Context must be set before using the DataService.");
            }
            dbHelper = new DBHelper(context);
        }
        return dbHelper;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean hasData() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();
        if (person!=null) return true;
        return false;
    }

    public LittlePerson getDefaultPerson() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();

        if (person==null) {
            Person fsPerson = fsService.getCurrentPerson();
            person = DataHelper.buildLittlePerson(fsPerson, context);
            getDBHelper().persistLittlePerson(person);
        }

        return person;
    }

    public LittlePerson getPersonById(int id) throws Exception {
        LittlePerson person = getDBHelper().getPersonById(id);
        return person;
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person) throws Exception {
        List<LittlePerson> family = getDBHelper().getRelativesForPerson(person.getId());
        if (family==null || family.size()==0) {
            family = new ArrayList<>();
            List<Relationship> closeRelatives = fsService.getCloseRelatives(person.getFamilySearchId());
            if (closeRelatives!=null) {
                for(Relationship r : closeRelatives) {
                    if (!r.getPerson1().getResourceId().equals(person.getFamilySearchId())) {
                        Person fsPerson = fsService.getPerson(r.getPerson1().getResourceId());
                        LittlePerson relative = DataHelper.buildLittlePerson(fsPerson, context);
                        if (relative!=null) {
                            getDBHelper().persistLittlePerson(relative);
                            family.add(relative);
                            org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                            rel.setId1(person.getId());
                            rel.setId2(relative.getId());
                            if (r.getKnownType()== RelationshipType.Couple) {
                                rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                            }
                            else {
                                rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD);
                            }
                            getDBHelper().persistRelationship(rel);
                        }
                    }
                    if (!r.getPerson2().getResourceId().equals(person.getFamilySearchId())) {
                        Person fsPerson = fsService.getPerson(r.getPerson2().getResourceId());
                        LittlePerson relative = DataHelper.buildLittlePerson(fsPerson, context);
                        if (relative!=null) {
                            getDBHelper().persistLittlePerson(relative);
                            family.add(relative);
                            org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                            rel.setId1(person.getId());
                            rel.setId2(relative.getId());
                            if (r.getKnownType()== RelationshipType.Couple) {
                                rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                            }
                            else {
                                rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD);
                            }
                            getDBHelper().persistRelationship(rel);
                        }
                    }
                }
            }
        }
        return family;
    }
}
