package com.yellowforktech.littlefamilytree.remote.phpgedview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.yellowforktech.littlefamilytree.remote.RemoteResult;
import com.yellowforktech.littlefamilytree.remote.RemoteService;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceBase;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gedcomx.atom.Entry;
import org.gedcomx.common.ResourceReference;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;
import org.gedcomx.source.SourceReference;
import org.gedcomx.types.RelationshipType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 4/23/2015.
 */
public class PGVService extends RemoteServiceBase implements RemoteService {
    private static final String SUCCESS = "SUCCESS";

    private String baseUrl;
    private static final String TAG = PGVService.class.getSimpleName();

    private String sessionId = null;
    private String sessionName = null;
    private Person currentPerson = null;
    private Map<String, List<Relationship>> closeRelatives = null;
    private Map<String, Person> personCache;
    private Map<String, Link> linkCache;
    private Map<String, List<SourceDescription>> memories = null;
    private Map<String, List<FamilyHolder>> families = null;
    private Map<String, String> recordCache = null;
    private int delayCount = 0;
    private String defaultPersonId;
    private GedcomParser gedcomParser;

    protected String userAgent = "PGVAgent";

    public PGVService(String baseUrl, String defaultPersonId) {
        personCache = new HashMap<>();
        linkCache = new HashMap<>();
        closeRelatives = new HashMap<>();
        memories = new HashMap<>();
        families = new HashMap<>();
        recordCache = new HashMap<>();
        gedcomParser = new GedcomParser();
        this.baseUrl = baseUrl;
        this.defaultPersonId = defaultPersonId;
        minRequestTime = 1000L;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getVersion() throws RemoteServiceSearchException {
        Uri action = Uri.parse(baseUrl+"client.php");
        Bundle params = new Bundle();
        params.putString("action","version");
        Bundle headers = new Bundle();
        headers.putString("User-Agent", "PGVAgent");
        String version = null;

        RemoteResult data = getRestData(METHOD_POST, action, params, headers);
        if (data!=null) {
            if (data.isSuccess()) {
                String responseBody = data.getData();
                String[] parts = responseBody.split("\\s+");
                if (parts.length > 1 ){
                    if (SUCCESS.equals(parts[0])) {
                        version = parts[1];
                    }
                }
            }
        }

        return version;
    }

    @Override
    public RemoteResult authenticate(String username, String password) throws RemoteServiceSearchException {
        Uri action = Uri.parse(baseUrl+"client.php");
        Bundle params = new Bundle();
        params.putString("action","connect");
        params.putString("username",username);
        params.putString("password",password);
        Bundle headers = new Bundle();
        headers.putString("User-Agent", "PGVAgent");

        RemoteResult data = getRestDataNoRetry(METHOD_POST, action, params, headers);
        if (data!=null) {
            if (data.isSuccess()) {
                String responseBody = data.getData();
                String[] parts = responseBody.split("\\s+");
                if (parts.length > 2 ){
                    if (SUCCESS.equals(parts[0])) {
                        sessionName = parts[1];
                        sessionId = parts[2];
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
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        if (currentPerson==null) {
            currentPerson = getPerson(defaultPersonId, true);
        }
        return currentPerson;
    }

    @Override
    public Person getPerson(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }

        if (!checkCache || personCache.get(personId)==null) {
            String gedcom = getGedcomRecord(personId, checkCache);

            if (!gedcom.isEmpty()) {
                try {
                    Person person = gedcomParser.parsePerson(gedcom);
                    personCache.put(personId, person);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing gedcom for person "+personId, e);
                }
            } else {
                throw new RemoteServiceSearchException("Unable to find gedcom record for person "+personId, 404);
            }
        }
        return personCache.get(personId);
    }

    @Override
    public Entry getLastChangeForPerson(String personId) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = getPerson(personId, false);
        if (person!=null) {
            Date date = (Date) person.getTransientProperty("CHAN");
            if (date!=null) {
                Entry entry = new Entry();
                entry.setUpdated(date);
                return entry;
            }
        }
        return null;
    }

    @Override
    public Link getPersonPortrait(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        if (checkCache && linkCache.containsKey(personId)) {
            return linkCache.get(personId);
        }
        Person person = getPerson(personId, checkCache);
        Link portrait = null;
        if (person!=null) {
            List<SourceReference> media = person.getMedia();
            if (media!=null) {
                for(SourceReference sr : media) {
                    for(Link link : sr.getLinks()) {
                        if(link.getHref()!=null && link.getHref().toString().startsWith("@")) {
                            String objeid =link.getHref().toString().replaceAll("@","");
                            String gedcom = getGedcomRecord(objeid, checkCache);
                            if (!gedcom.isEmpty()) {
                                try {
                                    SourceDescription sd = gedcomParser.parseObje(gedcom, baseUrl);
                                    if (sd != null) {
                                        List<Link> links = sd.getLinks();
                                        for (Link link2 : links) {
                                            if (link2.getRel() != null && link2.getRel().equals("image")) {
                                                if (portrait == null || (sd.getSortKey()!=null && sd.getSortKey().equals("1"))) {
                                                    portrait = link2;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing gedcom for OBJE " + objeid, e);
                                }
                            } else {
                                throw new RemoteServiceSearchException("Unable to find gedcom record for media "+objeid, 404);
                            }
                        } else if (portrait==null){
                            portrait = link;
                        }
                    }
                }
            }
        }
        linkCache.put(personId, portrait);
        return portrait;
    }

    @Override
    public List<Relationship> getCloseRelatives(boolean checkCache) throws RemoteServiceSearchException {
        return getCloseRelatives(defaultPersonId, checkCache);
    }

    @Override
    public List<Relationship> getCloseRelatives(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = getPerson(personId, checkCache);
        if (person==null) {
            throw new RemoteServiceSearchException("Unable to get person "+personId+" from PGV", 0);
        }

        if (!checkCache || closeRelatives.get(personId)==null) {
            List<Relationship> family = new ArrayList<>();
            List<Link> fams = person.getLinks();
            if (fams != null) {
                for(Link fam : fams) {
                    String famid = fam.getHref().toString().replaceAll("@","");
                    String gedcom = getGedcomRecord(famid, checkCache);
                    if (!gedcom.isEmpty()) {
                        try {
                            FamilyHolder fh = gedcomParser.parseFamily(gedcom);
                            if (fam.getRel().equals("FAMC")) {
                                for (Link p : fh.getParents()) {
                                    String relId = p.getHref().toString().replaceAll("@", "");
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
                            if (fam.getRel().equals("FAMS")) {
                                for (Link p : fh.getParents()) {
                                    String relId = p.getHref().toString().replaceAll("@", "");
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
                                for (Link p : fh.getChildren()) {
                                    String relId = p.getHref().toString().replaceAll("@", "");
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
                            closeRelatives.put(personId, family);
                        } catch (GedcomParseException e) {
                            Log.e(TAG, "Error parsing gedcom for family " + famid, e);
                        }
                    } else {
                        throw new RemoteServiceSearchException("Unable to find gedcom record for family "+famid, 404);
                    }
                }
            }
        }

        return closeRelatives.get(personId);
    }

    public List<Relationship> getParents(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = getPerson(personId, checkCache);
        if (person==null) {
            throw new RemoteServiceSearchException("Unable to get person "+personId+" from PGV", 0);
        }

        List<Relationship> family = new ArrayList<>();
        List<Link> fams = person.getLinks();
        if (fams != null) {
            for(Link fam : fams) {
                if (fam.getRel().equals("FAMC")) {
                    String famid = fam.getHref().toString().replaceAll("@", "");
                    String gedcom = getGedcomRecord(famid, checkCache);
                    if (!gedcom.isEmpty()) {
                        try {
                            FamilyHolder fh = gedcomParser.parseFamily(gedcom);
                            for (Link p : fh.getParents()) {
                                String relId = p.getHref().toString().replaceAll("@", "");
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
                        } catch (GedcomParseException e) {
                            Log.e(TAG, "Error parsing gedcom for family " + famid, e);
                        }
                    } else {
                        throw new RemoteServiceSearchException("Unable to find gedcom record for family "+famid, 404);
                    }
                }
            }
        }

        return family;
    }

    public List<Relationship> getChildren(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = getPerson(personId, checkCache);
        if (person==null) {
            throw new RemoteServiceSearchException("Unable to get person "+personId+" from PGV", 0);
        }

        List<Relationship> family = new ArrayList<>();
        List<Link> fams = person.getLinks();
        if (fams != null) {
            for(Link fam : fams) {
                if (fam.getRel().equals("FAMS")) {
                    String famid = fam.getHref().toString().replaceAll("@", "");
                    String gedcom = getGedcomRecord(famid, checkCache);
                    if (!gedcom.isEmpty()) {
                        try {
                            FamilyHolder fh = gedcomParser.parseFamily(gedcom);
                            for (Link p : fh.getChildren()) {
                                String relId = p.getHref().toString().replaceAll("@", "");
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
                        } catch (GedcomParseException e) {
                            Log.e(TAG, "Error parsing gedcom for family " + famid, e);
                        }
                    } else {
                        throw new RemoteServiceSearchException("Unable to find gedcom record for family "+famid, 404);
                    }
                }
            }
        }

        return family;
    }

    public List<Relationship> getSpouses(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = getPerson(personId, checkCache);
        if (person==null) {
            throw new RemoteServiceSearchException("Unable to get person "+personId+" from PGV", 0);
        }

        List<Relationship> family = new ArrayList<>();
        List<Link> fams = person.getLinks();
        if (fams != null) {
            for(Link fam : fams) {
                if (fam.getRel().equals("FAMS")) {
                    String famid = fam.getHref().toString().replaceAll("@", "");
                    String gedcom = getGedcomRecord(famid, checkCache);
                    if (!gedcom.isEmpty()) {
                        try {
                            FamilyHolder fh = gedcomParser.parseFamily(gedcom);
                            for (Link p : fh.getParents()) {
                                String relId = p.getHref().toString().replaceAll("@", "");
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
                        } catch (GedcomParseException e) {
                            Log.e(TAG, "Error parsing gedcom for family " + famid, e);
                        }
                    } else {
                        throw new RemoteServiceSearchException("Unable to find gedcom record for family "+famid, 404);
                    }
                }
            }
        }

        return family;
    }

    @Override
    public List<SourceDescription> getPersonMemories(String personId, boolean checkCache) throws RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        Person person = this.getPerson(personId, checkCache);
        if (person==null) {
            throw new RemoteServiceSearchException("Unable to get person "+personId+" from FamilySearch", 0);
        }

        if (!checkCache || memories.get(personId)==null){
            List<SourceReference> media = person.getMedia();
            if (media!=null) {
                List<SourceDescription> sdlist = new ArrayList<>(media.size());
                for(SourceReference sr : media) {
                    for(Link link : sr.getLinks()) {
                        if(link.getHref()!=null && link.getHref().toString().startsWith("@")) {
                            String objeid =link.getHref().toString().replaceAll("@","");
                            String gedcom = getGedcomRecord(objeid, checkCache);
                            if (!gedcom.isEmpty()) {
                                try {
                                    SourceDescription sd = gedcomParser.parseObje(gedcom, baseUrl);
                                    if (sd != null) {
                                        if (sd.getLinks()!=null) {
                                            sdlist.add(sd);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing gedcom for OBJE " + objeid, e);
                                }
                            } else {
                                throw new RemoteServiceSearchException("Unable to find gedcom record for family "+personId, 404);
                            }
                        }
                    }
                }
                memories.put(personId, sdlist);
            }
        }

        return memories.get(personId);
    }

    @Override
    public String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, RemoteServiceSearchException {
        if (sessionId==null) {
            throw new RemoteServiceSearchException("Not Authenticated with PhpGedView.", 0);
        }
        try {
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request =  new HttpGet();
            Bundle params = new Bundle();
            params.putString(sessionName,sessionId);
            attachUriWithQuery(request, uri, params);

            HttpClient client = new DefaultHttpClient();

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

    private String getGedcomRecord(String recordId, boolean checkCache) throws RemoteServiceSearchException {
        if (checkCache && recordCache.containsKey(recordId)) return recordCache.get(recordId);

        Uri uri = Uri.parse(baseUrl + "client.php");
        Bundle headers = new Bundle();
        headers.putString("User-Agent", "PGVAgent");
        headers.putString("Cookie", sessionName+"="+sessionId+"; ");
        Bundle params = new Bundle();
        params.putString("action","get");
        params.putString("xref", recordId);

        RemoteResult result = getRestData(METHOD_POST, uri, params, headers);
        if (result!=null) {
            if (result.isSuccess()) {
                if (result.isSuccess()) {
                    String results = result.getData();
                    if (results.startsWith(SUCCESS)) {
                        int pos = results.indexOf('0');
                        if (pos < 0) {
                            Log.e(TAG, "Error getting gedcom record "+ results);
                            throw new RemoteServiceSearchException("Error getting gedcom record "+ results, 500);
                        }
                        String gedcom = results.substring(pos);
                        //Log.d(TAG, "getGedcomRecord:"+recordId+":"+gedcom);
                        return gedcom;
                    } else {
                        Log.e(TAG, "Error getting gedcom record "+ results);
                        throw new RemoteServiceSearchException("Error getting gedcom record "+ results, 500);
                    }
                }
            }
        }
        return "";
    }

    @Override
    public String getPersonUrl(String remoteId) {
        return baseUrl+"individual.php?pid="+remoteId;
    }
}
