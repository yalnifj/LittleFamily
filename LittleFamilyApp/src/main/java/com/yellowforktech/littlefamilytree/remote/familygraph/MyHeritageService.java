package com.yellowforktech.littlefamilytree.remote.familygraph;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.familygraph.android.FamilyGraph;
import com.familygraph.android.FamilyGraphError;
import com.familygraph.android.Util;
import com.yellowforktech.littlefamilytree.remote.RemoteResult;
import com.yellowforktech.littlefamilytree.remote.RemoteService;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceBase;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.remote.phpgedview.FamilyHolder;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.ResourceReference;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.RelationshipType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String OAUTH2_PATH = "https://accounts.myheritage.com/oauth2/token";

    private static final String TAG = MyHeritageService.class.getSimpleName();

    private String sessionId = null;
    private int delayCount = 0;

    private Map<String, Person> personCache;

    private FamilyGraph familyGraph;
    private JSONConverter converter;

    public MyHeritageService() {
        familyGraph = new FamilyGraph(CLIENT_ID);
        converter = new JSONConverter();
        personCache = new HashMap<>();
    }

    public FamilyGraph getFamilyGraph() {
        return familyGraph;
    }

    @Override
    public RemoteResult authenticate(String username, String password) throws RemoteServiceSearchException {
        sessionId = familyGraph.getAccessToken();
        return null;
    }

    @Override
    public RemoteResult authWithToken(String token) throws RemoteServiceSearchException {
        sessionId = familyGraph.getAccessToken();
        return null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    public String createEncodedAuthToken(String username, String password) {
        return username+":"+password;
    }

    public JSONObject getCurrentUser() {
        try {
            String data = familyGraph.request("me");
            JSONObject userJ = Util.parseJson(data);
            return userJ;
        } catch (IOException e) {
            Log.e(TAG, "Error getting current user", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "Error getting current user", e);
        } catch (JSONException e) {
            Log.e(TAG, "Error getting current user", e);
        }
        return null;
    }

    @Override
    public Person getCurrentPerson() throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }
        Person currentPerson = null;
        try {
            JSONObject user = getCurrentUser();
            String indiId = user.getJSONObject("default_individual").getString("id");
            currentPerson = getPerson(indiId, false);
        } catch (Exception e) {
            Log.e(TAG, "error getCurrentPerson", e);
        }

        return currentPerson;
    }

    @Override
    public Person getPerson(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Person person = null;

        if (!checkCache || personCache.get(personId)==null) {
            try {
                String data = familyGraph.request(personId);
                JSONObject individual = Util.parseJson(data);
                person = converter.convertJSONPerson(individual);

                String eventData = familyGraph.request(personId + "/events");
                JSONObject events = Util.parseJson(eventData);
                converter.processEvents(events, person);

                personCache.put(personId, person);

            } catch (Exception e) {
                Log.e(TAG, "error getCurrentPerson", e);
            } catch (FamilyGraphError e) {
                Log.e(TAG, "familyGraphError getPerson " + personId, e);
                if (e.getErrorCode() == 404) {
                    personCache.remove(personId);
                    person = new Person();
                    person.setId(personId);
                    person.setTransientProperty("deleted", true);
                }
            }
        } else {
            person = personCache.get(personId);
        }

        return person;
    }

    @Override
    public Entry getLastChangeForPerson(String personId) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Entry entry = null;
        //TODO - ask MyHeritage about a changes api

        return entry;
    }

    @Override
    public Link getPersonPortrait(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        Person person = getPerson(personId, checkCache);
        Link portrait = null;
        if (person!=null) {
            List<SourceReference> media = person.getMedia();
            if (media!=null && media.size()>0) {
                for(SourceReference sr : media) {
                    for(Link link : sr.getLinks()) {
                        if(link.getHref()!=null) {
                            String objeid =link.getHref().toString();
                            try {
                                String data = familyGraph.request(objeid);
                                JSONObject json = Util.parseJson(data);
                                SourceDescription sd = converter.convertMedia(json);
                                List<Link> links = sd.getLinks();
                                for (Link link2 : links) {
                                    if (link2.getRel() != null && link2.getRel().equals("image")) {
                                        if (portrait == null || (sd.getSortKey()!=null && sd.getSortKey().equals("1"))) {
                                            portrait = link2;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "error getPersonPortrait", e);
                            } catch (FamilyGraphError e) {
                                Log.e(TAG, "FamilyGraphError getPersonPortrait", e);
                            }
                        }
                    }
                }
            }

            if (portrait==null) {
                List<SourceDescription> allMedia = getPersonMemories(personId, checkCache);
                for(SourceDescription sd : allMedia) {
                    List<Link> links = sd.getLinks();
                    for (Link link2 : links) {
                        if (link2.getRel() != null && link2.getRel().equals("image")) {
                            if (portrait == null || (sd.getSortKey()!=null && sd.getSortKey().equals("1"))) {
                                portrait = link2;
                            }
                        }
                    }
                }
            }
        }
        return portrait;
    }

    @Override
    public List<Relationship> getCloseRelatives(boolean checkCache) throws RemoteServiceSearchException {
        return getCloseRelatives(getCurrentPerson().getId(), checkCache);
    }

	@Override
    public List<Relationship> getCloseRelatives(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        List<Relationship> family = new ArrayList<>();
        try {
            String dataStr = familyGraph.request(personId+"/immediate_family");
            JSONObject json = Util.parseJson(dataStr);
            JSONArray data = json.getJSONArray("data");
            for(int i=0; i<data.length(); i++) {
                JSONObject rel = data.getJSONObject(i);
                if ("wife".equals(rel.getString("relationship_type")) || "husband".equals(rel.getString("relationship_type"))) {
                    Relationship relationship = new Relationship();
                    relationship.setKnownType(RelationshipType.Couple);
                    ResourceReference rr = new ResourceReference();
                    rr.setResourceId(rel.getJSONObject("individual").getString("id"));
                    relationship.setPerson1(rr);
                    ResourceReference rr2 = new ResourceReference();
                    rr2.setResourceId(personId);
                    relationship.setPerson2(rr2);
                    family.add(relationship);
                }
                if ("mother".equals(rel.getString("relationship_type")) || "father".equals(rel.getString("relationship_type"))) {
                    Relationship relationship = new Relationship();
                    relationship.setKnownType(RelationshipType.ParentChild);
                    ResourceReference rr = new ResourceReference();
                    rr.setResourceId(rel.getJSONObject("individual").getString("id"));
                    relationship.setPerson1(rr);
                    ResourceReference rr2 = new ResourceReference();
                    rr2.setResourceId(personId);
                    relationship.setPerson2(rr2);
                    family.add(relationship);
                }
                if ("daughter".equals(rel.getString("relationship_type")) || "son".equals(rel.getString("relationship_type"))) {
                    Relationship relationship = new Relationship();
                    relationship.setKnownType(RelationshipType.ParentChild);
                    ResourceReference rr = new ResourceReference();
                    rr.setResourceId(personId);
                    relationship.setPerson1(rr);
                    ResourceReference rr2 = new ResourceReference();
                    rr2.setResourceId(rel.getJSONObject("individual").getString("id"));
                    relationship.setPerson2(rr2);
                    family.add(relationship);
                }
            }
            return family;
        } catch (Exception e) {
            Log.e(TAG, "error getCloseRelatives", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "FamilyGraphError getCloseRelatives", e);
        }

        return family;
    }

    public List<Relationship> getParents(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        List<Relationship> family = new ArrayList<>();
        try {
            String dataStr = familyGraph.request(personId+"/child_in_families_connection");
            JSONObject json = Util.parseJson(dataStr);
            JSONArray fams = json.getJSONArray("data");
            if (fams != null) {
                for(int i=0; i< fams.length(); i++) {
                    JSONObject famc = fams.getJSONObject(i);
                    String famData = familyGraph.request(famc.getJSONObject("family").getString("id"));
                    JSONObject fam = Util.parseJson(famData);
                    FamilyHolder fh = converter.convertFamily(fam);
                    for (Link p : fh.getParents()) {
                        String relId = p.getHref().toString();
                        if (!relId.equals(personId)) {
                            Relationship rel = new Relationship();
                            rel.setKnownType(RelationshipType.ParentChild);
                            ResourceReference rr = new ResourceReference();
                            rr.setResourceId(relId);
                            rel.setPerson1(rr);
                            ResourceReference rr2 = new ResourceReference();
                            rr2.setResourceId(personId);
                            rel.setPerson2(rr2);
                            family.add(rel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error getParents", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "FamilyGraphError getParents", e);
        }

        return family;
    }

    public List<Relationship> getChildren(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        List<Relationship> family = new ArrayList<>();
        try {
            String dataStr = familyGraph.request(personId+"/spouse_in_families_connection");
            JSONObject json = Util.parseJson(dataStr);
            JSONArray fams = json.getJSONArray("data");
            if (fams != null) {
                for(int i=0; i< fams.length(); i++) {
                    JSONObject fam = fams.getJSONObject(i);
                    FamilyHolder fh = converter.convertFamily(fam);
                    for (Link p : fh.getChildren()) {
                        String relId = p.getHref().toString();
                        if (!relId.equals(personId)) {
                            Relationship rel = new Relationship();
                            rel.setKnownType(RelationshipType.ParentChild);
                            ResourceReference rr = new ResourceReference();
                            rr.setResourceId(personId);
                            rel.setPerson1(rr);
                            ResourceReference rr2 = new ResourceReference();
                            rr2.setResourceId(relId);
                            rel.setPerson2(rr2);
                            family.add(rel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error getChildren", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "FamilyGraphError getChildren", e);
        }

        return family;
    }

    public List<Relationship> getSpouses(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        List<Relationship> family = new ArrayList<>();
        try {
            String dataStr = familyGraph.request(personId+"/spouse_in_families_connection");
            JSONObject json = Util.parseJson(dataStr);
            JSONArray fams = json.getJSONArray("data");
            if (fams != null) {
                for(int i=0; i< fams.length(); i++) {
                    JSONObject fam = fams.getJSONObject(i);
                    FamilyHolder fh = converter.convertFamily(fam);
                    for (Link p : fh.getParents()) {
                        String relId = p.getHref().toString();
                        if (!relId.equals(personId)) {
                            Relationship rel = new Relationship();
                            rel.setKnownType(RelationshipType.Couple);
                            ResourceReference rr = new ResourceReference();
                            rr.setResourceId(relId);
                            rel.setPerson1(rr);
                            ResourceReference rr2 = new ResourceReference();
                            rr2.setResourceId(personId);
                            rel.setPerson2(rr2);
                            family.add(rel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error getSpouses", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "FamilyGraphError getSpouses", e);
        }

        return family;
    }

    @Override
    public List<SourceDescription> getPersonMemories(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with MyHeritage.", 0);
        }

        List<SourceDescription> media = new ArrayList<>();
        try {
            String reqStr = personId + "/media";
            boolean hasMorePages = true;
            while(hasMorePages) {
                String dataStr = familyGraph.request(reqStr);
                JSONObject json = Util.parseJson(dataStr);

                if (json.has("data")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject med = data.getJSONObject(i);
                        SourceDescription sd = converter.convertMedia(med);
                        media.add(sd);
                    }

                    if (json.has("paging")) {
                        if (json.getJSONObject("paging").has("next")) {
                            reqStr = json.getJSONObject("paging").getString("next");
                        } else {
                            hasMorePages = false;
                        }
                    } else {
                        hasMorePages = false;
                    }
                } else {
                    hasMorePages = false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error getPersonMemories", e);
        } catch (FamilyGraphError e) {
            Log.e(TAG, "FamilyGraphError getPersonMemories", e);
        }
        return media;
    }

    @Override
    public String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, RemoteServiceSearchException {
        try {
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request =  new HttpGet();
            attachUriWithQuery(request, uri, null);

            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + familyGraph.getAccessToken());

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
        try {
            Person person = getPerson(remoteId, true);
            for(Link link : person.getLinks()) {
                if (link.getRel().equals("link")) {
                    return link.getHref().toString();
                }
            }
        } catch (RemoteServiceSearchException e) {
            Log.e(TAG, "getPersonUrl", e);
        }
        return "https://www.myheritage.com/";
    }
}
