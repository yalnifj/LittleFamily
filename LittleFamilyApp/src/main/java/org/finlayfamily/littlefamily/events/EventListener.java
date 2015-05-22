package org.finlayfamily.littlefamily.events;

/**
 * Created by jfinlay on 5/22/2015.
 */
public interface EventListener {
    public void onEvent(String topic, Object o);
}
