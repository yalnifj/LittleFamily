package com.yellowforktech.littlefamilytree.games;

import android.content.Context;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.RelationshipCalculator;

/**
 * Created by Parents on 2/15/2016.
 */
public class SongRelationshipAttributor implements SongPersonAttribute {
    private LittlePerson me;
    private Context context;

    public SongRelationshipAttributor(LittlePerson me, Context context)
    {
        this.me = me;
        this.context = context;
    }

    @Override
    public String getAttributeFromPerson(LittlePerson person, int number) {
        if (me.equals(person)) {
            return context.getString(R.string.self);
        }
        return RelationshipCalculator.getRelationship(me, person, context);
    }
}