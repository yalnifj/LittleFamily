package org.finlayfamily.littlefamily.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.activities.tasks.AuthTask;
import org.finlayfamily.littlefamily.db.DBHelper;
import org.finlayfamily.littlefamily.familysearch.FSResult;
import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.types.RelationshipType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by jfinlay on 2/18/2015.
 */
public class DataService implements AuthTask.Listener {

    private FamilySearchService fsService;
    private DBHelper dbHelper = null;
    private Context context = null;

    private Queue<LittlePerson> syncQ;
    private boolean running = true;

    private static DataService ourInstance = new DataService();

    public static DataService getInstance() {
        return ourInstance;
    }

    private DataService() {
        fsService = FamilySearchService.getInstance();
        syncQ = new LinkedList<>();
        SyncThread syncer = new SyncThread();
        syncer.start();
    }

    public DBHelper getDBHelper() throws Exception {
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
        if (fsService.getSessionId() == null) {
            try {
                String token = getDBHelper().getTokenForSystemId("FamilySearch");
                if (token != null) {
                    if (fsService.getSessionId() == null) {
                        Log.d(this.getClass().getSimpleName(), "Launching new AuthTask for stored credentials");
                        AuthTask task = AuthTask.getInstance();
                        task.addListener(this);
                        task.execute(token);
                    }
                }
            } catch (Exception e) {
                Log.e("DataService", "Error checking authentication", e);
            }
        }
    }

    @Override
    public void onComplete(FSResult result) {
        AuthTask task = AuthTask.getInstance();
        task.removeListener(this);
    }

