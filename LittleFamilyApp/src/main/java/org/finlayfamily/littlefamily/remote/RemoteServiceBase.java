package org.finlayfamily.littlefamily.remote;

import android.net.Uri;
import android.os.Bundle;
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
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 4/23/2015.
 */
public abstract class RemoteServiceBase implements RemoteService {

    protected String userAgent;
    protected Long lastRequestTime;
    protected Long minRequestTime = 250L;

    protected RemoteResult getRestData(String method, Uri action, Bundle params, Bundle headers) throws RemoteServiceSearchException {
        int retries = 0;
        while(retries < 3) {
            try {
                if (lastRequestTime!=null && System.currentTimeMillis() - lastRequestTime < minRequestTime) {
                    Thread.sleep(System.currentTimeMillis() - lastRequestTime); //-- limit to N requests per second
                }
            } catch (InterruptedException e) {
            }
            try {
                lastRequestTime = System.currentTimeMillis();
                return getRestDataNoRetry(method, action, params, headers);
            } catch (Exception e) {
            }
            try {
                Thread.sleep(30000 * (retries+1)); //-- wait for a few seconds and try again
            } catch (InterruptedException e) {
            }
            retries++;
        }
        throw new RemoteServiceSearchException("Maximum number of retries exceeded", 500);
    }

    protected RemoteResult getRestDataNoRetry(String method, Uri action, Bundle params, Bundle headers) throws RemoteServiceSearchException {
        RemoteResult data = new RemoteResult();
        try {
            // At the very least we always need an action.
            if (action == null) {
                Log.e(this.getClass().getSimpleName(), "You did not define an action. REST call canceled.");
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
                if (userAgent!=null) {
                    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
                }

                for (BasicNameValuePair header : paramsToList(headers)) {
                    request.addHeader( header.getName(), header.getValue() );
                }

                // Let's send some useful debug information so we can monitor things
                // in LogCat.
                Log.d(this.getClass().getSimpleName(), "Executing request: "+ method +": "+ action.toString() + " "+params.toString());

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
            Log.e(this.getClass().getSimpleName(), "URI syntax was incorrect. "+ method +": "+ action.toString()+ " "+e, e);
            throw new RemoteServiceSearchException("URI syntax was incorrect. "+e, 0, e);
        }
        catch (UnsupportedEncodingException e) {
            Log.e(this.getClass().getSimpleName(), "A UrlEncodedFormEntity was created with an unsupported encoding. "+e, e);
            throw new RemoteServiceSearchException("A UrlEncodedFormEntity was created with an unsupported encoding. "+e, 0, e);
        }
        catch (ClientProtocolException e) {
            Log.e(this.getClass().getSimpleName(), "There was a problem when sending the request. "+e, e);
            throw new RemoteServiceSearchException("There was a problem when sending the request. "+e, 0, e);
        }
        catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "There was a problem when sending the request. "+e , e);
            throw new RemoteServiceSearchException("There was a problem when sending the request. "+e, 0, e);
        }

        return data;
    }

    protected static void attachUriWithQuery(HttpRequestBase request, Uri uri, Bundle params) {
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
            Log.e("RemoteServiceBase", "URI syntax was incorrect: "+ uri.toString(), e);
        }
    }

    protected static List<BasicNameValuePair> paramsToList(Bundle params) {
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
