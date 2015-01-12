package org.finlayfamily.littlefamily.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.finlayfamily.littlefamily.familysearch.FamilySearchException;
import org.finlayfamily.littlefamily.familysearch.FamilySearchService;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.conclusion.Person;
import org.gedcomx.links.Link;

import java.io.File;
import java.net.MalformedURLException;

/**
 * Created by jfinlay on 1/12/2015.
 */
public class DataHelper {
    public static LittlePerson buildLittlePerson(Person fsPerson, Context context) throws FamilySearchException {
        FamilySearchService service = FamilySearchService.getInstance();
        LittlePerson person = new LittlePerson(fsPerson);
        //-- check if we already have a photo downloaded for this person
        File dataFolder = ImageHelper.getDataFolder(context);
        File imageFile = new File(dataFolder, fsPerson.getId());
        if (imageFile.exists()) {
            person.setPhotoPath(imageFile.getAbsolutePath());
        } else {
            Link portrait = service.getPersonPortrait(fsPerson.getId());
            if (portrait != null) {
                String imagePath = null;
                try {
                    Uri uri = Uri.parse(portrait.getHref().toString());
                    imagePath = service.downloadImage(uri, fsPerson.getId(), context);
                    person.setPhotoPath(imagePath);
                } catch (MalformedURLException e) {
                    Log.e("DataHelper.buildLittlePerson", "error", e);
                }
            }
        }
        return person;
    }
}
