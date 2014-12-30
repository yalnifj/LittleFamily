package org.finlayfamily.littlefamily.familysearch;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

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
import org.gedcomx.Gedcomx;
import org.gedcomx.conclusion.Person;
import org.gedcomx.rt.GedcomxSerializer;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Person> closeRelatives = null;
    private Map<String, Person> personCache;

    private static FamilySearchService ourInstance = new FamilySearchService();

    public static FamilySearchService getInstance() {
        return ourInstance;
    }

    private FamilySearchService() {
        personCache = new HashMap<>();
    }

    public boolean authenticate(String username, String password) throws FamilySearchException {
        Uri action = Uri.parse(FS_IDENTITY_PATH);

        Bundle params = new Bundle();
        params.putString("key", FS_APP_KEY);
        Bundle headers = new Bundle();
        headers.putString("Authorization", "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP));

        String result = getRestData(METHOD_GET, action, params, headers);
        if (result!=null && !result.isEmpty()) {
            Serializer serializer = new Persister();
            try {
                Identity session = serializer.read(Identity.class, result);
                this.sessionId = session.getSession().id;
                Log.i( TAG, "session: " + sessionId );
                return true;
            }
            catch (Exception e) {
                Log.e( TAG, "error", e );
            }
        }

        return false;
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

            String result = getRestData(METHOD_GET, uri, params, headers);

            Serializer serializer = GedcomxSerializer.create();
            try {
                Gedcomx doc = serializer.read( Gedcomx.class, result );
                if (doc.getPersons() != null && doc.getPersons().size() > 0) {
                    currentPerson = doc.getPersons().get( 0 );
                    personCache.put(currentPerson.getId(), currentPerson);
                    Log.i( TAG, "persons " + doc.getPersons().size() + ": " + currentPerson.getId() );
                }
            }
            catch (Exception e) {
                Log.e( TAG, "error", e );
            }
        }
        return currentPerson;
    }

    public List<Person> getCloseRelatives() throws FamilySearchException {
        if (sessionId==null) {
            throw new FamilySearchException("Not Authenticated with FamilySearch.", 0);
        }

        if (getCurrentPerson()==null) {
            throw new FamilySearchException("Unable to get current person from FamilySearch", 0);
        }

        if (closeRelatives==null) {
            Uri uri = Uri.parse(FS_PLATFORM_PATH + "tree/persons-with-relationships");
            Bundle headers = new Bundle();
            headers.putString("Authorization", "Bearer " + sessionId);
            headers.putString("Accept", "application/x-gedcomx-v1+xml");
            Bundle params = new Bundle();
            params.putString("person", currentPerson.getId());

            String result = getRestData(METHOD_GET, uri, params, headers);

            Serializer serializer = GedcomxSerializer.create();
            try {
                Gedcomx doc = serializer.read( Gedcomx.class, result );
                if (doc.getPersons() != null && doc.getPersons().size() > 0) {
                    closeRelatives = new ArrayList<>(doc.getPersons());
                    for(Person p : closeRelatives) {
                        personCache.put(p.getId(), p);
                    }
                    Log.i( TAG, "persons " + doc.getPersons().size() );
                }
            }
            catch (Exception e) {
                Log.e( TAG, "error", e );
            }
        }

        return closeRelatives;
    }

    private String getRestData(String method, Uri action, Bundle params, Bundle headers) throws FamilySearchException{
        String data = null;
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

                HttpEntity responseEntity = response.getEntity();
                StatusLine responseStatus = response.getStatusLine();
                int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;
                if (statusCode != 200) {
                    throw new FamilySearchException("Invalid response from FamilySearch: "+responseStatus.getReasonPhrase(), statusCode);
                }

                if (responseEntity!=null) {
                    data = EntityUtils.toString(responseEntity);
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
}
