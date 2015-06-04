package org.finlayfamily.littlefamily.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

import org.finlayfamily.littlefamily.activities.LittleFamilyActivity;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.TreeNode;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.types.GenderType;

/**
 * Created by Parents on 5/29/2015.
 */
public class TreePersonAnimatedSprite extends Sprite {
    public static final int STATE_CLOSED = 0;
    public static final int STATE_ANIMATING_OPEN_LEFT = 1;
    public static final int STATE_ANIMATING_OPEN_RIGHT = 2;
    public static final int STATE_OPEN_LEFT = 3;
    public static final int STATE_OPEN_RIGHT = 4;
    public static final int STATE_ANIMATING_CLOSED_LEFT = 5;
    public static final int STATE_ANIMATING_CLOSED_RIGHT = 6;

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
    protected int detailWidth;
    protected int detailHeight;
    protected int dWidth;
    protected int dHeight;
    protected boolean opened;
    protected Paint openPaint;
    protected Paint shadowPaint;

    public TreePersonAnimatedSprite(TreeNode personNode, LittleFamilyActivity activity, Bitmap leftLeaf, Bitmap rightLeaf) {
        this.person = personNode.getPerson();
        this.node = personNode;
        this.activity = activity;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        this.selectable = true;
        this.setHeight(leftLeaf.getHeight());
        this.detailWidth = 400;
        this.detailHeight = 300;

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

        openPaint = new Paint();
        openPaint.setColor(Color.parseColor("#448844"));
        openPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#77001100"));
        shadowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    protected String getAncestralRelationship(LittlePerson p) {
        String rel = "";
        for(int g=3; g<=node.getDepth(); g++) {
            rel += "Great ";
        }
        if (node.getDepth()>=2) {
            rel += "Grand ";
        }
        if (p.getGender()== GenderType.Female) {
            if (node.getDepth()==0) {
                rel = "Sister";
            } else {
                rel += "Mother";
            }
        }
        else if (p.getGender()==GenderType.Male) {
            if (node.getDepth()==0) {
                rel = "Brother";
            } else {
                rel += "Father";
            }
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
        if (state==STATE_ANIMATING_OPEN_LEFT || state==STATE_ANIMATING_OPEN_RIGHT) {
            dWidth += 30;
            dHeight += 30;
            if (dWidth > detailWidth) dWidth = detailWidth;
            if (dHeight > detailHeight) dHeight = detailHeight;
            if (dWidth==detailWidth && dHeight==detailHeight) {
                if (state==STATE_ANIMATING_OPEN_LEFT) state = STATE_OPEN_LEFT;
                else state = STATE_OPEN_RIGHT;
                opened = true;
            }
        }
        if (state==STATE_ANIMATING_CLOSED_LEFT || state==STATE_ANIMATING_CLOSED_RIGHT) {
            dWidth -= 30;
            dHeight -= 30;
            if (dWidth < 0) dWidth = 0;
            if (dHeight < 0) dHeight = 0;
            if (dWidth==0 && dHeight==0) {
                state = STATE_CLOSED;
                opened = false;
            }
        }
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

        if (state==STATE_ANIMATING_OPEN_LEFT || state==STATE_OPEN_LEFT || state==STATE_ANIMATING_CLOSED_LEFT) {
            if (Build.VERSION.SDK_INT > 20) {
                canvas.drawRoundRect(getX() + leftLeaf.getWidth()+10, getY()+10, getX() + leftLeaf.getWidth() + dWidth+10, getY() + dHeight+10, 10, 10, shadowPaint);
                canvas.drawRoundRect(getX() + leftLeaf.getWidth(), getY(), getX() + leftLeaf.getWidth() + dWidth, getY() + dHeight, 10, 10, openPaint);
            } else {
                canvas.drawRect(getX() + leftLeaf.getWidth()+10, getY()+10, getX() + leftLeaf.getWidth() + dWidth+10, getY() + dHeight+10, shadowPaint);
                canvas.drawRect(getX() + leftLeaf.getWidth(), getY(), getX() + leftLeaf.getWidth() + dWidth, getY() + dHeight, openPaint);
            }
        }
        if (state==STATE_ANIMATING_OPEN_RIGHT || state==STATE_OPEN_RIGHT || state==STATE_ANIMATING_CLOSED_RIGHT) {
            if (Build.VERSION.SDK_INT > 20) {
                canvas.drawRoundRect(getX() + getWidth()+10, getY()+10, getX() + getWidth() + dWidth+10, getY() + dHeight+10, 10, 10, shadowPaint);
                canvas.drawRoundRect(getX() + getWidth(), getY(), getX() + getWidth() + dWidth, getY() + dHeight, 10, 10, openPaint);
            } else {
                canvas.drawRect(getX() + getWidth()+10, getY()+10, getX() + getWidth() + dWidth+10, getY() + dHeight+10, shadowPaint);
                canvas.drawRect(getX() + getWidth(), getY(), getX() + getWidth() + dWidth, getY() + dHeight, openPaint);
            }
        }

        LittlePerson detailPerson = node.getPerson();
        if (state==STATE_OPEN_LEFT) {
            if (node.getSpouse() != null && detailPerson.getGender() == GenderType.Female) {
                detailPerson = node.getSpouse();
            }
            String name = detailPerson.getName();
            if (name==null) name = detailPerson.getGivenName();
            canvas.drawText(name, getX() + leftLeaf.getWidth() + detailWidth/2, getY()+30, textPaint);
            String relationship = getAncestralRelationship(detailPerson);
            canvas.drawText(relationship, getX() + leftLeaf.getWidth() + detailWidth/2, getY()+60, textPaint);

        } else if (state==STATE_OPEN_RIGHT) {
            if (node.getSpouse() != null && detailPerson.getGender() != GenderType.Female) {
                detailPerson = node.getSpouse();
            }
            String name = detailPerson.getName();
            if (name==null) name = detailPerson.getGivenName();
            canvas.drawText(name, getX() + getWidth() + detailWidth/2, getY()+30, textPaint);
            String relationship = getAncestralRelationship(detailPerson);
            canvas.drawText(relationship, getX() + getWidth() + detailWidth/2, getY()+60, textPaint);
        }
    }

    @Override
    public void onSelect(float x, float y) {
        selected = true;
    }

    @Override
    public boolean onMove(float oldX, float oldY, float newX, float newY) {
        moved = true;
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        if (!moved) {
            if (!opened) {
                dWidth = 0;
                dHeight = 0;
                if (node.getSpouse() == null) {
                    state = STATE_ANIMATING_OPEN_LEFT;
                } else {
                    if (x < getX() + getWidth() / 2) {
                        state = STATE_ANIMATING_OPEN_LEFT;
                    } else {
                        state = STATE_ANIMATING_OPEN_RIGHT;
                    }
                }
            } else {
                dWidth = detailWidth;
                dHeight = detailHeight;
                if (state==STATE_OPEN_LEFT) {
                    state = STATE_ANIMATING_CLOSED_LEFT;
                } else {
                    state = STATE_ANIMATING_CLOSED_RIGHT;
                }
            }
        }
        selected = false;
        moved = false;
    }

    @Override
    public void onDestroy() {
        leftLeaf = null;
        rightLeaf = null;
        photo.recycle();
        photo = null;
    }

    public boolean isOpened() {
        return opened;
    }


}
