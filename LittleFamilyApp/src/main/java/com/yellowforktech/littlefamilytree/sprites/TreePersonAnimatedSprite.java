package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.activities.MyTreeActivity;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.TreeNode;
import com.yellowforktech.littlefamilytree.events.EventQueue;
import com.yellowforktech.littlefamilytree.games.DollConfig;
import com.yellowforktech.littlefamilytree.util.ImageHelper;
import com.yellowforktech.littlefamilytree.util.PlaceHelper;

import org.gedcomx.types.GenderType;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

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

    protected LittlePerson father;
    protected LittlePerson mother;
    protected TreeNode node;
    protected MyTreeActivity activity;
    protected Bitmap leftLeaf;
    protected Bitmap rightLeaf;
    protected Bitmap photo;
    protected Bitmap spPhoto;
    protected Bitmap dressUpBtn;
    protected Bitmap spDressUpBtn;
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
    protected DateFormat df;
    protected List<Bitmap> activityButtons;
    protected List<Bitmap> spActivityButtons;

    public TreePersonAnimatedSprite(TreeNode personNode, MyTreeActivity activity, Bitmap leftLeaf, Bitmap rightLeaf) {
        super();
        this.node = personNode;
        this.activity = activity;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        this.selectable = true;
        this.setHeight(leftLeaf.getHeight());
        this.detailWidth = 400;
        this.detailHeight = 300;

        activityButtons = new ArrayList<>();
        activityButtons.add(activity.getMatchBtn());
        activityButtons.add(activity.getPuzzleBtn());
        activityButtons.add(activity.getPaintBtn());
        activityButtons.add(activity.getPencilBtn());
        activityButtons.add(activity.getBubbleBtn());
        spActivityButtons = new ArrayList<>();
        spActivityButtons.add(activity.getMatchBtn());
        spActivityButtons.add(activity.getPuzzleBtn());
        spActivityButtons.add(activity.getPaintBtn());
        spActivityButtons.add(activity.getPencilBtn());
        spActivityButtons.add(activity.getBubbleBtn());

        if (personNode.getSpouse()!=null) {
            this.setWidth(leftLeaf.getWidth() + rightLeaf.getWidth());
        } else {
            this.setWidth(leftLeaf.getWidth());
        }

        photo = null;
        if (node.getPerson().getPhotoPath() != null) {
            photo = ImageHelper.loadBitmapFromFile(node.getPerson().getPhotoPath(), ImageHelper.getOrientation(node.getPerson().getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
        }
        if (photo==null){
            photo = ImageHelper.loadBitmapFromResource(activity, node.getPerson().getDefaultPhotoResource(), 0, (int)(leftLeaf.getWidth()*0.7), (int) (height*0.7));
        }
        String place = "unknown";
        if (node.getPerson().getBirthPlace()!=null) {
            place = node.getPerson().getBirthPlace();
        }
        DollConfig dollConfig = activity.getDressUpDolls().getDollConfig(PlaceHelper.getTopPlace(place), node.getPerson());
        String thumbnailFile = dollConfig.getThumbnail();
        try {
            InputStream is = activity.getAssets().open(thumbnailFile);
            dressUpBtn = ImageHelper.loadBitmapFromStream(is, 0, MyTreeActivity.buttonSize, MyTreeActivity.buttonSize);
            if (node.getPerson().getGender()==GenderType.Female && node.getSpouse()!=null) {
                spActivityButtons.add(dressUpBtn);
            } else {
                activityButtons.add(dressUpBtn);
            }
            is.close();
        } catch (IOException e) {
            Log.e("TreePersonAnimatedSprit", "Error opening asset file", e);
        }

        spPhoto = null;
        if (node.getSpouse()!=null) {
            if (node.getSpouse().getPhotoPath() != null) {
                spPhoto = ImageHelper.loadBitmapFromFile(node.getSpouse().getPhotoPath(), ImageHelper.getOrientation(node.getSpouse().getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
            }
            if (spPhoto==null) {
                spPhoto = ImageHelper.loadBitmapFromResource(activity, node.getSpouse().getDefaultPhotoResource(), 0, (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7));
            }
            dollConfig = activity.getDressUpDolls().getDollConfig(place, node.getSpouse());
            thumbnailFile = dollConfig.getThumbnail();
            try {
                InputStream is = activity.getAssets().open(thumbnailFile);
                spDressUpBtn = ImageHelper.loadBitmapFromStream(is, 0, MyTreeActivity.buttonSize, MyTreeActivity.buttonSize);
                if (node.getSpouse().getGender()==GenderType.Female) {
                    spActivityButtons.add(spDressUpBtn);
                } else {
                    activityButtons.add(spDressUpBtn);
                }
                is.close();
            } catch (IOException e) {
                Log.e("TreePersonAnimatedSprit", "Error opening asset file", e);
            }
        }

        if (node.getPerson().getGender()==GenderType.Female) {
            mother = node.getPerson();
        } else {
            father = node.getPerson();
        }

        if (node.getSpouse()!=null) {
            if (father==null) father = node.getSpouse();
            else mother = node.getSpouse();
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

        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    }

    public TreeNode getNode() {
        return node;
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
                if (state==STATE_ANIMATING_OPEN_LEFT) {
                    state = STATE_OPEN_LEFT;
                    LittlePerson person = father;
                    if (person==null) person = mother;
                    try {
                        String relationship = node.getAncestralRelationship(person);
                        String text = "";
                        if (!relationship.equalsIgnoreCase("you")) {
                            text = String.format(activity.getResources().getString(R.string.relative_is_your),
                                    person.getName(), relationship);
                        } else {
                            text = String.format(activity.getResources().getString(R.string.player_greeting), person.getGivenName());
                        }
                        if (person.getBirthDate()!=null || person.getBirthPlace()!=null) {
                            String birthText = getBirthText(person);
                            text += " " + birthText;
                        }
                        activity.speak(text);
                    } catch (Exception e) { }
                }
                else {
                    state = STATE_OPEN_RIGHT;
                    try {
                        String relationship = node.getAncestralRelationship(mother);
                        String text = "";
                        if (!relationship.equalsIgnoreCase("you")) {
                            text = String.format(activity.getResources().getString(R.string.relative_is_your),
                                    mother.getName(), relationship);
                        } else {
                            text = String.format(activity.getResources().getString(R.string.player_greeting), mother.getGivenName());
                        }
                        if (mother.getBirthDate()!=null || mother.getBirthPlace()!=null) {
                            String birthText = getBirthText(mother);
                            text += " " + birthText;
                        }
                        activity.speak(text);
                    } catch (Exception e) {}
                }
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
        if (node.getPerson().getGender()==GenderType.Male || node.getSpouse()!=null) {
            Rect dst = new Rect();
            dst.set((int)getX(), (int)getY(), (int)(getX()+leftLeaf.getWidth()), (int) (getY()+leftLeaf.getHeight()));
            canvas.drawBitmap(leftLeaf, null, dst, null);
        }

        if (node.getPerson().getGender()==GenderType.Female || node.getSpouse()!=null) {
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
            if (node.getPerson().getGender()!=GenderType.Female) {
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
        int px = x + (leftLeaf.getWidth()/2  - pw / 2);
        if (node.getSpouse()!=null && node.getPerson().getGender()==GenderType.Female) {
            px = px + leftLeaf.getWidth();
        }
        int py = y + (height / 2 - ph / 2);
        photoRect.set(px, py, px + pw, py + ph);
        canvas.drawBitmap(photo, null, photoRect, null);
        canvas.drawText(node.getPerson().getGivenName(), px + pw / 2, getY() + height, textPaint);

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
            Bitmap duBtn = dressUpBtn;
            if (node.getSpouse() != null && detailPerson.getGender() == GenderType.Female) {
                detailPerson = node.getSpouse();
                duBtn = spDressUpBtn;
            }
            String name = detailPerson.getName();
            if (name==null) name = detailPerson.getGivenName();
            canvas.drawText(name, getX() + leftLeaf.getWidth() + detailWidth/2, getY()+30, textPaint);
            String relationship = node.getAncestralRelationship(detailPerson);
            canvas.drawText(relationship, getX() + leftLeaf.getWidth() + detailWidth / 2, getY() + 70, textPaint);

            float bx = getX() + leftLeaf.getWidth() + 20;
            float by = getY() + detailHeight -MyTreeActivity.buttonSize*2;
            int count=1;
            for(Bitmap button : activityButtons) {
                canvas.drawBitmap(button, bx, by, null);
                bx += button.getWidth()+20;
                if (count % 3 == 0) {
                    bx = getX() + leftLeaf.getWidth() + 20;
                    by+=MyTreeActivity.buttonSize;
                }
                count++;
            }
        } else if (state==STATE_OPEN_RIGHT) {
            Bitmap duBtn = dressUpBtn;
            if (node.getSpouse() != null && detailPerson.getGender() != GenderType.Female) {
                detailPerson = node.getSpouse();
                duBtn = spDressUpBtn;
            }
            String name = detailPerson.getName();
            if (name==null) name = detailPerson.getGivenName();
            canvas.drawText(name, getX() + getWidth() + detailWidth/2, getY()+30, textPaint);
            String relationship = node.getAncestralRelationship(detailPerson);
            canvas.drawText(relationship, getX() + getWidth() + detailWidth/2, getY()+70, textPaint);

            float bx = getX() + getWidth() + 20;
            float by = getY() + detailHeight -MyTreeActivity.buttonSize*2;
            int count=1;
            for(Bitmap button : spActivityButtons) {
                canvas.drawBitmap(button, bx, by, null);
                bx += button.getWidth()+20;
                if (count % 3 == 0) {
                    bx = getX() + getWidth() + 20;
                    by+=MyTreeActivity.buttonSize;
                }
                count++;
            }
        }
    }

    public String getBirthText(LittlePerson detailPerson) {
        String text = "";
        if (node.isRoot()) {
            if (detailPerson.getBirthPlace()==null) {
                text = String.format(activity.getResources().getString(R.string.you_born_on),
                        df.format(detailPerson.getBirthDate()));
            } else if (detailPerson.getBirthDate()==null) {
                text = String.format(activity.getResources().getString(R.string.you_born_in),
                        detailPerson.getBirthPlace());
            } else {
                text = String.format(activity.getResources().getString(R.string.you_born_in_on),
                        detailPerson.getBirthPlace(), df.format(detailPerson.getBirthDate()));
            }
        }
        else {
            String heshe = activity.getResources().getString(R.string.he);
            if (detailPerson.getGender() == GenderType.Female) {
                heshe = activity.getResources().getString(R.string.she);
            }
            if (detailPerson.getBirthPlace() == null) {
                text = String.format(activity.getResources().getString(R.string.born_on),
                        heshe, df.format(detailPerson.getBirthDate()));
            } else if (detailPerson.getBirthDate() == null) {
                text = String.format(activity.getResources().getString(R.string.born_in),
                        heshe, detailPerson.getBirthPlace());
            } else {
                text = String.format(activity.getResources().getString(R.string.born_in_on),
                        heshe, detailPerson.getBirthPlace(), df.format(detailPerson.getBirthDate()));
            }
        }
        return text;
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
                    if (x < (getX() + leftLeaf.getWidth())*scale) {
                        state = STATE_ANIMATING_OPEN_LEFT;
                    } else {
                        state = STATE_ANIMATING_OPEN_RIGHT;
                    }
                }
            } else if (state != STATE_ANIMATING_CLOSED_LEFT && state!=STATE_ANIMATING_CLOSED_RIGHT){
                dWidth = detailWidth;
                dHeight = detailHeight;
                if (state==STATE_OPEN_LEFT) {
                    float bx = getX() + leftLeaf.getWidth() + 20;
                    float by = getY() + detailHeight - MyTreeActivity.buttonSize*2;
                    int count=1;
                    for (Bitmap button : activityButtons) {
                        if (x > bx * scale && y > by * scale
                                && x < (bx + button.getWidth()) * scale && y < (by + button.getHeight()) * scale) {
                            sendEvent(button, node.getPerson());
                            break;
                        }
                        bx += button.getWidth() + 20;
                        if (count % 3 == 0) {
                            by+=MyTreeActivity.buttonSize;
                            bx = getX() + leftLeaf.getWidth() + 20;
                        }
                        count++;
                    }
                    state = STATE_ANIMATING_CLOSED_LEFT;
                } else {
                    if (x < (getX() + leftLeaf.getWidth())*scale) {
                        dWidth = 0;
                        dHeight = 0;
                        state = STATE_ANIMATING_OPEN_LEFT;
                    } else {
                        float bx = getX() + getWidth() + 20;
                        float by = getY() + detailHeight - MyTreeActivity.buttonSize*2;
                        int count=1;
                        for (Bitmap button : spActivityButtons) {
                            if (x > bx * scale && y > by * scale
                                    && x < (bx + button.getWidth()) * scale && y < (by + button.getHeight()) * scale) {
                                sendEvent(button, node.getSpouse());
                                break;
                            }
                            bx += button.getWidth() + 20;
                            if (count % 3 == 0) {
                                bx = getX() + getWidth() + 20;
                                by+=MyTreeActivity.buttonSize;
                            }
                            count++;
                        }
                        state = STATE_ANIMATING_CLOSED_RIGHT;
                    }
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

    public boolean inSprite(float tx, float ty) {
        if (!selectable) return false;
        if (state==STATE_OPEN_LEFT) {
            if (tx >= x * getScale() && tx <= (x + leftLeaf.getWidth()+detailWidth) * getScale() && ty >= y * getScale() && ty <= (y + detailHeight) * getScale()) {
                return true;
            }
        } else if (state==STATE_OPEN_RIGHT) {
            if (tx >= x * getScale() && tx <= (x + getWidth()+detailWidth) * getScale() && ty >= y * getScale() && ty <= (y + detailHeight) * getScale()) {
                return true;
            }
        } else {
            if (tx >= x * getScale() && tx <= (x + width) * getScale() && ty >= y * getScale() && ty <= (y + height) * getScale()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOpened() {
        return opened;
    }


    public void sendEvent(Bitmap button, LittlePerson person) {
        if (button==activity.getMatchBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_MATCH, person);
        else if (button==activity.getPuzzleBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_PUZZLE, person);
        else if (button==activity.getPaintBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_COLORING, person);
        else if (button==activity.getPencilBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_SCRATCH, person);
        else if (button==activity.getBubbleBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_BUBBLES, person);
        else
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_DRESSUP, person);
    }
}
