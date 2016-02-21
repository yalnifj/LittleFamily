package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Parents on 2/15/2016.
 */
public class SongDatePlaceAttributor implements SongPersonAttribute {
    private DateFormat df = new SimpleDateFormat("yyyy");
    @Override
    public String getAttributeFromPerson(LittlePerson person, int number) {
        if (number % 2 == 0) {
            //-- date
            if (person.getBirthDate() != null) {
                return df.format(person.getBirthDate());
            } else {
                return "some time";
            }
        } else {
            //--place
            if (person.getBirthPlace() != null) {
                String country = PlaceHelper.getPlaceCountry(person.getBirthPlace());
                if (country.equalsIgnoreCase("united states")) {
                    String state = PlaceHelper.getTopPlace(person.getBirthPlace(), 2);
                    if (PlaceHelper.isInUS(state)) {
                        return state;
                    }
                }
                return country;
            } else {
                return "Earth";
            }
        }
    }
}