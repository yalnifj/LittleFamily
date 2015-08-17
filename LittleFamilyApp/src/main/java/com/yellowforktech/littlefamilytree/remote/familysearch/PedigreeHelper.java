package com.yellowforktech.littlefamilytree.remote.familysearch;

import com.yellowforktech.littlefamilytree.data.DataHelper;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.Relationship;
import com.yellowforktech.littlefamilytree.data.RelationshipType;

import org.gedcomx.conclusion.Person;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Parents on 8/16/2015.
 */
public class PedigreeHelper {

    public static Map<Integer, LittlePerson> getPedigree(LittlePerson person, DataService dataService) throws Exception {
        FamilySearchService fsService = (FamilySearchService) dataService.getRemoteService();
        Map<Integer, Person> tree = fsService.getPedigree(person.getFamilySearchId());

        for (Person p : tree.values()) {
            LittlePerson lp = dataService.getPersonByRemoteId(p.getId());
            if (lp==null) {
                Person fsPerson = fsService.getPerson(p.getId(), true);
                //-- check for person by alternate id
                if (!fsPerson.getId().equals(p.getId())) {
                    lp = dataService.getDBHelper().getPersonByFamilySearchId(fsPerson.getId());
                }
                if (lp == null) {
                    dataService.fireStatusUpdate("Processing " + fsPerson.getFullName());
                    lp = DataHelper.buildLittlePerson(fsPerson, dataService.getContext(), fsService, true);
                    dataService.getDBHelper().persistLittlePerson(lp);
                }
            }
        }
        //  8 9 10 11 12 13 14 15
        //   4    5     6     7
        //      2          3
        //            1
        Map<Integer, LittlePerson> returnMap = new HashMap<>(tree.size());
        for(Integer ahnen : tree.keySet()) {
            Person p = tree.get(ahnen);
            Integer parent1 = ahnen * 2;
            Integer parent2 = parent1 + 1;
            if (parent1 < tree.size()) {
                Person p1 = tree.get(parent1);
                Person p2 = tree.get(parent2);
                LittlePerson lp = dataService.getDBHelper().getPersonByFamilySearchId(p.getId());
                LittlePerson lp1 = dataService.getDBHelper().getPersonByFamilySearchId(p1.getId());
                LittlePerson lp2 = dataService.getDBHelper().getPersonByFamilySearchId(p2.getId());
                if (lp!=null && lp1!=null) {
                    returnMap.put(ahnen, lp);
                    returnMap.put(parent1, lp1);
                    Relationship rel = dataService.getDBHelper()
                            .getRelationship(lp1.getId(), lp.getId(), RelationshipType.PARENTCHILD);
                    if (rel == null) {
                        rel = new Relationship();
                        rel.setId1(lp1.getId());
                        rel.setId2(lp.getId());
                        rel.setType(RelationshipType.PARENTCHILD);
                        dataService.getDBHelper().persistRelationship(rel);
                    }
                }
                if (lp!=null && lp2!=null) {
                    returnMap.put(ahnen, lp);
                    returnMap.put(parent1, lp2);
                    Relationship rel = dataService.getDBHelper()
                            .getRelationship(lp2.getId(), lp.getId(), RelationshipType.PARENTCHILD);
                    if (rel == null) {
                        rel = new Relationship();
                        rel.setId1(lp2.getId());
                        rel.setId2(lp.getId());
                        rel.setType(RelationshipType.PARENTCHILD);
                        dataService.getDBHelper().persistRelationship(rel);
                    }
                }
            }
        }

        return returnMap;
    }
}
