package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.data.LittlePerson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kids on 6/17/15.
 */
public class BubbleSpriteSurfaceView extends SpritedSurfaceView {

    private List<LittlePerson> parents;
    private List<LittlePerson> children;

    private List<BubbleCompleteListener> listeners;

    public BubbleSpriteSurfaceView(Context context) {
        super(context);
        listeners = new ArrayList<>(1);
    }

    public BubbleSpriteSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        listeners = new ArrayList<>(1);
    }

    public List<LittlePerson> getParents() {
        return parents;
    }

    public void setParents(List<LittlePerson> parents) {
        this.parents = parents;
    }

    public List<LittlePerson> getChildren() {
        return children;
    }

    public void setChildren(List<LittlePerson> children) {
        this.children = children;
    }

    public void registerListener(BubbleCompleteListener l) {
        listeners.add(l);
    }

    public interface BubbleCompleteListener {
        public void onBubbleComplete();
    }
}
