package org.finlayfamily.littlefamily.filters;

import com.jabistudio.androidjhlabs.filter.PointFilter;

/**
 * Created by jfinlay on 3/23/2015.
 */
public class AlphaFilter extends PointFilter {
    public AlphaFilter() {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        int a = rgb & 0xff000000;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
		int avg = (r + g + b) / 3;	// simple average

        int v = 0x00ffffff;
        if (avg > 50) {
            v = a;
        }
        return v;
    }

    public String toString() {
        return "Filters/AlphaFilter";
    }
}
