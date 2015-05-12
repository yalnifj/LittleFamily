package org.finlayfamily.littlefamily.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.finlayfamily.littlefamily.activities.tasks.AuthTask;
import org.finlayfamily.littlefamily.db.DBHelper;
import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.RemoteService;
import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;
import org.finlayfamily.littlefamily.remote.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.remote.phpgedview.PGVService;
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

/**
 * Created by jfinlay on 2/18/2015.
 */
public class DataService implements AuthTask.Listener {
    public static final String SERVICE_TYPE = "serviceType";
    public static final String SERVICE_TYPE_PHPGEDVIEW = PGVService.class.getSimpleName();
    public static final String SERVICE_TYPE_FAMILYSEARCH = FamilySearchService.class.getSimpleName();
    public static final String SERVICE_TOKEN = "Token";
    public static final String SERVICE_BASEURL = "BaseUrl";
    public static final String SERVICE_DEFAULTPERSONID = "DefaultPersonId";

    private RemoteService remoteService;
    private DBHelper dbHelper = null;
    private Context context = null;

    private Queue<ThreadPerson> syncQ;
    private boolean running = true;
    private int QDepth;

    private SyncThread syncer;
    private boolean authenticating = false;
    private String serviceType = null;

    private static DataService ourInstance = new DataService();

    public static DataService getInstance() {
        return ourInstance;
    }

