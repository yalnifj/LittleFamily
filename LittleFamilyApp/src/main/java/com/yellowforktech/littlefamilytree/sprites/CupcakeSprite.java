package com.yellowforktech.littlefamilytree.sprites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by jfinlay on 3/30/2016.
 */
public class CupcakeSprite extends TouchEventGameSprite {
    private LittlePerson person;
    private Bitmap photo;
    private Context context;
    private Paint textPaint;

    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public CupcakeSprite(Bitmap bitmap, LittlePerson person, Context context, String eventTopic, DisplayMetrics dm) {
        super(bitmap, eventTopic, dm);
        this.person = person;
        this.context = context;
    }

    public LittlePerson getPerson() {
        return person;
    }

    @Override
    public void doDraw(Canvas canvas) {
        super.doDraw(canvas);

        if (photo==null) {
            if (person.getPhotoPath() != null) {
                photo = ImageHelper.loadBitmapFromFile(person.getPhotoPath(), ImageHelper.getOrientation(person.getPhotoPath()), width/2, width/2, false);
            }
            if (photo == null) {
                photo = ImageHelper.loadBitmapFromResource(context, person.getDefaultPhotoResource(), 0, width/2, width/2);
            }
        }
        if (photo!= null) {
            canvas.drawBitmap(photo, getX()+width/4, getY()+height/3, null);
        }
        if (person!=null) {
            if (textPaint==null) {
                textPaint = new Paint();
                textPaint.setTextSize(width / 10);
                textPaint.setColor(Color.parseColor("#552200"));
            }
            float textTop = getY() + height / 3 + width / 1.5f;
            if (person.getName()!=null) {
                Rect bounds = new Rect();
                textPaint.getTextBounds(person.getName(), 0, person.getName().length(), bounds);
                float dx = (getWidth() - bounds.width()) / 2;
                if (dx < 0) {
                    textPaint.setTextSize(textPaint.getTextSize() * 0.8f);
                    textPaint.getTextBounds(person.getName(), 0, person.getName().length(), bounds);
                    dx = (getWidth() - bounds.width()) / 2;
                }
                canvas.drawText(person.getName(), getX() + dx, textTop, textPaint);
            }
            if (person.getBirthDate()!=null) {
                Rect bounds = new Rect();
                String date = dateFormat.format(person.getBirthDate());
                textPaint.getTextBounds(date, 0, date.length(), bounds);
                float dx = (getWidth() - bounds.width()) / 2;
                canvas.drawText(date, getX() + dx, textTop + textPaint.getTextSize(), textPaint);
            }
            if (person.getAge() != null) {
                int age = person.getAge();
                Calendar now = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.setTime(person.getBirthDate());
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                if (cal.after(now)) {
                    age++;
                }
                Rect bounds = new Rect();
                String ageStr = "Age "+age;
                textPaint.getTextBounds(ageStr, 0, ageStr.length(), bounds);
                float dx = (getWidth() - bounds.width()) / 2;
                canvas.drawText(ageStr, getX() + dx, textTop + textPaint.getTextSize()*2, textPaint);
            }
        }
    }
}
