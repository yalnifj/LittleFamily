package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.data.LittlePerson;

import org.gedcomx.types.GenderType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		countryMappings.put("native american", "nativeamerican");
		countryMappings.put("germany", "germany");
        countryMappings.put("denmark", "denmark");
        countryMappings.put("england", "england");
        countryMappings.put("france", "france");
        countryMappings.put("wales", "wales");
        countryMappings.put("scotland", "scotland");
        countryMappings.put("mexico", "mexico");
        countryMappings.put("sweden", "sweden");
        countryMappings.put("netherlands", "netherlands");
        countryMappings.put("russia", "russia");
        countryMappings.put("spain", "spain");
        countryMappings.put("portugal", "portugal");
    }

    public DollConfig getDollConfig(String place, LittlePerson person) {
        String folder = null;
        if (place!=null) folder = countryMappings.get(place.toLowerCase());
        if (folder==null) folder = countryMappings.get("unknown");

        DollConfig dc = new DollConfig();
        dc.setFolderName(folder);
        if (person.getGender()== GenderType.Female)
            dc.setBoygirl("girl");
        else
            dc.setBoygirl("boy");
        dc.setOriginalPlace(place);

        return dc;
    }

    public Set<String> getDollPlaces() {
        HashSet<String> places = new HashSet<>(countryMappings.keySet());
        places.remove("unknown");
        return places;
    }
}