    private DataService() {
        syncQ = new LinkedList<>();
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

    public void setRemoteService(String type, RemoteService service) {
        this.serviceType = type;
        this.remoteService = service;
    }

    public RemoteService getRemoteService() {
        return remoteService;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setContext(Context context) {
        this.context = context;
        if (remoteService==null) {
            try {
                serviceType = getDBHelper().getProperty(SERVICE_TYPE);
                if (serviceType != null) {
                    if (serviceType.equals(PGVService.class.getSimpleName())) {
                        String baseUrl = getDBHelper().getProperty(serviceType + SERVICE_BASEURL);
                        String defaultPersonId = getDBHelper().getProperty(serviceType + SERVICE_DEFAULTPERSONID);
                        remoteService = new PGVService(baseUrl, defaultPersonId);
                    } else {
                        remoteService = FamilySearchService.getInstance();
                    }
                    if (remoteService.getSessionId() == null) {
                        String token = getDBHelper().getTokenForSystemId(serviceType + SERVICE_TOKEN);
                        if (token != null) {
                            synchronized (this) {
                                if (remoteService.getSessionId() == null && !authenticating) {
                                    authenticating = true;
                                    Log.d(this.getClass().getSimpleName(), "Launching new AuthTask for stored credentials");
                                    AuthTask task = new AuthTask(this, remoteService);
                                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("DataService", "Error checking authentication", e);
            }
        }
        if (syncer==null) {
            syncer = new SyncThread();
            syncer.start();
        }
    }

    @Override
    public void onComplete(RemoteResult result) {
        synchronized (this) {
            authenticating = false;
            this.notifyAll();
        }
        if (syncer==null) {
            syncer = new SyncThread();
            syncer.start();
        }
    }

    public boolean hasData() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();
        if (person!=null) return true;
        return false;
    }

    public void waitForAuth() {
        while(authenticating) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                Log.e(this.getClass().getSimpleName(), "Error waiting for authentication", e);
            }
        }
    }

    private class ThreadPerson {
        LittlePerson person;
        int depth;

        @Override
        public boolean equals(Object tp) {
            if (tp instanceof  ThreadPerson)
                return ((ThreadPerson)tp).person.equals(person);
            return false;
        }
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

                waitForAuth();

                ThreadPerson tp = null;
                synchronized (syncQ) {
                    tp = syncQ.poll();
                }
                try {
                    if (tp != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.HOUR, -1);
                        LittlePerson person = tp.person;
                        if (person.getLastSync().before(cal.getTime())) {
                            Log.d("SyncThread", "Synchronizing person " + person.getId() + " " + person.getFamilySearchId() + " " + person.getName());
                            Entry entry = remoteService.getLastChangeForPerson(person.getFamilySearchId());
                            Log.d("SyncThread", "Synchronizing person local date=" + person.getLastSync() + " remote date=" + entry);
                            if (entry == null || entry.getUpdated().after(person.getLastSync())) {
                                Person fsPerson = remoteService.getPerson(person.getFamilySearchId(), false);
                                LittlePerson updated = DataHelper.buildLittlePerson(fsPerson, context, remoteService, false);
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

                                List<Relationship> closeRelatives = remoteService.getCloseRelatives(person.getFamilySearchId(), false);
                                if (closeRelatives != null) {
                                    List<org.finlayfamily.littlefamily.data.Relationship> oldRelations = getDBHelper().getRelationshipsForPerson(person.getId());
                                    for (Relationship r : closeRelatives) {
                                        org.finlayfamily.littlefamily.data.RelationshipType type = org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD;
                                        if (r.getKnownType() == RelationshipType.Couple) {
                                            type = org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE;
                                        }
                                        if (!r.getPerson1().getResourceId().equals(person.getFamilySearchId())) {
                                            org.finlayfamily.littlefamily.data.Relationship rel = syncRelationship(person, r.getPerson1().getResourceId(), type);
                                            if (rel != null) {
                                                oldRelations.remove(rel);
                                            }
                                        }
                                        if (!r.getPerson2().getResourceId().equals(person.getFamilySearchId())) {
                                            org.finlayfamily.littlefamily.data.Relationship rel = syncRelationship(person, r.getPerson2().getResourceId(), type);
                                            if (rel != null) {
                                                oldRelations.remove(rel);
                                            }
                                        }
                                    }

                                    for (org.finlayfamily.littlefamily.data.Relationship rel : oldRelations) {
                                        getDBHelper().deleteRelationshipById(rel.getId());
                                    }
                                }

                                List<SourceDescription> sds = remoteService.getPersonMemories(person.getFamilySearchId(), true);
                                if (sds != null) {
                                    List<Media> oldMedia = getDBHelper().getMediaForPerson(person.getId());
                                    for (SourceDescription sd : sds) {
                                        Media med = getDBHelper().getMediaByFamilySearchId(sd.getId());
                                        if (med == null) {
                                            List<Link> links = sd.getLinks();
                                            if (links != null) {
                                                for (Link link : links) {
                                                    if (link.getRel() != null && link.getRel().equals("image")) {
                                                        med = new Media();
                                                        med.setType("photo");
                                                        med.setFamilySearchId(sd.getId());
                                                        String localPath = DataHelper.downloadFile(link.getHref().toString(), person.getFamilySearchId(), DataHelper.lastPath(link.getHref().toString()), remoteService, context);
                                                        if (localPath != null) {
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

                                    for (Media old : oldMedia) {
                                        getDBHelper().deleteMediaById(old.getId());
                                    }
                                }
                            } else {
                                person.setLastSync(new java.util.Date());
                                getDBHelper().persistLittlePerson(person);
                            }
                        }
                    }
                    try {
                        Thread.sleep(5000);  //-- don't bombard the server
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //-- force load of family members if we haven't previously loaded them
                    //--- allows building the tree in the background
                    if (tp.depth < 5) {
                        QDepth = tp.depth;
                        List<LittlePerson> family = getDBHelper().getRelativesForPerson(tp.person.getId(), false);
                        if (tp.person.isHasParents() == null || family == null || family.size() == 0) {
                            family = getFamilyMembersFromRemoteService(tp.person, false);
                        }
                    }
                    QDepth = 0;
                }catch(RemoteServiceSearchException e){
                    Log.e("SyncThread", "Error reading from FamilySearch", e);
                }catch(Exception e){
                    Log.e("SyncThread", "Error syncing person", e);
                }
            }
        }

        private org.finlayfamily.littlefamily.data.Relationship syncRelationship(LittlePerson person, String fsid, org.finlayfamily.littlefamily.data.RelationshipType type) throws Exception {
            LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsid);
            if (relative==null) {
                Person fsPerson2 = remoteService.getPerson(fsid, false);
                relative = DataHelper.buildLittlePerson(fsPerson2, context, remoteService, false);
                relative.setHasParents(true);
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

    public void addToSyncQ(LittlePerson person, int depth) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        if (person.isHasParents()==null || person.getLastSync().before(cal.getTime())) {
            synchronized (syncQ) {
                ThreadPerson tp = new ThreadPerson();
                tp.person = person;
                tp.depth = depth;
                if (!syncQ.contains(tp)) {
                    syncQ.add(tp);
                    syncQ.notifyAll();
                }
            }
        }
    }

    public LittlePerson getDefaultPerson() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();

        if (person==null) {
            waitForAuth();
            Person fsPerson = remoteService.getCurrentPerson();
            person = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
            getDBHelper().persistLittlePerson(person);
        } else {
            addToSyncQ(person, 0);
        }

        return person;
    }

    public LittlePerson getPersonById(int id) throws Exception {
        LittlePerson person = getDBHelper().getPersonById(id);
        if (person!=null) addToSyncQ(person, QDepth);
        return person;
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person) throws Exception {
        return getFamilyMembers(person, true);
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person, boolean loadSpouse) throws Exception {
        List<LittlePerson> family = getDBHelper().getRelativesForPerson(person.getId(), loadSpouse);
        if (family==null || family.size()==0) {
            family = getFamilyMembersFromRemoteService(person, loadSpouse);
        }
        return family;
    }

    public List<LittlePerson> getFamilyMembersFromRemoteService(LittlePerson person, boolean loadSpouse) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        LittlePerson spouse = null;
        List<Relationship> closeRelatives = remoteService.getCloseRelatives(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
        }
        if (loadSpouse) {
            List<LittlePerson> spouseParents = new ArrayList<>();
            for(LittlePerson p : family) {
                if ("spouse".equals(p.getRelationship())) {
                    List<Relationship> spouseRelatives = remoteService.getCloseRelatives(p.getFamilySearchId(), false);
                    List<LittlePerson> spouseFamily = processRelatives(spouseRelatives, p);
                    for(LittlePerson parent : spouseFamily) {
                        if ("parent".equals(parent.getRelationship()) && !family.contains(parent)) {
                            spouseParents.add(parent);
                        }
                    }
                }
            }
            family.addAll(spouseParents);
        }
        return family;
    }

    private List<LittlePerson> processRelatives(List<Relationship> closeRelatives, LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        for(Relationship r : closeRelatives) {
            if (!r.getPerson1().getResourceId().equals(person.getFamilySearchId())) {
                LittlePerson relative = getDBHelper().getPersonByFamilySearchId(r.getPerson1().getResourceId());
                if (relative==null) {
                    Person fsPerson = remoteService.getPerson(r.getPerson1().getResourceId(), true);
                    relative = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
                }
                if (relative!=null) {
                    family.add(relative);
                    addToSyncQ(relative, QDepth+1);
                    org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                    rel.setId2(person.getId());
                    if (r.getKnownType()== RelationshipType.Couple) {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                        relative.setRelationship("spouse");
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge());
                        }
                    }
                    else {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD);
                        relative.setRelationship("parent");
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge()+25);
                        }
                        if(person.isHasParents()==null || person.isHasParents()==false) {
                            person.setHasParents(true);
                            getDBHelper().persistLittlePerson(person);
                        }
                    }
                    getDBHelper().persistLittlePerson(relative);
                    rel.setId1(relative.getId());
                    getDBHelper().persistRelationship(rel);
                }
            }
            if (!r.getPerson2().getResourceId().equals(person.getFamilySearchId())) {
                LittlePerson relative = getDBHelper().getPersonByFamilySearchId(r.getPerson2().getResourceId());
                if (relative==null) {
                    Person fsPerson = remoteService.getPerson(r.getPerson2().getResourceId(), true);
                    relative = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
                }
                if (relative!=null) {
                    family.add(relative);
                    addToSyncQ(relative, QDepth+1);
                    org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                    rel.setId1(person.getId());
                    if (r.getKnownType()== RelationshipType.Couple) {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                        relative.setRelationship("spouse");
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge());
                        }
                    }
                    else {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.PARENTCHILD);
                        relative.setRelationship("child");
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge()-20);
                        }
                        if(relative.isHasParents()==null || relative.isHasParents()==false) {
                            relative.setHasParents(true);
                        }
                    }
                    getDBHelper().persistLittlePerson(relative);
                    rel.setId2(relative.getId());
                    getDBHelper().persistRelationship(rel);
                }
            }
        }
        return family;
    }

    public List<LittlePerson> getParents(LittlePerson person) throws Exception {
        List<LittlePerson> parents = getDBHelper().getParentsForPerson(person.getId());
        if (parents==null || parents.size()==0) {
            if (person.isHasParents()==null || person.isHasParents()) {
                getFamilyMembersFromRemoteService(person, false);
                parents = getDBHelper().getParentsForPerson(person.getId());
                if (parents == null) {
                    parents = new ArrayList<>();
                }
            }
        }
        return parents;
    }

    public List<Media> getMediaForPerson(LittlePerson person) throws Exception {
        List<Media> media = getDBHelper().getMediaForPerson(person.getId());
        if (media==null || media.size()==0) {
            media = new ArrayList<>();
            try {
                waitForAuth();
                List<SourceDescription> sds = remoteService.getPersonMemories(person.getFamilySearchId(), true);
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
                                        String localPath = DataHelper.downloadFile(link.getHref().toString(), person.getFamilySearchId(), DataHelper.lastPath(link.getHref().toString()), remoteService, context);
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
            } catch(RemoteServiceSearchException e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
                Toast.makeText(context, "Error communicating with FamilySearch. " + e, Toast.LENGTH_LONG).show();
            }
        }
        return media;
    }
}
