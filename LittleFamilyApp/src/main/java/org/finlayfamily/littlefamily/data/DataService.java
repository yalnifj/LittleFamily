package org.finlayfamily.littlefamily.data;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.finlayfamily.littlefamily.activities.tasks.AuthTask;
import org.finlayfamily.littlefamily.db.DBHelper;
import org.finlayfamily.littlefamily.remote.AES;
import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.RemoteService;
import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;
import org.finlayfamily.littlefamily.remote.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.remote.phpgedview.PGVService;
import org.finlayfamily.littlefamily.util.PlaceHelper;
import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.types.RelationshipType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by jfinlay on 2/18/2015.
 */
public class DataService implements AuthTask.Listener {
    public static final String SERVICE_TYPE = "service_type";
    public static final String SERVICE_TYPE_PHPGEDVIEW = PGVService.class.getSimpleName();
    public static final String SERVICE_TYPE_FAMILYSEARCH = FamilySearchService.class.getSimpleName();
    public static final String SERVICE_TOKEN = "Token";
    public static final String SERVICE_BASEURL = "BaseUrl";
    public static final String SERVICE_DEFAULTPERSONID = "DefaultPersonId";
    public static final String SERVICE_USERNAME= "Username";

    private RemoteService remoteService;
    private DBHelper dbHelper = null;
    private Context context = null;

    private Queue<ThreadPerson> syncQ;
    private boolean running = true;
    private int QDepth;

    private SyncThread syncer;
    private boolean authenticating = false;
    private String serviceType = null;

    private List<DataNetworkStateListener> listeners;
    private DataNetworkState currentState = DataNetworkState.REMOTE_FINISHED;

    private static DataService ourInstance = new DataService();

    public static DataService getInstance() {
        return ourInstance;
    }

