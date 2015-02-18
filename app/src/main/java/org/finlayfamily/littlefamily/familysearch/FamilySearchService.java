package org.finlayfamily.littlefamily.familysearch;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.familysearch.identity.Identity;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.Gedcomx;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.rt.GedcomxSerializer;
import org.gedcomx.source.SourceDescription;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Parents on 12/29/2014.
 */
public class FamilySearchService {
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_POST = "POST";
    protected static final String SESSION_ID = "sessionid";
    private static final String FS_IDENTITY_PATH = "https://sandbox.familysearch.org/identity/v2/login";
    private static final String FS_PLATFORM_PATH = "https://sandbox.familysearch.org/platform/";
    private static final String FS_APP_KEY = "a0T3000000BM5hcEAD";
    private static final String TAG = FamilySearchService.class.getSimpleName();

    private String sessionId = null;
    private Person currentPerson = null;
    private Map<String, List<Relationship>> closeRelatives = null;
    private Map<String, Person> personCache;
    private Map<String, Link> linkCache;
    private Map<String, List<SourceDescription>> memories = null;
    private String encodedAuthToken = null;
    private int delayCount = 0;

    private static FamilySearchService ourInstance = new FamilySearchService();

    public static FamilySearchService getInstance() {
        return ourInstance;
    }

    private FamilySearchService() {
        personCache = new HashMap<>();
        linkCache = new HashMap<>();
		closeRelatives = new HashMap<>();
        memories = new HashMap<>();
    }

    public FSResult authenticate(String username, String password) throws FamilySearchException {
        encodedAuthToken = Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);

