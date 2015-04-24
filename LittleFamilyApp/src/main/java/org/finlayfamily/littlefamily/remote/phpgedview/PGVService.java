package org.finlayfamily.littlefamily.remote.phpgedview;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.finlayfamily.littlefamily.remote.RemoteResult;
import org.finlayfamily.littlefamily.remote.RemoteService;
import org.finlayfamily.littlefamily.remote.RemoteServiceBase;
import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;
import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;

import java.net.MalformedURLException;
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
    private String encodedAuthToken = null;
    private int delayCount = 0;
    private String defaultPersonId;
    private GedcomParser gedcomParser;

    public PGVService(String baseUrl, String defaultPersonId) {
        personCache = new HashMap<>();
        linkCache = new HashMap<>();
        closeRelatives = new HashMap<>();
        memories = new HashMap<>();
        families = new HashMap<>();
        gedcomParser = new GedcomParser();
        this.baseUrl = baseUrl;
        this.defaultPersonId = defaultPersonId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public RemoteResult authenticate(String username, String password) throws RemoteServiceSearchException {
        Uri action = Uri.parse(baseUrl+"/client.php");
        Bundle params = new Bundle();
        params.putString("action","connect");
        params.putString("username",username);
        params.putString("password",password);
        Bundle headers = new Bundle();

        encodedAuthToken = username+":"+password;

        RemoteResult data = getRestData(METHOD_POST, action, params, headers);
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

    @Override
    public String getEncodedAuthToken() {
        return encodedAuthToken;
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
        if (!checkCache || personCache.get(personId)==null) {
            Uri uri = Uri.parse(baseUrl + "client.php");
            Bundle headers = new Bundle();
            Bundle params = new Bundle();
            params.putString("action","connect");
            params.putString(sessionName, sessionId);
            params.putString("xref", personId);

            RemoteResult result = getRestData(METHOD_POST, uri, params, headers);
            if (result!=null) {
                if (result.isSuccess()) {
                    if (result.isSuccess()) {
                        String results = result.getData();
                        if (results.startsWith(SUCCESS)) {
                            try {
                                String gedcom = results.substring(results.indexOf('1'));
                                Person person = gedcomParser.parsePerson(gedcom);
                                personCache.put(personId, person);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing gedcom for person "+personId, e);
                            }

                        } else {
                            Log.e(TAG, results);
                        }
                    }
                }
            }
        }
        return personCache.get(personId);
    }

    @Override
    public Entry getLastChangeForPerson(String personId) throws RemoteServiceSearchException {
        return null;
    }

    @Override
    public Link getPersonPortrait(String personId, boolean checkCache) throws RemoteServiceSearchException {
        return null;
    }

    @Override
    public List<Relationship> getCloseRelatives(boolean checkCache) throws RemoteServiceSearchException {
        return null;
    }

    @Override
    public List<Relationship> getCloseRelatives(String personId, boolean checkCache) throws RemoteServiceSearchException {
        return null;
    }

    @Override
    public List<SourceDescription> getPersonMemories(String personId, boolean checkCache) throws RemoteServiceSearchException {
        return null;
    }

    @Override
    public String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, RemoteServiceSearchException {
        return null;
    }
}
