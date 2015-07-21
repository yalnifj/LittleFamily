package com.yellowforktech.littlefamilytree.util;

import android.graphics.Color;

/**
 * Created by jfinlay on 4/17/2015.
 */
public class ColorHelper {
    public static int lightenColor(int color,float factor){
        float r = Color.red(color)*factor;
        float g = Color.green(color)*factor;
        float b = Color.blue(color)*factor;
        int ir = Math.min(255,(int)r);
        int ig = Math.min(255,(int)g);
        int ib = Math.min(255,(int)b);
        int ia = Color.alpha(color);
        return(Color.argb(ia, ir, ig, ib));
    }

    public static int lightenColor2(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1.0f - factor * (1.0f - hsv[2]);
        return Color.HSVToColor(hsv);
    }
}
