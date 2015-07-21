package com.yellowforktech.littlefamilytree.remote;

import android.content.Context;
import android.net.Uri;

import org.gedcomx.atom.Entry;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.links.Link;
import org.gedcomx.source.SourceDescription;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Created by jfinlay on 4/23/2015.
 */
public interface RemoteService {
    String METHOD_GET = "GET";
    String METHOD_PUT = "PUT";
    String METHOD_DELETE = "DELETE";
    String METHOD_POST = "POST";

    RemoteResult authenticate(String username, String password) throws RemoteServiceSearchException;

    RemoteResult authWithToken(String token) throws RemoteServiceSearchException;

    String getSessionId();

    String createEncodedAuthToken(String username, String password);

    Person getCurrentPerson() throws RemoteServiceSearchException;

    Person getPerson(String personId, boolean checkCache) throws RemoteServiceSearchException;

    Entry getLastChangeForPerson(String personId) throws RemoteServiceSearchException;

    Link getPersonPortrait(String personId, boolean checkCache) throws RemoteServiceSearchException;

    List<Relationship> getCloseRelatives(boolean checkCache) throws RemoteServiceSearchException;

    List<Relationship> getCloseRelatives(String personId, boolean checkCache) throws RemoteServiceSearchException;

    List<Relationship> getParents(String personId, boolean checkCache) throws RemoteServiceSearchException;
    List<Relationship> getChildren(String personId, boolean checkCache) throws RemoteServiceSearchException;
    List<Relationship> getSpouses(String personId, boolean checkCache) throws RemoteServiceSearchException;

    List<SourceDescription> getPersonMemories(String personId, boolean checkCache) throws RemoteServiceSearchException;

    String downloadImage(Uri uri, String folderName, String fileName, Context context) throws MalformedURLException, RemoteServiceSearchException;
}