    private DataService() {
        syncQ = new LinkedList<>();
        listeners = new ArrayList<>();
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
        syncQ.clear();
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
                //serviceType = getDBHelper().getProperty(SERVICE_TYPE);
                serviceType = PreferenceManager.getDefaultSharedPreferences(context).getString(SERVICE_TYPE, null);
                if (serviceType != null) {
                    if (serviceType.equals(PGVService.class.getSimpleName())) {
                        String baseUrl = getDBHelper().getProperty(serviceType + SERVICE_BASEURL);
                        String defaultPersonId = getDBHelper().getProperty(serviceType + SERVICE_DEFAULTPERSONID);
                        remoteService = new PGVService(baseUrl, defaultPersonId);
                    } else {
                        remoteService = FamilySearchService.getInstance();
                    }
                    if (remoteService.getSessionId() == null) {
                        autoLogin();
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
	
	public void autoLogin()
	{
		try {
		String token = getDBHelper().getTokenForSystemId(serviceType + SERVICE_TOKEN);
        if (token != null)
		{
			synchronized (this)
			{
				if (remoteService.getSessionId() == null && !authenticating)
				{
					authenticating = true;
					Log.d(this.getClass().getSimpleName(), "Launching new AuthTask for stored credentials");
					AuthTask task = new AuthTask(this, remoteService);
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, token);
				}
			}
		}
		} catch(Exception e) {
			Log.e("dataService", "Unable to authenticate", e);
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
            Log.d("SyncThread", "SyncThread started.");
            try {
                //-- read old q from DB
                List<Integer> ids = getDBHelper().getSyncQ();
                if (ids!=null) {
                    for(Integer id : ids) {
                        if (id!=null) {
                            LittlePerson person = getDBHelper().getPersonById(id);
                            if (person!=null) {
                                ThreadPerson tp = new ThreadPerson();
                                tp.depth = 0;
                                tp.person = person;
                                syncQ.add(tp);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SyncThread", "Error reading Q from DB", e);
            }

            while(running) {
                while (syncQ.size() == 0) {
                    try {
                        Log.d("SyncThread", "Waiting for Q data");
                        synchronized (syncQ) {
                            syncQ.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

				if (remoteService.getSessionId()==null) {
					autoLogin();
				}
                waitForAuth();

                ThreadPerson tp = null;
                synchronized (syncQ) {
                    tp = syncQ.poll();
                }
                try {
                    if (tp != null) {
                        getDBHelper().removeFromSyncQ(tp.person.getId());
                        Calendar cal = Calendar.getInstance();
                        String syncDelayStr = PreferenceManager.getDefaultSharedPreferences(context).getString("sync_delay", "1");
                        int syncDelay = Integer.parseInt(syncDelayStr);
                        cal.add(Calendar.HOUR, -1 * syncDelay);
                        LittlePerson person = tp.person;
                        if (person.getLastSync().before(cal.getTime()) || person.isHasParents()==null) {
                            Log.d("SyncThread", "Synchronizing person " + person.getId() + " " + person.getFamilySearchId() + " " + person.getName());
                            Entry entry = remoteService.getLastChangeForPerson(person.getFamilySearchId());
                            Log.d("SyncThread", "Synchronizing person local date=" + person.getLastSync() + " remote date=" + entry);
                            if (entry == null || entry.getUpdated().after(person.getLastSync())) {
                                syncPerson(person);
                            } else {
                                person.setLastSync(new java.util.Date());
                                getDBHelper().persistLittlePerson(person);
                            }
                        }
                    }
                    try {
                        Thread.sleep(10000);  //-- don't bombard the server
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //-- force load of family members if we haven't previously loaded them
                    //--- allows building the tree in the background
                    if (tp.depth < 6 || tp.person.isHasParents() == null) {
                        QDepth = tp.depth;
                        LittlePerson defaultPerson = getDefaultPerson();
                        String defaultPlace = PlaceHelper.getPlaceCountry(defaultPerson.getBirthPlace());
                        String place = PlaceHelper.getPlaceCountry(tp.person.getBirthPlace());
                        if (place!=null && place.equals(defaultPlace) && tp.person.isHasParents() == null) {
                            List<LittlePerson> parents = getParentsFromRemoteService(tp.person);
                            for (LittlePerson p : parents) {
                                addToSyncQ(p, tp.depth+1);
                            }
                        }
                    }
                    QDepth = 0;
                }catch(RemoteServiceSearchException e){
                    Log.e("SyncThread", "Error reading from "+serviceType, e);

                }catch(Exception e){
                    Log.e("SyncThread", "Error syncing person", e);
                }
            }
        }
    }
    //--end sync thread

    public void syncPerson(LittlePerson person) throws Exception {
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

        try {
            Thread.sleep(5000);  //-- don't bombard the server
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    }

    private org.finlayfamily.littlefamily.data.Relationship syncRelationship(LittlePerson person, String fsid, org.finlayfamily.littlefamily.data.RelationshipType type) throws Exception {
        LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsid);
        if (relative==null) {
            try {
                Thread.sleep(5000);  //-- don't bombard the server
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Person fsPerson2 = remoteService.getPerson(fsid, true);
            relative = DataHelper.buildLittlePerson(fsPerson2, context, remoteService, true);
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

    public void addToSyncQ(LittlePerson person, int depth) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        if (person.isHasParents()==null || person.getLastSync().before(cal.getTime())) {
            synchronized (syncQ) {
                ThreadPerson tp = new ThreadPerson();
                tp.person = person;
                tp.depth = depth;
                if (!syncQ.contains(tp)) {
                    syncQ.add(tp);
                    getDBHelper().addToSyncQ(person.getId());
                    syncQ.notifyAll();
                }
            }
        }
    }

    public void addToSyncQ(List<LittlePerson> people, int depth) throws Exception {
        for (LittlePerson person : people) {
            addToSyncQ(person, depth);
        }
    }

    public void registerNetworkStateListener(DataNetworkStateListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterNetworkStateListener(DataNetworkStateListener listener) {
        this.listeners.remove(listener);
    }

    public void fireNetworkStateChanged(DataNetworkState newState) {
        if (newState != currentState) {
            currentState = newState;
            for (DataNetworkStateListener listener : listeners) {
                listener.remoteStateChanged(newState);
            }
        }
    }

    public LittlePerson getDefaultPerson() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();

        if (person==null) {
            fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
            waitForAuth();
            Person fsPerson = remoteService.getCurrentPerson();
            person = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
            getDBHelper().persistLittlePerson(person);
            fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
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

    public LittlePerson getPersonByRemoteId(String fsid) throws Exception {
        LittlePerson person = getDBHelper().getPersonByFamilySearchId(fsid);
        if (person!=null) addToSyncQ(person, QDepth);
        else {
            fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
            waitForAuth();
            Person fsPerson = remoteService.getPerson(fsid, true);
            person = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
            getDBHelper().persistLittlePerson(person);
            fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
        }
        return person;
    }

    public List<LittlePerson> searchPeople(String givenName, String surname) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (givenName!=null && !givenName.isEmpty()) params.put(DBHelper.COL_GIVEN_NAME, givenName+"%");
        if (surname!=null && !surname.isEmpty()) params.put(DBHelper.COL_NAME, "% " + surname + "%");
        return getDBHelper().search(params);
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person) throws Exception {
        return getFamilyMembers(person, true);
    }

    public List<LittlePerson> getFamilyMembers(LittlePerson person, boolean loadSpouse) throws Exception {
        List<LittlePerson> family = getDBHelper().getRelativesForPerson(person.getId(), loadSpouse);
        if ((family==null || family.size()==0) && (person.isHasSpouses()==null || person.isHasParents()==null || person.isHasChildren()==null)) {
            fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
            family = getFamilyMembersFromRemoteService(person, loadSpouse);
            fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
        } else {
            addToSyncQ(family, 1);
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
                    for (LittlePerson parent : spouseFamily) {
                        if ("parent".equals(parent.getRelationship()) && !family.contains(parent)) {
                            spouseParents.add(parent);
                            p.setHasParents(true);
                        }
                    }
                }
            }
            family.addAll(spouseParents);
        }
        return family;
    }

    public List<LittlePerson> getParentsFromRemoteService(LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        List<Relationship> closeRelatives = remoteService.getParents(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
        }

        return family;
    }

    public List<LittlePerson> getChildrenFromRemoteService(LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        List<Relationship> closeRelatives = remoteService.getChildren(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
        }

        return family;
    }

    public List<LittlePerson> getSpousesFromRemoteService(LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        List<Relationship> closeRelatives = remoteService.getSpouses(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
        }

        return family;
    }

    private List<LittlePerson> processRelatives(List<Relationship> closeRelatives, LittlePerson person) throws Exception {
        boolean personChanged = false;
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
                    org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                    rel.setId2(person.getId());
                    if (r.getKnownType()== RelationshipType.Couple) {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                        relative.setRelationship("spouse");
                        relative.setHasSpouses(true);
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge());
                        }
                        if (person.isHasSpouses()==null || person.isHasSpouses()==false) {
                            person.setHasSpouses(true);
                            personChanged = true;
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
                            personChanged = true;
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
                    org.finlayfamily.littlefamily.data.Relationship rel = new org.finlayfamily.littlefamily.data.Relationship();
                    rel.setId1(person.getId());
                    if (r.getKnownType()== RelationshipType.Couple) {
                        rel.setType(org.finlayfamily.littlefamily.data.RelationshipType.SPOUSE);
                        relative.setRelationship("spouse");
                        relative.setHasSpouses(true);
                        if (relative.getAge()==null && person.getAge()!=null) {
                            relative.setAge(person.getAge());
                        }
                        if (person.isHasSpouses()==null || person.isHasSpouses()==false) {
                            person.setHasSpouses(true);
                            personChanged = true;
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
                        if (person.isHasChildren()==null || person.isHasChildren()==false) {
                            person.setHasChildren(true);
                            personChanged = true;
                        }
                    }
                    getDBHelper().persistLittlePerson(relative);
                    rel.setId2(relative.getId());
                    getDBHelper().persistRelationship(rel);
                }
            }
        }
        if (personChanged) {
            getDBHelper().persistLittlePerson(person);
        }
        return family;
    }

    public List<LittlePerson> getParents(LittlePerson person) throws Exception {
        List<LittlePerson> parents = getDBHelper().getParentsForPerson(person.getId());
        if (parents==null || parents.size()==0) {
            if (person.isHasParents()==null || person.isHasParents()) {
                fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                getParentsFromRemoteService(person);
                parents = getDBHelper().getParentsForPerson(person.getId());
                if (parents == null) {
                    parents = new ArrayList<>();
                }
                if (parents.size()==0) {
                    person.setHasParents(false);
                    getDBHelper().persistLittlePerson(person);
                } else {
                    person.setHasParents(true);
                    getDBHelper().persistLittlePerson(person);
                }
                fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
            }
        } else {
            addToSyncQ(parents, 1);
        }
        return parents;
    }

    public List<LittlePerson> getChildren(LittlePerson person) throws Exception {
        List<LittlePerson> children = getDBHelper().getChildrenForPerson(person.getId());
        if (children==null || children.size()==0) {
            if (person.isHasChildren()==null || person.isHasChildren()) {
                fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                getChildrenFromRemoteService(person);
                children = getDBHelper().getChildrenForPerson(person.getId());
                if (children == null) {
                    children = new ArrayList<>();
                }
                if (children.size() == 0) {
                    person.setHasChildren(false);
                    getDBHelper().persistLittlePerson(person);
                } else {
                    person.setHasChildren(true);
                    getDBHelper().persistLittlePerson(person);
                }
                fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
            }
        } else {
            addToSyncQ(children, 2);
        }
        return children;
    }

    public List<LittlePerson> getSpouses(LittlePerson person) throws Exception {
        List<LittlePerson> spouses = getDBHelper().getSpousesForPerson(person.getId());
        if (spouses==null || spouses.size()==0) {
            if (person.isHasSpouses()==null || person.isHasSpouses()) {
                fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                getSpousesFromRemoteService(person);
                spouses = getDBHelper().getSpousesForPerson(person.getId());
                if (spouses == null) {
                    spouses = new ArrayList<>();
                }
                if (spouses.size() == 0) {
                    person.setHasSpouses(false);
                    getDBHelper().persistLittlePerson(person);
                } else {
                    person.setHasSpouses(true);
                    getDBHelper().persistLittlePerson(person);
                }
                fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
            }
        } else {
            addToSyncQ(spouses, 1);
        }
        return spouses;
    }

    public List<Media> getMediaForPerson(LittlePerson person) throws Exception {
        List<Media> media = getDBHelper().getMediaForPerson(person.getId());
        if (media==null || media.size()==0) {
            media = new ArrayList<>();
            try {
                fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                waitForAuth();
                List<SourceDescription> sds = remoteService.getPersonMemories(person.getFamilySearchId(), true);
                if (sds!=null) {
                    for (SourceDescription sd : sds) {
                        Media med = getDBHelper().getMediaByFamilySearchId(sd.getId());
                        if (med == null) {
                            List<Link> links = sd.getLinks();
                            if (links != null) {
                                for (Link link : links) {
                                    try {
                                        if (link.getRel() != null && link.getRel().equals("image")) {
                                            med = new Media();
                                            med.setType("photo");
                                            med.setFamilySearchId(sd.getId());
                                            String localPath = DataHelper.downloadFile(link.getHref().toString(), person.getFamilySearchId(), DataHelper.lastPath(link.getHref().toString()), remoteService, context);
                                            if (localPath != null) {
                                                med.setLocalPath(localPath);
                                                getDBHelper().persistMedia(med);
                                                media.add(med);
                                                Tag tag = new Tag();
                                                tag.setMediaId(med.getId());
                                                tag.setPersonId(person.getId());
                                                getDBHelper().persistTag(tag);
                                            }
                                        }
                                    } catch(Exception e) {
                                        Log.e(this.getClass().getSimpleName(), "Error loading image ", e);
                                    }
                                }
                            }
                        }
                    }
                }
                fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
            } catch(RemoteServiceSearchException e) {
                Log.e(this.getClass().getSimpleName(), "error", e);
            }
        }
        return media;
    }

    public String getEncryptedProperty(String property) throws Exception {
        String value = getDBHelper().getProperty(property);
        InputStream is = new ByteArrayInputStream( value.getBytes() );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String uid = getDBHelper().getProperty(DBHelper.UUID_PROPERTY);
        AES.decrypt(uid.toCharArray(), is, out);
        return out.toString();
    }

    public void saveEncryptedProperty(String property, String value) throws Exception {
        InputStream is = new ByteArrayInputStream( value.getBytes() );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String uid = getDBHelper().getProperty(DBHelper.UUID_PROPERTY);
        AES.encrypt(128, uid.toCharArray(), is, out);
        getDBHelper().saveProperty(property, out.toString());
    }
}
