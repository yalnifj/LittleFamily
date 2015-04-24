package org.finlayfamily.littlefamily.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.finlayfamily.littlefamily.remote.RemoteService;
import org.finlayfamily.littlefamily.remote.RemoteServiceSearchException;
import org.finlayfamily.littlefamily.remote.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.util.ImageHelper;
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

        File imageFile = getImageFile(fsPerson.getId(), "portrait.jpg", context);
        if (checkCache && imageFile!=null && imageFile.exists()) {
            person.setPhotoPath(imageFile.getAbsolutePath());
        } else {
            Link portrait = service.getPersonPortrait(fsPerson.getId(), checkCache);
            if (portrait != null) {
                String imagePath = null;
                try {
                    Uri uri = Uri.parse(portrait.getHref().toString());
                    imagePath = service.downloadImage(uri, fsPerson.getId(), "portrait.jpg", context);
                    person.setPhotoPath(imagePath);
                } catch (MalformedURLException e) {
                    Log.e("buildLittlePerson", "error", e);
                }
            }
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
            if (parts[i]!=null && !parts[i].isEmpty()) return parts[i];
        }
        return href;
    }
}
