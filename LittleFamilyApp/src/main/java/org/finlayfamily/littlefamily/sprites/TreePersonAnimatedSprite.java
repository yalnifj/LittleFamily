package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.types.GenderType;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreePersonAnimatedSprite extends Sprite {
    protected LittlePerson person;
    protected TreeNode node;
    protected LittleFamilyActivity activity;
    protected Bitmap leftLeaf;
    protected Bitmap rightLeaf;
    protected Bitmap photo;
    protected Bitmap spPhoto;
    protected boolean moved;
    protected Paint textPaint;
    protected int treeWidth;

    public static final int STATE_CLOSED = 0;
    public static final int STATE_ANIMATING_OPEN = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_ANIMATING_CLOSED = 3;

    public TreePersonAnimatedSprite(TreeNode personNode, LittleFamilyActivity activity, Bitmap leftLeaf, Bitmap rightLeaf) {
        this.person = personNode.getPerson();
        this.node = personNode;
        this.activity = activity;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        this.selectable = true;
        this.setHeight(leftLeaf.getHeight());

        if (personNode.getSpouse()!=null) {
            this.setWidth(leftLeaf.getWidth() + rightLeaf.getWidth());
        } else {
            this.setWidth(leftLeaf.getWidth());
        }

        photo = null;
        if (person.getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
        } else {
            photo = ImageHelper.loadBitmapFromResource(activity, person.getDefaultPhotoResource(), 0, (int)(leftLeaf.getWidth()*0.7), (int) (height*0.7));
        }

        spPhoto = null;
        if (node.getSpouse()!=null) {
            if (node.getSpouse().getPhotoPath() != null) {
                spPhoto = ImageHelper.loadBitmapFromFile(node.getSpouse().getPhotoPath(), ImageHelper.getOrientation(node.getSpouse().getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
            } else {
                spPhoto = ImageHelper.loadBitmapFromResource(activity, node.getSpouse().getDefaultPhotoResource(), 0, (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7));
            }
        }

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(32);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    protected String getAncestralRelationship(TreeNode node) {
        String rel = "";
        for(int g=3; g<=node.getDepth(); g++) {
            rel += "Great ";
        }
        if (node.getDepth()>=2) {
            rel += "Grand ";
        }
        if (node.getPerson().getGender()== GenderType.Female) {
            rel += "Mother";
        }
        else if (node.getPerson().getGender()==GenderType.Male) {
            rel += "Father";
        } else {
            rel += "Parent";
        }
        return rel;
    }

    public int getTreeWidth() {
        return treeWidth;
    }

    public void setTreeWidth(int treeWidth) {
        this.treeWidth = treeWidth;
    }

    @Override
    public void doStep() {

    }

    @Override
    public void doDraw(Canvas canvas) {
        if (person.getGender()==GenderType.Male || node.getSpouse()!=null) {
            Rect dst = new Rect();
            dst.set((int)getX(), (int)getY(), (int)(getX()+leftLeaf.getWidth()), (int) (getY()+leftLeaf.getHeight()));
            canvas.drawBitmap(leftLeaf, null, dst, null);
        }

        if (person.getGender()==GenderType.Female || node.getSpouse()!=null) {
            int x = (int) getX();
            int y = (int) getY();
            if (node.getSpouse()!=null) {
                x = x+leftLeaf.getWidth();
            }
            Rect dst = new Rect();
            dst.set(x, y, x+rightLeaf.getWidth(), y+rightLeaf.getHeight());
            canvas.drawBitmap(rightLeaf, null, dst, null);
        }

        Rect photoRect = new Rect();


        if (node.getSpouse()!=null) {
            int x = (int) getX();
            int y = (int) getY();
            float ratio = ((float)spPhoto.getWidth())/spPhoto.getHeight();
            int pw = (int) (leftLeaf.getWidth()*0.7f);
            int ph = (int) (leftLeaf.getWidth()*0.7f);
            if (spPhoto.getWidth() > spPhoto.getHeight()) {
                ph = (int) (pw / ratio);
            } else {
                pw = (int) (ph * ratio);
            }
            int px = x + (leftLeaf.getWidth()/2  - pw/2);
            if (person.getGender()!=GenderType.Female) {
                px = px + leftLeaf.getWidth();
            }
            int py = y + (height/2 - ph/2);
            photoRect.set(px, py, px + pw, py + ph);
            canvas.drawBitmap(spPhoto, null, photoRect, null);
            canvas.drawText(node.getSpouse().getGivenName(), px + pw/2, getY() + height, textPaint);
        }

        float ratio = ((float)photo.getWidth())/photo.getHeight();
        int pw = (int) (leftLeaf.getWidth()*0.7f);
        int ph = (int) (leftLeaf.getWidth()*0.7f);
        if (photo.getWidth() > photo.getHeight()) {
            ph = (int) (pw / ratio);
        } else {
            pw = (int) (ph * ratio);
        }
        int x = (int) getX();
        int y = (int) getY();
        int px = x + (leftLeaf.getWidth()/2  - pw/2);
        if (person.getGender()==GenderType.Female) {
            px = px + leftLeaf.getWidth();
        }
        int py = y + (height/2 - ph/2);
        photoRect.set(px, py, px + pw, py + ph);
        canvas.drawBitmap(photo, null, photoRect, null);
        canvas.drawText(person.getGivenName(), px + pw/2, getY()+height, textPaint);
    }

    @Override
    public void onSelect(float x, float y) {

    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        moved = true;
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        if (!moved) {
            state = STATE_ANIMATING_OPEN;
        }
        moved = false;
    }

    @Override
    public void onDestroy() {
        leftLeaf = null;
        rightLeaf = null;
        photo.recycle();
        photo = null;
    }
}
