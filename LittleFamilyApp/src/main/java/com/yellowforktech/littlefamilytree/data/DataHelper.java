package com.yellowforktech.littlefamilytree.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.yellowforktech.littlefamilytree.remote.RemoteService;
import com.yellowforktech.littlefamilytree.remote.RemoteServiceSearchException;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import org.gedcomx.conclusion.Person;
import org.gedcomx.links.Link;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class DataHelper {
    public static LittlePerson buildLittlePerson(Person fsPerson, Context context, RemoteService service, boolean checkCache) throws RemoteServiceSearchException {
        LittlePerson person = new LittlePerson(fsPerson);
        person.setLastSync(new Date());

        try {
            Link portrait = service.getPersonPortrait(fsPerson.getId(), true);
            if (portrait != null) {
                String imagePath = null;
                imagePath = DataHelper.downloadFile(portrait.getHref().toString(), fsPerson.getId(), DataHelper.lastPath(portrait.getHref().toString()), service, context);
                person.setPhotoPath(imagePath);
            }
        } catch (Exception e) {
            Log.e("buildLittlePerson", "error", e);
        }

        return person;
    }

    public static File getImageFile(String folderName, String fileName, Context context) {
        //-- check if we already have a photo downloaded for this person
        File dataFolder = ImageHelper.getDataFolder(context);
        File folder = new File(dataFolder, folderName);
        if (!folder.exists()) {
            return null;
        } else if (!folder.isDirectory()) {
            return null;
        }
        File imageFile = new File(folder, fileName);
        return imageFile;
    }

    public static String downloadFile(String href, String folderName, String fileName, RemoteService service, Context context) throws RemoteServiceSearchException {
        String imagePath = null;
        try {
            File imageFile = getImageFile(folderName,fileName, context);
            if (imageFile!=null && imageFile.exists()) {
                return imageFile.getAbsolutePath();
            } else {
                Uri uri = Uri.parse(href);
                imagePath = service.downloadImage(uri, folderName, fileName, context);
                return imagePath;
            }
        } catch (MalformedURLException e) {
            Log.e("DataHelper.downloadFile", "error", e);
        }
        return null;
    }

    public static String lastPath(String href) {
        String[] parts = href.split("/");
        for(int i = parts.length-1; i>=0; i--) {
            if (parts[i]!=null && !parts[i].isEmpty()) {
                String filePath = parts[i];
                int pos = filePath.indexOf("?");
                if (pos>0) {
                    filePath = filePath.substring(0, pos-1);
                }
                return filePath;
            }
        }
        return href;
    }
}
