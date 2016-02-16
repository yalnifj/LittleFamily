package com.yellowforktech.littlefamilytree.games;

import com.yellowforktech.littlefamilytree.data.LittlePerson;

/**
 * Created by Parents on 2/15/2016.
 */
public class SongNameAttributor implements SongPersonAttribute {
    @Override
    public String getAttributeFromPerson(LittlePerson person) {
        return person.getGivenName();
    }
}