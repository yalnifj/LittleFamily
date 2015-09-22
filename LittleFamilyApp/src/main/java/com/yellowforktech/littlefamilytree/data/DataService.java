package com.yellowforktech.littlefamilytree.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yellowforktech.littlefamilytree.activities.tasks.AuthTask;
import com.yellowforktech.littlefamilytree.db.DBHelper;
import com.yellowforktech.littlefamilytree.remote.AES;
import com.yellowforktech.littlefamilytree.remote.RemoteResult;
import com.yellowforktech.littlefamilytree.remote.RemoteService;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.remote.familysearch.FamilySearchService;
import com.yellowforktech.littlefamilytree.remote.phpgedview.PGVService;

import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.types.RelationshipType;

import java.io.File;
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
    private Object cellLock = new Object();

    private SyncThread syncer;
    private boolean authenticating = false;
    private String serviceType = null;
    private Object pauseLock = new Object();
    private boolean syncPaused = false;

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

    public boolean isAuthenticating() {
        return authenticating;
    }

    public void pauseSync() {
        synchronized (pauseLock) {
            syncPaused = true;
        }
    }

    public void resumeSync() {
        synchronized (pauseLock) {
            syncPaused = false;
            pauseLock.notifyAll();
        }
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
        if (remoteService==null || remoteService.getSessionId() == null) {
            syncPaused = true;
        }
        if (syncer==null) {
            syncer = new SyncThread();
            syncer.start();
        }
    }

    public Context getContext() {
        return context;
    }

    public void autoLogin()
	{
		try {
		String token = getEncryptedProperty(serviceType + SERVICE_TOKEN);
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
            Boolean syncBackground = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sync_background", true);
            if (syncBackground) {
                syncer = new SyncThread();
                syncer.start();
            }
        }
        if (this.remoteService==null || remoteService.getSessionId() == null) {
            syncPaused = true;
        } else {
            syncPaused = false;
        }

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("sync_background")) {
                    if (sharedPreferences.getBoolean("sync_background", true)) {
                        if (syncer == null || (!running && !syncer.isAlive())) {
                            syncer = new SyncThread();
                            syncer.start();
                        }
                    } else {
                        if (syncer!=null) {
                            running = false;
                        }
                    }
                } else if (key.equals("sync_cellular")) {
                    if (sharedPreferences.getBoolean("sync_cellular", false) || !isConnectedMobile()) {
                        synchronized (cellLock) {
                            cellLock.notifyAll();
                        }
                    }
                }
            }
        });
    }

    public boolean hasData() throws Exception {
        //-- check succesful login
        waitForAuth();
        if (remoteService.getSessionId() == null) return false;
        //-- check for people
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

    public NetworkInfo getNetworkInfo(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public boolean isConnectedMobile(){
        NetworkInfo info = getNetworkInfo();
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
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
                synchronized (syncQ) {
                    List<Integer> ids = getDBHelper().getSyncQ();
                    if (ids != null) {
                        List<Integer> toClear = null;
                        //-- limit previous sync q to 25 on startup
                        Log.d("SyncThread", "SyncQ has "+ids.size()+" starting ids");
                        if (ids.size() > 25) {
                            toClear = ids.subList(25, ids.size());
                            ids = ids.subList(0, 25);
                        }
                        for (Integer id : ids) {
                            if (id != null) {
                                LittlePerson person = getDBHelper().getPersonById(id);
                                if (person != null) {
                                    ThreadPerson tp = new ThreadPerson();
                                    tp.depth = 5;
                                    tp.person = person;
                                    syncQ.add(tp);
                                }
                            }
                        }

                        if (toClear!=null) {
                            for (Integer id : toClear) {
                                if (id != null) {
                                    getDBHelper().removeFromSyncQ(id);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SyncThread", "Error reading Q from DB", e);
            }

            while(running) {
                Boolean syncCell = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sync_cellular", false);
                while (!syncCell && isConnectedMobile()) {
                    try {
                        Log.d("SyncThread", "Waiting for Wifi");
                        synchronized (cellLock) {
                            cellLock.wait(60000); //-- check every minute for network state change
                        }
                        syncCell = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sync_cellular", false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while(syncPaused) {
                    try {
                        Log.d("SyncThread", "Sync paused");
                        synchronized (pauseLock) {
                            pauseLock.wait(8000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("SyncThread", "Sync resumed");

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
                        Log.d("SyncThread", "Sync Q has " + syncQ.size() + " people in it.");
                        if (person.getLastSync().before(cal.getTime())) {
                            Log.d("SyncThread", "Synchronizing person " + person.getId() + " " + person.getFamilySearchId() + " " + person.getName());
                            Entry entry = remoteService.getLastChangeForPerson(person.getFamilySearchId());
                            if (entry != null) {
                                Log.d("SyncThread", "Local date=" + person.getLastSync() + " remote date=" + entry.getUpdated());
                            }
                            if (entry == null || entry.getUpdated().after(person.getLastSync())) {
                                person = syncPerson(person);
                                if (person == null) {
                                    continue;
                                }
                                try {
                                    Thread.sleep(10000);  //-- don't bombard the server
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                person.setLastSync(new java.util.Date());
                                getDBHelper().persistLittlePerson(person);
                                try {
                                    Thread.sleep(5000);  //-- don't bombard the server
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        //-- make sure person has a tree level
                        if (tp.person.getTreeLevel()==null) {
                            List<LittlePerson> dbChildren = getDBHelper().getChildrenForPerson(tp.person.getId());
                            if (dbChildren!=null && dbChildren.size()>0) {
                                for (LittlePerson child : dbChildren) {
                                    if (child.getTreeLevel()!=null) {
                                        tp.person.setTreeLevel(child.getTreeLevel() + 1);
                                        getDBHelper().persistLittlePerson(tp.person);
                                        break;
                                    }
                                }
                            }
                            if (tp.person.getTreeLevel()==null) {
                                List<LittlePerson> dbSpouses = getDBHelper().getSpousesForPerson(tp.person.getId());
                                if (dbSpouses!=null && dbSpouses.size()>0) {
                                    for (LittlePerson spouse : dbSpouses) {
                                        if (spouse.getTreeLevel()!=null) {
                                            tp.person.setTreeLevel(spouse.getTreeLevel());
                                            getDBHelper().persistLittlePerson(tp.person);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (tp.person.getTreeLevel()==null) {
                                List<LittlePerson> dbParents = getDBHelper().getParentsForPerson(tp.person.getId());
                                if (dbParents!=null && dbParents.size()>0) {
                                    for (LittlePerson parent : dbParents) {
                                        if (parent.getTreeLevel()!=null) {
                                            tp.person.setTreeLevel(parent.getTreeLevel()-1);
                                            getDBHelper().persistLittlePerson(tp.person);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        //-- force load of family members if we haven't previously loaded them
                        //--- allows building the tree in the background
                        if (tp.depth < 6) {
                            if (tp.person.isHasParents() == null) {
                                List<LittlePerson> dbParents = getDBHelper().getParentsForPerson(tp.person.getId());
                                if (dbParents == null || dbParents.size() == 0) {
                                    Log.d("SyncThread", "Synchronizing parents for " + tp.person.getId() + " " + tp.person.getFamilySearchId() + " " + tp.person.getName());
                                    List<LittlePerson> parents = getParentsFromRemoteService(tp.person);
                                    for (LittlePerson p : parents) {
                                        addToSyncQ(p, tp.depth + 1);
                                    }
                                    try {
                                        Thread.sleep(10000);  //-- don't bombard the server
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    tp.person.setHasParents(true);
                                    getDBHelper().persistLittlePerson(tp.person);
                                }
                            }
                        }

                        //-- force load descendants of lower levels, picks up aunts, uncles, cousins, grandchildren
                        if (tp.person.getTreeLevel()!=null && tp.person.getTreeLevel()<2) {
                            if (tp.person.isHasChildren()==null) {
                                List<LittlePerson> dbChildren = getDBHelper().getChildrenForPerson(tp.person.getId());
                                if (dbChildren == null || dbChildren.size() == 0) {
                                    Log.d("SyncThread", "Synchronizing children for " + tp.person.getId() + " " + tp.person.getFamilySearchId() + " " + tp.person.getName());
                                    List<LittlePerson> children = getChildrenFromRemoteService(tp.person);
                                    for (LittlePerson p : children) {
                                        addToSyncQ(p, tp.depth - 1);
                                    }
                                    try {
                                        Thread.sleep(10000);  //-- don't bombard the server
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    tp.person.setHasChildren(true);
                                    getDBHelper().persistLittlePerson(tp.person);
                                }
                            }

                            if (tp.person.isHasSpouses()==null) {
                                List<LittlePerson> dbSpouses = getDBHelper().getSpousesForPerson(tp.person.getId());
                                if (dbSpouses == null || dbSpouses.size() == 0) {
                                    Log.d("SyncThread", "Synchronizing spouses for " + tp.person.getId() + " " + tp.person.getFamilySearchId() + " " + tp.person.getName());
                                    List<LittlePerson> spouses = getSpousesFromRemoteService(tp.person);
                                    for (LittlePerson p : spouses) {
                                        addToSyncQ(p, tp.depth);
                                    }
                                    try {
                                        Thread.sleep(10000);  //-- don't bombard the server
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    tp.person.setHasSpouses(true);
                                    getDBHelper().persistLittlePerson(tp.person);
                                }
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

    public LittlePerson syncPerson(LittlePerson person) throws Exception {
        Person fsPerson = remoteService.getPerson(person.getFamilySearchId(), false);
        if (fsPerson.getTransientProperty("deleted")!=null) {
            getDBHelper().deletePersonById(person.getId());
            return null;
        } else {
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
                List<com.yellowforktech.littlefamilytree.data.Relationship> oldRelations = getDBHelper().getRelationshipsForPerson(person.getId());
                for (Relationship r : closeRelatives) {
                    com.yellowforktech.littlefamilytree.data.RelationshipType type = com.yellowforktech.littlefamilytree.data.RelationshipType.PARENTCHILD;
                    if (r.getKnownType() == RelationshipType.Couple) {
                        type = com.yellowforktech.littlefamilytree.data.RelationshipType.SPOUSE;
                    }
                    com.yellowforktech.littlefamilytree.data.Relationship rel = syncRelationship(r.getPerson1().getResourceId(), r.getPerson2().getResourceId(), type);
                    if (rel != null) {
                        oldRelations.remove(rel);
                    }
                }

                for (com.yellowforktech.littlefamilytree.data.Relationship rel : oldRelations) {
                    getDBHelper().deleteRelationshipById(rel.getId());
                }
            }

            List<SourceDescription> sds = remoteService.getPersonMemories(person.getFamilySearchId(), true);
            boolean foundMedia = false;
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
                                        foundMedia = true;
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
                        foundMedia = true;
                        oldMedia.remove(med);
                    }
                }

                for (Media old : oldMedia) {
                    getDBHelper().deleteMediaById(old.getId());
                }

                if (foundMedia) {
                    if (person.isHasMedia() == null || person.isHasMedia() == false) {
                        person.setHasMedia(true);
                        getDBHelper().persistLittlePerson(person);
                    }
                } else {
                    if (person.isHasMedia() == null || person.isHasMedia() == true) {
                        person.setHasMedia(false);
                        getDBHelper().persistLittlePerson(person);
                    }
                }
            }
        }
        return person;
    }

    private com.yellowforktech.littlefamilytree.data.Relationship syncRelationship(String fsid1, String fsid2, com.yellowforktech.littlefamilytree.data.RelationshipType type) throws Exception {
        LittlePerson person = getDBHelper().getPersonByFamilySearchId(fsid1);
        if (person==null) {
            try {
                Thread.sleep(5000);  //-- don't bombard the server
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Person fsPerson = remoteService.getPerson(fsid1, true);
            person = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
            getDBHelper().persistLittlePerson(person);
        }
        LittlePerson relative = getDBHelper().getPersonByFamilySearchId(fsid2);
        if (relative==null) {
            try {
                Thread.sleep(5000);  //-- don't bombard the server
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Person fsPerson2 = remoteService.getPerson(fsid2, true);
            relative = DataHelper.buildLittlePerson(fsPerson2, context, remoteService, true);
            getDBHelper().persistLittlePerson(relative);
        }
        if (person !=null && relative!=null) {
            boolean personChanged = false;
            boolean relativeChanged = false;
            com.yellowforktech.littlefamilytree.data.Relationship rel = getDBHelper().getRelationship(person.getId(), relative.getId(), type);
            if (rel==null) {
                rel = new com.yellowforktech.littlefamilytree.data.Relationship();
                rel.setId1(person.getId());
                rel.setId2(relative.getId());
                rel.setType(type);
                getDBHelper().persistRelationship(rel);
            }

            if (rel.getType() == com.yellowforktech.littlefamilytree.data.RelationshipType.SPOUSE) {
                if (person.isHasSpouses()==null || person.isHasSpouses()==false) {
                    person.setHasSpouses(true);
                    personChanged = true;
                }
                if (relative.isHasSpouses()==null || relative.isHasSpouses()==false) {
                    relative.setHasSpouses(true);
                    relativeChanged = true;
                }
                if (person.getTreeLevel()==null && relative.getTreeLevel()!=null) {
                    person.setTreeLevel(relative.getTreeLevel());
                    personChanged = true;
                }
                if (relative.getTreeLevel()==null && person.getTreeLevel()!=null) {
                    relative.setTreeLevel(person.getTreeLevel());
                    relativeChanged = true;
                }
                if (person.getAge() == null && relative.getAge() != null) {
                    person.setAge(relative.getAge());
                    personChanged = true;
                }
                if (relative.getAge() == null && person.getAge() != null) {
                    relative.setAge(person.getAge());
                    relativeChanged = true;
                }
            } else {
                if (person.isHasChildren()==null || person.isHasChildren()==false) {
                    person.setHasChildren(true);
                    personChanged = true;
                }
                if (person.getTreeLevel()==null && relative.getTreeLevel()!=null) {
                    person.setTreeLevel(relative.getTreeLevel() + 1);
                    personChanged = true;
                }
                if (relative.getAge() == null && person.getAge() != null) {
                    relative.setAge(person.getAge() - 25);
                    relativeChanged = true;
                }


                if (relative.isHasParents()==null || relative.isHasParents()==false) {
                    relative.setHasParents(true);
                    relativeChanged = true;
                }
                if (relative.getTreeLevel()==null && person.getTreeLevel()!=null) {
                    relative.setTreeLevel(person.getTreeLevel()-1);
                    relativeChanged = true;
                }
                if (person.getAge() == null && relative.getAge() != null) {
                    person.setAge(relative.getAge() + 25);
                    personChanged = true;
                }
            }

            if (personChanged) {
                getDBHelper().persistLittlePerson(person);
            }
            if (relativeChanged) {
                getDBHelper().persistLittlePerson(relative);
            }

            return rel;
        }
        return null;
    }

    public void addToSyncQ(LittlePerson person, int depth) throws Exception {
        Calendar cal = Calendar.getInstance();
        String syncDelayStr = PreferenceManager.getDefaultSharedPreferences(context).getString("sync_delay", "1");
        int syncDelay = Integer.parseInt(syncDelayStr);
        cal.add(Calendar.HOUR, -1 * syncDelay);
        if (person.isHasParents()==null) {
            List<LittlePerson> parents = getDBHelper().getParentsForPerson(person.getId());
            if (parents!=null && parents.size()>0) {
                person.setHasParents(true);
                getDBHelper().persistLittlePerson(person);
            }
        }
        if (person.isHasParents()==null || person.getLastSync().before(cal.getTime()) || person.getTreeLevel()==null
                || (person.getTreeLevel()<=1 && person.isHasChildren()==null)) {
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

    public void fireStatusUpdate(String status) {
        for (DataNetworkStateListener listener : listeners) {
            listener.statusUpdate(status);
        }
    }

    public LittlePerson getDefaultPerson() throws Exception {
        LittlePerson person = getDBHelper().getFirstPerson();

        if (person==null) {
            fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
            waitForAuth();
            Person fsPerson = remoteService.getCurrentPerson();
            person = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
            person.setTreeLevel(0);
            getDBHelper().persistLittlePerson(person);
            fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
        } else {
            if (person.getTreeLevel()==null) {
                person.setTreeLevel(0);
                getDBHelper().persistLittlePerson(person);
            }
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
            if (person.isHasParents()==null) {
                if (family!=null && family.size()>0)
                    person.setHasParents(true);
                else
                    person.setHasParents(false);
                getDBHelper().persistLittlePerson(person);
            }
        } else {
            person.setHasParents(false);
            getDBHelper().persistLittlePerson(person);
        }

        return family;
    }

    public List<LittlePerson> getChildrenFromRemoteService(LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        List<Relationship> closeRelatives = remoteService.getChildren(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
            if (person.isHasChildren()==null) {
                if (family!=null && family.size()>0)
                    person.setHasChildren(true);
                else
                    person.setHasChildren(false);
                getDBHelper().persistLittlePerson(person);
            }
        } else {
            person.setHasChildren(false);
            getDBHelper().persistLittlePerson(person);
        }

        return family;
    }

    public List<LittlePerson> getSpousesFromRemoteService(LittlePerson person) throws Exception {
        List<LittlePerson> family = new ArrayList<>();
        waitForAuth();
        List<Relationship> closeRelatives = remoteService.getSpouses(person.getFamilySearchId(), true);
        if (closeRelatives!=null) {
            family = processRelatives(closeRelatives, person);
            if (person.isHasSpouses()==null) {
                if (family!=null && family.size()>0)
                    person.setHasSpouses(true);
                else
                    person.setHasSpouses(false);
                getDBHelper().persistLittlePerson(person);
            }
        } else {
            person.setHasSpouses(false);
            getDBHelper().persistLittlePerson(person);
        }

        return family;
    }

    private List<LittlePerson> processRelatives(List<Relationship> closeRelatives, LittlePerson person) throws Exception {
        boolean personChanged = false;
        List<LittlePerson> family = new ArrayList<>();
        for(Relationship r : closeRelatives) {
            LittlePerson person1 = getDBHelper().getPersonByFamilySearchId(r.getPerson1().getResourceId());
            LittlePerson person2 = getDBHelper().getPersonByFamilySearchId(r.getPerson2().getResourceId());

            if (person1 == null) {
                Person fsPerson = remoteService.getPerson(r.getPerson1().getResourceId(), true);
                //-- check for person by alternate id
                if (!fsPerson.getId().equals(r.getPerson1().getResourceId())) {
                    person1 = getDBHelper().getPersonByFamilySearchId(fsPerson.getId());
                }
                if (person1 == null) {
                    fireStatusUpdate("Processing " + fsPerson.getFullName());
                    person1 = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
                }
            }

            if (person2 == null) {
                Person fsPerson = remoteService.getPerson(r.getPerson2().getResourceId(), true);
                //-- check for person by alternate id
                if (!fsPerson.getId().equals(r.getPerson2().getResourceId())) {
                    person2 = getDBHelper().getPersonByFamilySearchId(fsPerson.getId());
                }
                if (person2 == null) {
                    fireStatusUpdate("Processing " + fsPerson.getFullName());
                    person2 = DataHelper.buildLittlePerson(fsPerson, context, remoteService, true);
                }
            }

            if (person1 != null && person2 != null) {
                com.yellowforktech.littlefamilytree.data.Relationship rel = new com.yellowforktech.littlefamilytree.data.Relationship();
                if (r.getKnownType() == RelationshipType.Couple) {
                    rel.setType(com.yellowforktech.littlefamilytree.data.RelationshipType.SPOUSE);
                    person1.setHasSpouses(true);
                    if (person2.getTreeLevel()==null && person1.getTreeLevel()!=null) {
                        person2.setTreeLevel(person1.getTreeLevel());
                    }
                    if (person2.getAge() == null && person1.getAge() != null) {
                        person2.setAge(person1.getAge());
                    }
                    person2.setHasSpouses(true);
                    if (person1.getTreeLevel()==null && person2.getTreeLevel()!=null) {
                        person1.setTreeLevel(person2.getTreeLevel());
                    }
                    if (person1.getAge() == null && person2.getAge() != null) {
                        person1.setAge(person2.getAge());
                    }
                } else {
                    rel.setType(com.yellowforktech.littlefamilytree.data.RelationshipType.PARENTCHILD);
                    person1.setHasChildren(true);
                    if (person2.getTreeLevel()==null && person1.getTreeLevel()!=null) {
                        person2.setTreeLevel(person1.getTreeLevel()-1);
                    }
                    if (person2.getAge() == null && person1.getAge() != null) {
                        person2.setAge(person1.getAge() - 25);
                    }
                    person2.setHasParents(true);
                    if (person1.getTreeLevel()==null && person2.getTreeLevel()!=null) {
                        person1.setTreeLevel(person2.getTreeLevel()+1);
                    }
                    if (person1.getAge() == null && person2.getAge() != null) {
                        person1.setAge(person2.getAge() + 25);
                    }
                }
                getDBHelper().persistLittlePerson(person1);
                getDBHelper().persistLittlePerson(person2);
                rel.setId1(person1.getId());
                rel.setId2(person2.getId());
                getDBHelper().persistRelationship(rel);

                if (!person.equals(person1) && !family.contains(person1)) family.add(person1);
                if (!person.equals(person2) && !family.contains(person2)) family.add(person2);
            }
        }

        return family;
    }

    public List<LittlePerson> getParents(LittlePerson person) throws Exception {
        List<LittlePerson> parents = getDBHelper().getParentsForPerson(person.getId());
        if (parents==null || parents.size()==0) {
            if (person.isHasParents()==null || person.isHasParents()) {
                fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                parents = getParentsFromRemoteService(person);
                //parents = getDBHelper().getParentsForPerson(person.getId());
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
            if (person.isHasParents()==null || !person.isHasParents()) {
                person.setHasParents(true);
                getDBHelper().persistLittlePerson(person);
            }
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
            if (person.isHasChildren()==null || !person.isHasChildren()) {
                person.setHasChildren(true);
                getDBHelper().persistLittlePerson(person);
            }
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
            if (person.isHasSpouses()==null || !person.isHasSpouses()) {
                person.setHasSpouses(true);
                getDBHelper().persistLittlePerson(person);
            }
            addToSyncQ(spouses, 1);
        }
        return spouses;
    }

    public List<Media> getMediaForPerson(LittlePerson person) throws Exception {
        List<Media> media = getDBHelper().getMediaForPerson(person.getId());
        if (person.isHasMedia()==null) {
            if (media == null || media.size() == 0) {
                media = new ArrayList<>();
                try {
                    boolean mediaFound = false;
                    fireNetworkStateChanged(DataNetworkState.REMOTE_STARTING);
                    waitForAuth();
                    List<SourceDescription> sds = remoteService.getPersonMemories(person.getFamilySearchId(), true);
                    if (sds != null) {
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
                                                String folderName = person.getFamilySearchId();
                                                String fileName = DataHelper.lastPath(link.getHref().toString());
                                                File localFile = DataHelper.getImageFile(folderName, fileName, context);
                                                String localPath = null;
                                                if (!localFile.exists()) {
                                                    localPath = DataHelper.downloadFile(link.getHref().toString(), folderName, fileName, remoteService, context);
                                                } else {
                                                    localPath = localFile.getAbsolutePath();
                                                }
                                                if (localPath != null) {
                                                    mediaFound = true;
                                                    med.setLocalPath(localPath);
                                                    getDBHelper().persistMedia(med);
                                                    media.add(med);
                                                    Tag tag = new Tag();
                                                    tag.setMediaId(med.getId());
                                                    tag.setPersonId(person.getId());
                                                    getDBHelper().persistTag(tag);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(this.getClass().getSimpleName(), "Error loading image ", e);
                                        }
                                    }
                                }
                            } else {
                                media.add(med);
                            }
                        }
                    }
                    if (mediaFound) {
                        person.setHasMedia(true);
                    } else {
                        person.setHasMedia(false);
                    }
                    getDBHelper().persistLittlePerson(person);
                    fireNetworkStateChanged(DataNetworkState.REMOTE_FINISHED);
                } catch (RemoteServiceSearchException e) {
                    Log.e(this.getClass().getSimpleName(), "error", e);
                }
            }
        }
        return media;
    }

    public String getEncryptedProperty(String property) throws Exception {
        String value = getDBHelper().getProperty(property);
        String uid = getDBHelper().getProperty(DBHelper.UUID_PROPERTY);
        value = AES.decrypt(128, uid, value);
        return value;
    }

    public void saveEncryptedProperty(String property, String value) throws Exception {
        String uid = getDBHelper().getProperty(DBHelper.UUID_PROPERTY);
        String encrypted = AES.encrypt(128, uid, value);
        //String descrypted = AES.decrypt(128, uid, encrypted);
        getDBHelper().saveProperty(property, encrypted);
    }
}
