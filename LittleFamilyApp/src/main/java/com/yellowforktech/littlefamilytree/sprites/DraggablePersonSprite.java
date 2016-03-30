package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.data.LittlePerson;

/**
 * Created by jfinlay on 8/26/2015.
 */
public class DraggablePersonSprite extends DraggableSprite {
    protected LittlePerson person;

    public DraggablePersonSprite(Bitmap bitmap, LittlePerson person, int maxWidth, int maxHeight, String eventTopic, DisplayMetrics dm) {
        super(bitmap, maxWidth, maxHeight, eventTopic, dm);
        this.person = person;
        this.setData("person", person);
    }
	
	public LittlePerson getPerson() {
		return person;
	}
}
