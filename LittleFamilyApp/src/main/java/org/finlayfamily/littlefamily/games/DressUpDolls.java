package org.finlayfamily.littlefamily.games;

import org.finlayfamily.littlefamily.data.LittlePerson;
import org.gedcomx.types.GenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jfinlay on 4/16/2015.
 */
public class DressUpDolls {
    private Map<String, String> countryMappings;

    public DressUpDolls() {
        countryMappings = new HashMap<>();
        countryMappings.put("united states", "usa");
        countryMappings.put("unknown", "usa");
        countryMappings.put("ireland", "ireland");
    }

    public DollConfig getDollConfig(String place, LittlePerson person) {
        String folder = countryMappings.get(place.toLowerCase());
        if (folder==null) folder = countryMappings.get("unknown");

        DollConfig dc = new DollConfig();
        dc.setFolderName(folder);
        if (person.getGender()== GenderType.Female)
            dc.setBoygirl("girl");
        else
            dc.setBoygirl("boy");

        return dc;
    }

}
