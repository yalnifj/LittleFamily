package com.yellowforktech.littlefamilytree.remote.familygraph;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.familygraph.android.FamilyGraph;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.remote.RemoteResult;
import com.yellowforktech.littlefamilytree.remote.RemoteService;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceBase;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.gedcomx.Gedcomx;
import org.gedcomx.atom.Entry;
import org.gedcomx.atom.Feed;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.rt.GedcomxSerializer;
import org.gedcomx.source.SourceDescription;
import org.simpleframework.xml.Serializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Parents on 12/29/2014.
 */
public class MyHeritageService extends RemoteServiceBase implements RemoteService {
    protected static final String CLIENT_ID = "0d9d29c39d0ded7bd6a9e334e5f673a7";
    protected static final String CLIENT_SECRET = "9021b2dcdb4834bd12a491349f61cb27";

    private static final String FS_PLATFORM_PATH = "https://familygraph.myheritage.com/";

    private static final String OAUTH2_PATH = "https://accounts.myheritage.com/oauth2/token";

    private static final String TAG = MyHeritageService.class.getSimpleName();

    private String sessionId = null;
    private Person currentPerson = null;
    private Map<String, Person> personCache;
    private int delayCount = 0;

    private FamilyGraph familyGraph;

    public MyHeritageService() {
        personCache = new HashMap<>();
        familyGraph = new FamilyGraph(CLIENT_ID);
    }

    public FamilyGraph getFamilyGraph() {
        return familyGraph;
    }

    @Override
    public RemoteResult authenticate(String username, String password) throws RemoteServiceSearchException {
        //-- OAuth2
        Uri action = Uri.parse(OAUTH2_PATH);
        Bundle params = new Bundle();
        params.putString("grant_type", "password");
        params.putString("client_id", CLIENT_ID);
        params.putString("username", username);
        params.putString("password", password);
        Bundle headers = new Bundle();
        //headers.putString("Authorization", "Basic " + encodedAuthToken);

        RemoteResult data = getRestDataNoRetry(METHOD_POST, action, params, headers);
        if (data!=null) {
            if (data.isSuccess()) {
                String json = data.getData();
                int pos1 = json.indexOf("access_token");
                if (pos1>0) {
                    pos1 = json.indexOf(":", pos1);
                    if (pos1>0) {
                        int pos2 = json.indexOf("\"", pos1+2);
                        this.sessionId = json.substring(pos1+2, pos2);
                        Log.i(TAG, "access_token: " + sessionId);
                    }
                }
            } else {
                this.sessionId = null;
            }
        }

        return data;
    }