    public boolean hasData() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();
        if (person!=null) return true;
        return false;
    }

    private class SyncThread extends Thread {
        public void run() {
            while(running) {
                while (syncQ.size() == 0) {
                    try {
                        synchronized (syncQ) {
                            syncQ.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LittlePerson person = null;
                synchronized (syncQ) {
                    person = syncQ.poll();
                }
                try {
                    Log.d("SyncThread", "Synchronizing person "+person.getId()+" "+person.getFamilySearchId()+" "+person.getName());
                    Entry entry = fsService.getLastChangeForPerson(person.getFamilySearchId());
                    Log.d("SyncThread", "Synchronizing person local date="+person.getLastSync()+" remote date="+entry.getUpdated());
                    if (entry==null || entry.getUpdated().after(person.getLastSync())) {
                        Person fsPerson = fsService.getPerson(person.getFamilySearchId(), false);
                        LittlePerson updated = DataHelper.buildLittlePerson(fsPerson, context, false);
                        updated.setId(person.getId());
                        person.setLastSync(updated.getLastSync());
                        person.setPhotoPath(updated.getPhotoPath());
                        person.setAge(updated.getAge());
                        person.setBirthDate(updated.getBirthDate());
                        person.setFamilySearchId(updated.getFamilySearchId());
                        person.setGender(updated.getGender());
                        person.setGivenName(updated.getGivenName());
                        person.setName(updated.getName());
                        getDBHelper().persistLittlePerson(person);

                        List<Relationship> closeRelatives = fsService.getCloseRelatives(person.getFamilySearchId(), false);
                        if (closeRelatives!=null) {
                            List<org.finlayfamily.littlefamily.data.Relationship> oldRelations = getDBHelper().getRelationshipsForPerson(person.getId());
                            for(Relationship r : closeRelatives) {
                                org.finlayfamily.littlefamily.data.RelationshipType type = org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD;
                                if (r.getKnownType()== RelationshipType.Couple) {
                                    type = org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE;
                                }
                                if (!r.getPerson1().getResourceId().equals(person.getFamilySearchId())) {
                                    org.finlayfamily.littlefamily.data.Relationship rel = syncRelationship(person, r.getPerson1().getResourceId(), type);
                                    if (rel!=null) {
                                        oldRelations.remove(rel);
                                    }
                                }
                                if (!r.getPerson2().getResourceId().equals(person.getFamilySearchId())) {
                                    org.finlayfamily.littlefamily.data.Relationship rel = syncRelationship(person, r.getPerson2().getResourceId(), type);
                                    if (rel!=null) {
                                        oldRelations.remove(rel);
                                    }
                                }
                            }

                            for(org.finlayfamily.littlefamily.data.Relationship rel : oldRelations) {
                                getDBHelper().deleteRelationshipById(rel.getId());
                            }
                        }

                        List<SourceDescription> sds = fsService.getPersonMemories(person.getFamilySearchId(), true);
                        if (sds!=null) {
                            List<Media> oldMedia = getDBHelper().getMediaForPerson(person.getId());
                            for (SourceDescription sd : sds) {
                                Media med = getDBHelper().getMediaByFamilySearchId(sd.getId());
                                if (med==null) {
                                    List<Link> links = sd.getLinks();
                                    if (links != null) {
                                        for (Link link : links) {
                                            if (link.getRel() != null && link.getRel().equals("image")) {
                                                med = new Media();
                                                med.setType("photo");
                                                med.setFamilySearchId(sd.getId());
                                                String localPath = DataHelper.downloadFile(link.getHref().toString(), person.getFamilySearchId(), DataHelper.lastPath(link.getHref().toString()), context);
                                                if (localPath!=null) {
                                                    med.setLocalPath(localPath);
                                                    getDBHelper().persistMedia(med);
                                                    Tag tag = new Tag();
                                                    tag.setMediaId(med.getId());
                                                    tag.setPersonId(person.getId());
                                                    getDBHelper().persistTag(tag);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    oldMedia.remove(med);
                                }
                            }

                            for(Media old : oldMedia) {
                                getDBHelper().deleteMediaById(old.getId());
                            }
                        }
                    } else {
                        person.setLastSync(new java.util.Date());
                        getDBHelper().persistLittlePerson(person);
                    }
                } catch (FamilySearchException e) {
                    Log.e("SyncThread", "Error reading from FamilySearch", e);
                } catch (Exception e) {
                    Log.e("SyncThread", "Error syncing person", e);
                }
            }
        }

        private org.finlayfamily.littlefamily.data.Relationship syncRelationship(LittlePerson person, String fsid, org.finlayfamily.littlefamily.data.RelationshipType type) throws Exception {
            LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsid);
            if (relative==null) {
                Person fsPerson2 = fsService.getPerson(fsid, false);
                relative = DataHelper.buildLittlePerson(fsPerson2, context, false);
                getDBHelper().persistLittlePerson(relative);
            }
            if (relative!=null) {
                org.finlayfamily.littlefamily.data.Relationship rel = getDBHelper().getRelationship(person.getId(), relative.getId(), type);
                if (rel==null) {
                    rel = new org.finlayfamily.littlefamily.data.Relationship();
                    rel.setId1(person.getId());
                    rel.setId2(relative.getId());
                    rel.setType(type);
                    getDBHelper().persistRelationship(rel);
                }
                return rel;
            }
            return null;
        }
    }

    public void addToSyncQ(LittlePerson person) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        if (person.getLastSync().before(cal.getTime())) {
            synchronized (syncQ) {
                if (!syncQ.contains(person)) {
                    syncQ.add(person);
                    syncQ.notifyAll();
                }
            }
        }
    }

    public LittlePerson getDefaultPerson() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();

        if (person==null) {
            Person fsPerson = fsService.getCurrentPerson();
            person = DataHelper.buildLittlePerson(fsPerson, context, true);
            getDBHelper().persistLittlePerson(person);
        } else {
            addToSyncQ(person);
        }

        return person;
    }

    public LittlePerson getPersonById(int id) throws Exception {
        LittlePerson person = getDBHelper().getPersonById(id);
        if (person!=null) addToSyncQ(person);
        return person;
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person) throws Exception {
        List<LittlePerson> family = getDBHelper().getRelativesForPerson(person.getId());
        if (family==null || family.size()==0) {
            family = new ArrayList<>();
            List<Relationship> closeRelatives = fsService.getCloseRelatives(person.getFamilySearchId(), true);
            if (closeRelatives!=null) {
                for(Relationship r : closeRelatives) {
                    if (!r.getPerson1().getResourceId().equals(person.getFamilySearchId())) {
                        Person fsPerson = fsService.getPerson(r.getPerson1().getResourceId(), true);
                        LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsPerson.getId());
                        if (relative==null) {
                            relative = DataHelper.buildLittlePerson(fsPerson, context, true);
                        }
                        if (relative!=null) {
                            getDBHelper().persistLittlePerson(relative);
                            family.add(relative);
                            addToSyncQ(relative);
                            org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                            rel.setId1(relative.getId());
                            rel.setId2(person.getId());
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
                        Person fsPerson = fsService.getPerson(r.getPerson2().getResourceId(), true);
                        LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsPerson.getId());
                        if (relative==null) {
                            relative = DataHelper.buildLittlePerson(fsPerson, context, true);
                        }
                        if (relative!=null) {
                            getDBHelper().persistLittlePerson(relative);
                            family.add(relative);
                            addToSyncQ(relative);
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

    public List<LittlePerson> getParents(LittlePerson person) throws Exception {
        List<LittlePerson> parents = getDBHelper().getParentsForPerson(person.getId());
        if (parents==null || parents.size()==0) {
            getFamilyMembers(person);
            parents = getDBHelper().getParentsForPerson(person.getId());
            if (parents==null) {
                parents = new ArrayList<>();
            }
        }
        return parents;
    }

    public List<Media> getMediaForPerson(LittlePerson person) throws Exception {
        List<Media> media = getDBHelper().getMediaForPerson(person.getId());
        if (media==null || media.size()==0) {
            media = new ArrayList<>();
            try {
                List<SourceDescription> sds = fsService.getPersonMemories(person.getFamilySearchId(), true);
                if (sds!=null) {
                    for (SourceDescription sd : sds) {
                        Media med = getDBHelper().getMediaByFamilySearchId(sd.getId());
                        if (med==null) {
                            List<Link> links = sd.getLinks();
                            if (links != null) {
                                for (Link link : links) {
                                    if (link.getRel() != null && link.getRel().equals("image")) {
                                        med = new Media();
                                        med.setType("photo");
                                        med.setFamilySearchId(sd.getId());
                                        String localPath = DataHelper.downloadFile(link.getHref().toString(), person.getFamilySearchId(), DataHelper.lastPath(link.getHref().toString()), context);
                                        if (localPath!=null) {
                                            med.setLocalPath(localPath);
                                            getDBHelper().persistMedia(med);
                                            media.add(med);
                                            Tag tag = new Tag();
                                            tag.setMediaId(med.getId());
                                            tag.setPersonId(person.getId());
                                            getDBHelper().persistTag(tag);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch(FamilySearchException e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
                Toast.makeText(context, "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
            }
        }
        return media;
    }
}