        return authWithToken();
    }

    private FSResult authWithToken() throws FamilySearchException {
        if (encodedAuthToken==null) {
            throw new FamilySearchException("Unable to authenticate with FamilySearch", 401);
        }
        Uri action = Uri.parse(FS_IDENTITY_PATH);
        Bundle params = new Bundle();
        params.putString("key", FS_APP_KEY);
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Basic " + encodedAuthToken);

        FSResult data = getRestData(METHOD_GET, action, params, headers);
        if (data!=null) {
            if (data.isSuccess()) {
                Serializer serializer = new Persister();
                try {
                    Identity session = serializer.read(Identity.class, data.getData());
                    this.sessionId = session.getSession().id;
                    Log.i(TAG, "session: " + sessionId);
                } catch (Exception e) {
                    Log.e(TAG, "error", e);
                }
            } else {
                this.sessionId = null;
            }
        }

        return data;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Person getCurrentPerson() throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }
        if (currentPerson==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/current-person");
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();

            FSResult result = getRestData(METHOD_GET, uri, params, headers);
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
                    }
                }
            }
        }
        return currentPerson;
    }

    public Person getPerson(String personId) throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }
        if (personCache.get(personId)==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId);
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();

            FSResult result = getRestData(METHOD_GET, uri, params, headers);
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
                        Person person = getPerson(newFSId);
                        personCache.put(personId, person);
                    }
                }
                else {
                    //-- check status and retry if possible
                    if (handleStatusCodes(result)) {
                        return getPerson(personId);
                    }
                }
            }
        }
        return personCache.get(personId);
    }

    public Link getPersonPortrait(String personId) throws FamilySearchException {
        if (linkCache.containsKey(personId)) {
            return linkCache.get(personId);
        }
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }

        Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/portraits");
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Bearer " + sessionId);
        headers.putString("Accept", "application/x-gedcomx-v1+xml");
        Bundle params = new Bundle();

        FSResult result = getRestData(METHOD_GET, uri, params, headers);
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
                                    linkCache.put(personId, link);
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
                    return getPersonPortrait(personId);
                }
            }
        }
        linkCache.put(personId, null);
        return null;
    }

    public List<Relationship> getCloseRelatives() throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }

        if (getCurrentPerson()==null) {
            throw new FamilySearchException("Unable to get current person from FamilySearch", 0);
        }

        return getCloseRelatives(currentPerson.getId());
    }

	public List<Relationship> getCloseRelatives(String personId) throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }

		Person person = this.getPerson(personId);
        if (person==null) {
            throw new FamilySearchException("Unable to get person "+personId+" from FamilySearch", 0);
        }

        if (closeRelatives.get(person.getId())==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons-with-relationships");
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();
            params.putString("person", person.getId());

            FSResult result = getRestData(METHOD_GET, uri, params, headers);
            if (result!=null) {
                if (result.isSuccess()) {
                    Serializer serializer = GedcomxSerializer.create();
                    try {
                        Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                        if (doc.getRelationships() != null && doc.getRelationships().size() > 0) {
                            List<Relationship> relatives = new ArrayList<>(doc.getRelationships());
                            for (Relationship r : relatives) {
                                getPerson(r.getPerson1().getResourceId());
                                getPerson(r.getPerson2().getResourceId());
                            }
                            closeRelatives.put(person.getId(), relatives);
                            Log.i(TAG, "getCloseRelatives " + doc.getRelationships().size() + ": " + personId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                } else {
                    //-- check status and retry if possible
                    if (handleStatusCodes(result)) {
                        return getCloseRelatives(personId);
                    }
                }
            }
        }

        return closeRelatives.get(person.getId());
    }

    public List<SourceDescription> getPersonMemories(String personId) throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }

        Person person = this.getPerson(personId);
        if (person==null) {
            throw new FamilySearchException("Unable to get person "+personId+" from FamilySearch", 0);
        }

        if (memories.get(personId)==null){
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons/"+personId+"/memories");
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");

            Bundle params = new Bundle();
            params.putString("type", "photo"); //-- limit to photos for now

            FSResult result = getRestData(METHOD_GET, uri, params, headers);
            if (result!=null) {
                if (result.isSuccess()) {
                    Serializer serializer = GedcomxSerializer.create();
                    try {
                        Gedcomx doc = serializer.read(Gedcomx.class, result.getData());
                        if (doc.getSourceDescriptions() != null && doc.getSourceDescriptions().size() > 0) {
                            memories.put(personId, doc.getSourceDescriptions());
                            Log.i(TAG, "getPersonMemories found " + doc.getSourceDescriptions().size() + " photos for " + personId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "error", e);
                    }
                } else {
                    //-- check status and retry if possible
                    if (handleStatusCodes(result)) {
                        return getPersonMemories(personId);
                    }
                }
            }
        }

        return memories.get(personId);
    }

    private boolean handleStatusCodes(FSResult data) throws FamilySearchException {
        //-- not authenticated
        if (data.getStatusCode()==401 && sessionId!=null) {
            FSResult res = authWithToken();
            if (res.isSuccess()) {
                return true;
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
                Thread.sleep(2000);
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
	
    private FSResult getRestData(String method, Uri action, Bundle params, Bundle headers) throws FamilySearchException{
        FSResult data = new FSResult();
        try {
            // At the very least we always need an action.
            if (action == null) {
                Log.e(TAG, "You did not define an action. REST call canceled.");
                throw new IllegalArgumentException("Action URI must not be null");
            }

            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
            HttpRequestBase request = null;

            // Let's build our request based on the HTTP verb we were
            // given.
            switch (method) {
                case METHOD_GET: {
                    request = new HttpGet();
                    attachUriWithQuery(request, action, params);
                }
                break;

                case METHOD_DELETE: {
                    request = new HttpDelete();
                    attachUriWithQuery(request, action, params);
                }
                break;

                case METHOD_POST: {
                    request = new HttpPost();
                    request.setURI(new URI(action.toString()));

                    // Attach form entity if necessary. Note: some REST APIs
                    // require you to POST JSON. This is easy to do, simply use
                    // postRequest.setHeader('Content-Type', 'application/json')
                    // and StringEntity instead. Same thing for the PUT case
                    // below.
                    HttpPost postRequest = (HttpPost) request;

                    if (params != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
                        postRequest.setEntity(formEntity);
                    }
                }
                break;

                case METHOD_PUT: {
                    request = new HttpPut();
                    request.setURI(new URI(action.toString()));

                    // Attach form entity if necessary.
                    HttpPut putRequest = (HttpPut) request;

                    if (params != null) {
                        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(paramsToList(params));
                        putRequest.setEntity(formEntity);
                    }
                }
                break;
            }

            if (request != null) {
                HttpClient client = new DefaultHttpClient();

                for (BasicNameValuePair header : paramsToList(headers)) {
                    request.addHeader( header.getName(), header.getValue() );
                }

                // Let's send some useful debug information so we can monitor things
                // in LogCat.
                Log.d(TAG, "Executing request: "+ method +": "+ action.toString());

                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this Loader's thread.
                HttpResponse response = client.execute(request);
                data.setResponse(response);

                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
                data.setStatusCode(statusCode);
                if (statusCode == 200) {
                    data.setSuccess(true);
                }

                if (responseEntity!=null) {
                    String results = EntityUtils.toString(responseEntity);
                    data.setData(results);
                }
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ method +": "+ action.toString()+ " "+e, e);
            throw new FamilySearchException("URI syntax was incorrect. "+e, 0, e);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding. "+e, e);
            throw new FamilySearchException("A UrlEncodedFormEntity was created with an unsupported encoding. "+e, 0, e);
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "There was a problem when sending the request. "+e, e);
            throw new FamilySearchException("There was a problem when sending the request. "+e, 0, e);
        }
        catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request. "+e , e);
            throw new FamilySearchException("There was a problem when sending the request. "+e, 0, e);
        }

        return data;
    }

    private static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
        try {
            if (params == null) {
                // No params were given or they have already been
                // attached to the Uri.
                request.setURI(new URI(uri.toString()));
            }
            else {
                Uri.Builder uriBuilder = uri.buildUpon();

                // Loop through our params and append them to the Uri.
                for (BasicNameValuePair param : paramsToList(params)) {
                    uriBuilder.appendQueryParameter(param.getName(), param.getValue());
                }

                uri = uriBuilder.build();
                request.setURI(new URI(uri.toString()));
            }
        }
        catch (URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect: "+ uri.toString());
        }
    }

    private static List<BasicNameValuePair> paramsToList(Bundle params) {
        ArrayList<BasicNameValuePair> formList = new ArrayList<BasicNameValuePair>(params.size());

        for (String key : params.keySet()) {
            Object value = params.get(key);

            // We can only put Strings in a form entity, so we call the toString()
            // method to enforce. We also probably don't need to check for null here
            // but we do anyway because Bundle.get() can return null.
            if (value != null) formList.add(new BasicNameValuePair(key, value.toString()));
        }

        return formList;
    }

    public String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, FamilySearchException {
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
            throw new FamilySearchException("There was a problem when sending the request. "+e, 0, e);
        }

        return null;
    }
}