    @Override
    public RemoteResult authWithToken(String token) throws RemoteServiceSearchException {
        String[] parts = token.split(":", 2);
        String username = parts[0];
        String password = parts[1];
        return authenticate(username, password);
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public String createEncodedAuthToken(String username, String password) {
        return username+":"+password;
    }

    @Override
    public Person getCurrentPerson() throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }
        if (currentPerson==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/current-person");
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();

            RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
            if (result!=null) {
                if (result.isSuccess()) {
                    Serializer serializer = GedcomxSerializer.create();
                    try {
                        Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                        if (doc.getPersons() != null && doc.getPersons().size() > 0) {
                            currentPerson = doc.getPersons().get(0);
                            personCache.put(currentPerson.getId(), currentPerson);
                            Log.i(TAG, "getCurrentPerson " + doc.getPersons().size() + ": " + currentPerson.getId());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                } else {
                    //-- check status and retry if possible
                    if (handleStatusCodes(result)) {
                        return getCurrentPerson();
                    } else {
                        throw new RemoteServiceSearchException("Error reading current person: "+result.getData(), result.getStatusCode());
                    }
                }
            }
        }
        return currentPerson;
    }

    @Override
    public Person getPerson(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }
        if (!checkCache || personCache.get(personId)==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId);
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();

            RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
            if (result!=null) {
                if (result.isSuccess()) {
                    Serializer serializer = GedcomxSerializer.create();
                    try {
                        Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                        if (doc.getPersons() != null && doc.getPersons().size() > 0) {
                            for (Person p : doc.getPersons()) {
                                personCache.put(p.getId(), p);
                            }
                            Log.i(TAG, "getPerson " + doc.getPersons().size() + ": " + personId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                }
                //-- person merged to a new id
                else if(result.getStatusCode()==301) {
                    Header[] responseHeaders = result.getResponse().getHeaders("X-Entity-Forwarded-Id");
                    if (responseHeaders!=null && responseHeaders.length>0) {
                        Header header = responseHeaders[0];
                        String newFSId = header.getValue();
                        Person person = getPerson(newFSId, checkCache);
                        personCache.put(personId, person);
                    }
                }
                //-- not found
                else if(result.getStatusCode()==404) {
                    personCache.remove(personId);
                    Person person = new Person();
                    person.setId(personId);
                    person.setTransientProperty("deleted", true);
                    return person;
                }
                //-- deleted
                else if(result.getStatusCode()==410) {
                    personCache.remove(personId);
                    Person person = new Person();
                    person.setId(personId);
                    person.setTransientProperty("deleted", true);
                    return person;
                }
                else {
                    //-- check status and retry if possible
                    if (handleStatusCodes(result)) {
                        return getPerson(personId, checkCache);
                    }
                }
            }
        }
        return personCache.get(personId);
    }

    @Override
    public Entry getLastChangeForPerson(String personId) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/changes");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/atom+xml");
        Bundle params = new Bundle();
        params.putInt("count", 1);

        Entry entry = null;
        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Feed feed = serializer.read(Feed.class, result.getData());
                    if (feed.getEntries() != null && feed.getEntries().size() > 0) {
                        for (Entry e : feed.getEntries()) {
                            if (entry==null || e.getUpdated().after(entry.getUpdated())) {
                                entry = e;
                            }
                        }
                        Log.i(TAG, "getLastChangeForPerson " + feed.getEntries().size() + ": " + personId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            }
            //-- person merged to a new id
            else if(result.getStatusCode()==301) {
                Header[] responseHeaders = result.getResponse().getHeaders("X-Entity-Forwarded-Id");
                if (responseHeaders!=null && responseHeaders.length>0) {
                    Header header = responseHeaders[0];
                    String newFSId = header.getValue();
                    return getLastChangeForPerson(newFSId);
                }
            }
            else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getLastChangeForPerson(personId);
                }
            }
        }
        return entry;
    }

    @Override
    public Link getPersonPortrait(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/portraits");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getSourceDescriptions() != null && doc.getSourceDescriptions().size() > 0) {
                        for (SourceDescription sd : doc.getSourceDescriptions()) {
                            List<Link> links = sd.getLinks();
                            for (Link link : links) {
                                if (link.getRel() != null && link.getRel().equals("image-thumbnail")) {
                                    return link;
                                }
                            }
                        }
                        Log.i(TAG, "getPersonPortrait " + doc.getPersons().size() + ": " + personId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            }
            else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getPersonPortrait(personId, checkCache);
                }
            }
        }
        return null;
    }

    @Override
    public List<Relationship> getCloseRelatives(boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        if (getCurrentPerson()==null) {
            throw new RemoteServiceSearchException("Unable to get current person from MyHeritage", 0);
        }

        return getCloseRelatives(currentPerson.getId(), checkCache);
    }

	@Override
    public List<Relationship> getCloseRelatives(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons-with-relationships");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();
        params.putString("person", personId);

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getRelationships() != null && doc.getRelationships().size() > 0) {
                        List<Relationship> relatives = new ArrayList<>(doc.getRelationships());
                        Log.i(TAG, "getCloseRelatives " + doc.getRelationships().size() + ": " + personId);
                        return relatives;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getCloseRelatives(personId, checkCache);
                }
            }
        }

        return null;
    }

    public List<Relationship> getParents(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/parent-relationships");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getRelationships() != null && doc.getRelationships().size() > 0) {
                        List<Relationship> relatives = new ArrayList<>(doc.getRelationships());
                        Log.i(TAG, "getParents " + doc.getRelationships().size() + ": " + personId);
                        return relatives;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getParents(personId, checkCache);
                }
            }
        }

        return null;
    }

    public List<Relationship> getChildren(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/child-relationships");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getRelationships() != null && doc.getRelationships().size() > 0) {
                        List<Relationship> relatives = new ArrayList<>(doc.getRelationships());
                        Log.i(TAG, "getChildren " + doc.getRelationships().size() + ": " + personId);
                        return relatives;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getChildren(personId, checkCache);
                }
            }
        }

        return null;
    }

    public List<Relationship> getSpouses(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/spouse-relationships");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getRelationships() != null && doc.getRelationships().size() > 0) {
                        List<Relationship> relatives = new ArrayList<>(doc.getRelationships());
                        Log.i(TAG, "getSpouses " + doc.getRelationships().size() + ": " + personId);
                        return relatives;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getChildren(personId, checkCache);
                }
            }
        }

        return null;
    }

    public Map<Integer, Person> getPedigree(String personId) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Map<Integer, Person> tree = new HashMap<>();

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/ancestry");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();
        params.putString("person", personId);
        params.putString("personDetails", "");

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getPersons() != null && doc.getPersons().size() > 0) {
                        for (Person p : doc.getPersons()) {
                            if (!personCache.containsKey(p.getId())) {
                                personCache.put(p.getId(), p);
                            }
                            String placeNumStr = p.getDisplayExtension().getAscendancyNumber();
                            if (placeNumStr!=null) {
                                Integer ahnen = Integer.valueOf(placeNumStr);
                                tree.put(ahnen, p);
                            }
                        }
                        Log.i(TAG, "getPerson " + doc.getPersons().size() + ": " + personId);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getPedigree(personId);
                }
            }
        }

        return tree;
    }

    @Override
    public List<SourceDescription> getPersonMemories(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/memories");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");

        Bundle params = new Bundle();
        params.putString("type", "photo"); //-- limit to photos for now

        RemoteResult result = getRestData(METHOD_GET, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                Serializer serializer = GedcomxSerializer.create();
                try {
                    Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                    if (doc.getSourceDescriptions() != null && doc.getSourceDescriptions().size() > 0) {
                        Log.i(TAG, "getPersonMemories found " + doc.getSourceDescriptions().size() + " photos for " + personId);
                        return doc.getSourceDescriptions();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                //-- check status and retry if possible
                if (handleStatusCodes(result)) {
                    return getPersonMemories(personId, checkCache);
                }
            }
        }

        return null;
    }

    private boolean handleStatusCodes(RemoteResult data) throws RemoteServiceSearchException {
        //-- not authenticated
        if (data.getStatusCode()==401 && sessionId!=null) {
            try {
                String encodedAuthToken = null;
                encodedAuthToken = DataService.getInstance().getEncryptedProperty(DataService.SERVICE_TYPE_MYHERITAGE + DataService.SERVICE_TOKEN);
                RemoteResult res = authWithToken(encodedAuthToken);
                if (res.isSuccess()) {
                    return true;
                }
            } catch (Exception e) {
                throw new RemoteServiceSearchException("Unabled to authenticate with saved token", data.getStatusCode(), e);
            }
        }
        if (data.getStatusCode()==400) {
            Log.e(TAG, "Bad Request: "+data.getData());
            return false;
        }
        if (data.getStatusCode()==403) {
            Log.e(TAG, "Forbidden: "+data.getData());
            return false;
        }
        if (data.getStatusCode()==404) {
            Log.e(TAG, "Not Found: "+data.getData());
            return false;
        }
        if (data.getStatusCode()==429) {
            Log.d(TAG, "Connection throttled.  Delay and retry.");
            try {
                //-- allow up to 20 retries of throttled connections
                if (delayCount >= 20) {
                    return false;
                }
                delayCount++;
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage(), e);
                return false;
            }
            return true;
        }
        if (data.getStatusCode()>=500) {
            Log.e(TAG, "Internal Server Error: "+data.getData());
            return false;
        }
        return false;
    }

    @Override
    public String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, RemoteServiceSearchException {
        try {
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request =  new HttpGet();
            attachUriWithQuery(request, uri, null);

            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");

            HttpClient client = new DefaultHttpClient();

            for (BasicNameValuePair header : paramsToList(headers)) {
                request.addHeader( header.getName(), header.getValue() );
            }

            // Let's send some useful debug information so we can monitor things
            // in LogCat.
            Log.d(TAG, "Downloading image: "+ uri.toString());

            // Finally, we send our request using HTTP. This is the synchronous
            // long operation that we need to run on this Loader's thread.
            HttpResponse response = client.execute(request);

            HttpEntity responseEntity = response.getEntity();
            StatusLine responseStatus = response.getStatusLine();
            int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
            if (statusCode == 200) {
                if (responseEntity!=null) {
                    InputStream in = responseEntity.getContent();
                    File dataFolder = ImageHelper.getDataFolder(context);
                    File folder = new File(dataFolder, folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    else if (!folder.isDirectory()) {
                        folder.delete();
                        folder.mkdir();
                    }
                    File imageFile = new File(folder, fileName);
                    OutputStream out = new FileOutputStream(imageFile);

                    byte[] buffer = new byte[1024];
                    int loadedSize;
                    while((loadedSize = in.read(buffer)) != -1) {
                        out.write(buffer, 0, loadedSize);
                    }

                    in.close();
                    out.close();
                    return imageFile.getAbsolutePath();
                }
            }
        }
        catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request. "+e , e);
            throw new RemoteServiceSearchException("There was a problem when sending the request. "+e, 0, e);
        }

        return null;
    }

    @Override
    public String getPersonUrl(String remoteId) {
        return "https://MyHeritage.org/tree/#view=ancestor&person="+remoteId;
    }
}