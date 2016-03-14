package com.yellowforktech.littlefamilytree.sprites;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	protected boolean showSpouse;
    protected DisplayMetrics dm;

    public TreePersonAnimatedSprite(TreeNode personNode, MyTreeActivity activity, Bitmap leftLeaf, Bitmap rightLeaf, boolean showSpouse) {
        super();
        dm = activity.getResources().getDisplayMetrics();
		this.showSpouse = showSpouse;
        this.node = personNode;
        this.activity = activity;
        this.leftLeaf = leftLeaf;
        this.rightLeaf = rightLeaf;
        this.selectable = true;
        this.setHeight(leftLeaf.getHeight());
        this.detailWidth = (int) (activity.getButtonSize()*5);
        this.detailHeight = (int) (activity.getButtonSize()*3);

        if (showSpouse) {
            this.setWidth(leftLeaf.getWidth() + rightLeaf.getWidth());
        } else {
            this.setWidth(leftLeaf.getWidth());
        }

        photo = null;
        if (node.getPerson()!=null) {
            if (node.getPerson().getPhotoPath() != null) {
                photo = ImageHelper.loadBitmapFromFile(node.getPerson().getPhotoPath(), ImageHelper.getOrientation(node.getPerson().getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
            }
            if (photo == null) {
                photo = ImageHelper.loadBitmapFromResource(activity, node.getPerson().getDefaultPhotoResource(), 0, (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7));
            }
            String place = "unknown";
            if (node.getPerson().getBirthPlace() != null) {
                place = node.getPerson().getBirthPlace();
            }
            DollConfig dollConfig = activity.getDressUpDolls().getDollConfig(PlaceHelper.getTopPlace(place), node.getPerson());
            String thumbnailFile = dollConfig.getThumbnail();
            try {
                InputStream is = activity.getAssets().open(thumbnailFile);
                dressUpBtn = ImageHelper.loadBitmapFromStream(is, 0, activity.getButtonSize(), activity.getButtonSize());
                if (node.getPerson().getGender() == GenderType.Female && showSpouse) {
                    spDressUpBtn = dressUpBtn;
                }
                is.close();
            } catch (IOException e) {
                Log.e("TreePersonAnimatedSprit", "Error opening asset file", e);
            }

            if (node.getPerson().getGender()==GenderType.Female) {
                mother = node.getPerson();
            } else {
                father = node.getPerson();
            }
        }

        spPhoto = null;
        if (showSpouse && node.getSpouse()!=null) {
            if (node.getSpouse().getPhotoPath() != null) {
                spPhoto = ImageHelper.loadBitmapFromFile(node.getSpouse().getPhotoPath(), ImageHelper.getOrientation(node.getSpouse().getPhotoPath()), (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7), false);
            }
            if (spPhoto==null) {
                spPhoto = ImageHelper.loadBitmapFromResource(activity, node.getSpouse().getDefaultPhotoResource(), 0, (int) (leftLeaf.getWidth() * 0.7), (int) (height * 0.7));
            }
            String place = "unknown";
            if (node.getSpouse().getBirthPlace() != null) {
                place = node.getSpouse().getBirthPlace();
            }
            DollConfig dollConfig = activity.getDressUpDolls().getDollConfig(PlaceHelper.getTopPlace(place), node.getSpouse());
            String thumbnailFile = dollConfig.getThumbnail();
            try {
                InputStream is = activity.getAssets().open(thumbnailFile);
                Bitmap spThumb = ImageHelper.loadBitmapFromStream(is, 0, activity.getButtonSize(), activity.getButtonSize());
                if (node.getSpouse().getGender()==GenderType.Female) {
                    spDressUpBtn = spThumb;
                } else {
                    dressUpBtn = spThumb;
                }
                is.close();
            } catch (IOException e) {
                Log.e("TreePersonAnimatedSprit", "Error opening asset file", e);
            }

            if (father==null) father = node.getSpouse();
            else mother = node.getSpouse();
        }

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(26*dm.density);
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
            dWidth += 15*dm.density;
            dHeight += 15*dm.density;
            if (dWidth > detailWidth) dWidth = detailWidth;
            if (dHeight > detailHeight) dHeight = detailHeight;
            if (dWidth==detailWidth && dHeight==detailHeight) {
                if (state==STATE_ANIMATING_OPEN_LEFT) {
                    state = STATE_OPEN_LEFT;
                    LittlePerson person = father;
                    if (person==null) person = mother;
                    try {
                        String relationship = node.getAncestralRelationship(person, activity);
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
                        String relationship = node.getAncestralRelationship(mother, activity);
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
            dWidth -= 15*dm.density;
            dHeight -= 15*dm.density;
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
        Rect dst = new Rect();
        dst.set((int)getX(), (int)getY(), (int)(getX()+leftLeaf.getWidth()), (int) (getY()+leftLeaf.getHeight()));
        if (showSpouse) {
            canvas.drawBitmap(leftLeaf, null, dst, null);
        } else {
            if (node.getPerson()!=null) {
                if (node.getPerson().getGender()!=null && node.getPerson().getGender() == GenderType.Male) {
                    canvas.drawBitmap(leftLeaf, null, dst, null);
                } else {
                    canvas.drawBitmap(rightLeaf, null, dst, null);
                }
            }
        }

        if (showSpouse) {
            int x = (int) getX();
            int y = (int) getY();
            x = x+leftLeaf.getWidth();
            dst = new Rect();
            dst.set(x, y, x+rightLeaf.getWidth(), y+rightLeaf.getHeight());
            canvas.drawBitmap(rightLeaf, null, dst, null);
        }

        Rect photoRect = new Rect();

        if (showSpouse && spPhoto!=null) {
            int x = (int) getX();
            int y = (int) getY();
            float ratio = ((float)spPhoto.getWidth())/spPhoto.getHeight();
            int pw = (int) (leftLeaf.getWidth()*0.6f);
            int ph = (int) (leftLeaf.getWidth()*0.6f);
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

        if (photo!=null) {
            float ratio = ((float) photo.getWidth()) / photo.getHeight();
            int pw = (int) (leftLeaf.getWidth() * 0.6f);
            int ph = (int) (leftLeaf.getWidth() * 0.6f);
            if (photo.getWidth() > photo.getHeight()) {
                ph = (int) (pw / ratio);
            } else {
                pw = (int) (ph * ratio);
            }
            int x = (int) getX();
            int y = (int) getY();
            int px = x + (leftLeaf.getWidth() / 2 - pw / 2);
            if (showSpouse && node.getPerson().getGender() == GenderType.Female) {
                px = px + leftLeaf.getWidth();
            }
            int py = y + (height / 2 - ph / 2);
            photoRect.set(px, py, px + pw, py + ph);
            canvas.drawBitmap(photo, null, photoRect, null);
            canvas.drawText(node.getPerson().getGivenName(), px + pw / 2, getY() + height, textPaint);
        }

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
        if (detailPerson!=null) {
            if (state == STATE_OPEN_LEFT) {
                if (showSpouse && node.getSpouse() != null && detailPerson.getGender() == GenderType.Female) {
                    detailPerson = node.getSpouse();
                }
                String name = detailPerson.getName();
                if (name == null) name = detailPerson.getGivenName();
                canvas.drawText(name, getX() + leftLeaf.getWidth() + detailWidth / 2, getY() + textPaint.getTextSize(), textPaint);
                String relationship = node.getAncestralRelationship(detailPerson, activity);
                canvas.drawText(relationship, getX() + leftLeaf.getWidth() + detailWidth / 2, getY() + textPaint.getTextSize()*2, textPaint);

                if (activityButtons==null) {
                    activityButtons = new ArrayList<>();
                    activityButtons.add(activity.getMatchBtn());
                    activityButtons.add(activity.getPuzzleBtn());
                    activityButtons.add(activity.getPaintBtn());
                    activityButtons.add(activity.getPencilBtn());
                    activityButtons.add(activity.getBubbleBtn());
                    activityButtons.add(activity.getSongBtn());
                    if (dressUpBtn != null) activityButtons.add(dressUpBtn);
                }

                float bx = getX() + leftLeaf.getWidth() + 10*dm.density;
                float by = getY() + detailHeight - (10*dm.density + activity.getButtonSize() * 2);
                int count = 1;
                for (Bitmap button : activityButtons) {
                    if (button!=null) {
                        canvas.drawBitmap(button, bx, by, null);
                        bx += button.getWidth() + 10*dm.density;
                        if (count % 4 == 0) {
                            bx = getX() + leftLeaf.getWidth() + 10*dm.density;
                            by += activity.getButtonSize();
                        }
                        count++;
                    }
                }
            } else if (state == STATE_OPEN_RIGHT) {
                if (node.getSpouse() != null && detailPerson.getGender() != GenderType.Female) {
                    detailPerson = node.getSpouse();
                }
                String name = detailPerson.getName();
                if (name == null) name = detailPerson.getGivenName();
                canvas.drawText(name, getX() + getWidth() + detailWidth / 2, getY() + textPaint.getTextSize(), textPaint);
                String relationship = node.getAncestralRelationship(detailPerson, activity);
                canvas.drawText(relationship, getX() + getWidth() + detailWidth / 2, getY() + textPaint.getTextSize()*2, textPaint);

                if (spActivityButtons==null) {
                    spActivityButtons = new ArrayList<>();
                    spActivityButtons.add(activity.getMatchBtn());
                    spActivityButtons.add(activity.getPuzzleBtn());
                    spActivityButtons.add(activity.getPaintBtn());
                    spActivityButtons.add(activity.getPencilBtn());
                    spActivityButtons.add(activity.getBubbleBtn());
                    spActivityButtons.add(activity.getSongBtn());
                    if (spDressUpBtn != null) spActivityButtons.add(spDressUpBtn);
                }

                float bx = getX() + getWidth() + 10*dm.density;
                float by = getY() + detailHeight - (10*dm.density + activity.getButtonSize() * 2);
                int count = 1;
                for (Bitmap button : spActivityButtons) {
                    if (button!=null) {
                        canvas.drawBitmap(button, bx, by, null);
                        bx += button.getWidth() + 10*dm.density;
                        if (count % 4 == 0) {
                            bx = getX() + getWidth() + 10*dm.density;
                            by += activity.getButtonSize();
                        }
                        count++;
                    }
                }
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
        if (Math.abs(newX - oldX) > 6*dm.density || Math.abs(newY - oldY) > 6*dm.density ) {
            moved = true;
        }
        return false;
    }

    @Override
    public void onRelease(float x, float y) {
        if (!moved && node.getPerson()!=null) {
            if (!activity.isTreeSearchGameActive()) {
                if (!opened) {
                    dWidth = 0;
                    dHeight = 0;
                    if (!showSpouse) {
                        state = STATE_ANIMATING_OPEN_LEFT;
                    } else {
                        if (x < (getX() + leftLeaf.getWidth()) * scale) {
                            state = STATE_ANIMATING_OPEN_LEFT;
                        } else {
                            state = STATE_ANIMATING_OPEN_RIGHT;
                        }
                    }
                } else if (state != STATE_ANIMATING_CLOSED_LEFT && state != STATE_ANIMATING_CLOSED_RIGHT) {
                    dWidth = detailWidth;
                    dHeight = detailHeight;
                    if (state == STATE_OPEN_LEFT) {
                        float bx = getX() + leftLeaf.getWidth() + 20;
                        float by = getY() + detailHeight - activity.getButtonSize() * 2;
                        int count = 1;
                        for (Bitmap button : activityButtons) {
                            if (button!=null) {
                                if (x > bx * scale && y > by * scale
                                        && x < (bx + button.getWidth()) * scale && y < (by + button.getHeight()) * scale) {
                                    LittlePerson sendPerson = node.getPerson();
                                    if (sendPerson.getGender()==GenderType.Female) sendPerson = node.getSpouse();
                                    sendEvent(button, sendPerson);
                                    break;
                                }
                                bx += button.getWidth() + 20;
                                if (count % 4 == 0) {
                                    by += activity.getButtonSize();
                                    bx = getX() + leftLeaf.getWidth() + 20;
                                }
                                count++;
                            }
                        }
                        state = STATE_ANIMATING_CLOSED_LEFT;
                    } else {
                        if (x < (getX() + leftLeaf.getWidth()) * scale) {
                            dWidth = 0;
                            dHeight = 0;
                            state = STATE_ANIMATING_OPEN_LEFT;
                        } else {
                            float bx = getX() + getWidth() + 20;
                            float by = getY() + detailHeight - activity.getButtonSize() * 2;
                            int count = 1;
                            for (Bitmap button : spActivityButtons) {
                                if (button!=null) {
                                    if (x > bx * scale && y > by * scale
                                            && x < (bx + button.getWidth()) * scale && y < (by + button.getHeight()) * scale) {
                                        LittlePerson sendPerson = node.getPerson();
                                        if (sendPerson.getGender()!=GenderType.Female) sendPerson = node.getSpouse();
                                        sendEvent(button, sendPerson);
                                        break;
                                    }
                                    bx += button.getWidth() + 20;
                                    if (count % 4 == 0) {
                                        bx = getX() + getWidth() + 20;
                                        by += activity.getButtonSize();
                                    }
                                    count++;
                                }
                            }
                            state = STATE_ANIMATING_CLOSED_RIGHT;
                        }
                    }
                }
            }

            if (surfaceView != null) {
                surfaceView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
            }

            //-- fire person selected event
            Map<String, Object> params = new HashMap<>();
            params.put("node", getNode());
            boolean isSpouse = false;
            if (node.getSpouse()!=null) {
                if (x >= (getX() + leftLeaf.getWidth()) * scale) {
                    if (node.getSpouse().getGender()==GenderType.Female) {
                        isSpouse = true;
                    }
                } else {
                    if (node.getPerson().getGender()==GenderType.Female) {
                        isSpouse = true;
                    }
                }
            }
            params.put("isSpouse", isSpouse);
            params.put("sprite", this);
            EventQueue.getInstance().publish(MyTreeActivity.TOPIC_PERSON_SELECTED, params);
        }
        selected = false;
        moved = false;
    }

    @Override
    public void onDestroy() {
        leftLeaf = null;
        rightLeaf = null;
        if (photo!=null) {
            photo.recycle();
            photo = null;
        }
        if (spPhoto!=null) {
            spPhoto.recycle();
            spPhoto = null;
        }
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
        else if (button==activity.getSongBtn())
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_SONG, person);
        else
            EventQueue.getInstance().publish(LittleFamilyActivity.TOPIC_START_DRESSUP, person);
    }
}
