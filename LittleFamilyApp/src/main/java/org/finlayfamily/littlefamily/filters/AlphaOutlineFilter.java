package org.finlayfamily.littlefamily.filters;

import android.graphics.Rect;

import com.jabistudio.androidjhlabs.filter.WholeImageFilter;

public class AlphaOutlineFilter extends WholeImageFilter {
    protected int outlineColor;
    protected int alphaColor = 0x00ffffff;
    protected int alphaThreshhold1 = 50;
    protected int alphaThreshhold2 = 100;
    protected int outlineWidth = 2;

    public AlphaOutlineFilter(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    @Override
    protected int[] filterPixels(int width, int height, int[] inPixels, Rect transformedSpace) {
        int index = 0;
        int[] outPixels = new int[width * height];
        int x = 0;
        int y = 0;
        //--top edge
        for (x = 0; x < width; x++) {
            int a2 = inPixels[y * width + x] & 0xff000000;
            if (a2 > alphaThreshhold2) {
                outPixels[index++] = outlineColor;
            } else {
                outPixels[index++] = alphaColor;
            }
        }
        for (y = 1; y < height-1; y++) {
            boolean flip = false;
            int count = 0;
            boolean showAlpha = true;
            for (x = 0; x < width-outlineWidth; x++) {
                if (x>0) {
                    int a1 = inPixels[y * width + x - 1] & 0xff000000;
                    int a2 = inPixels[y * width + x] & 0xff000000;
                    int u = inPixels[(y - 1) * width + x] & 0xff000000;

                    if (count > outlineWidth) {
                        showAlpha = true;
                        flip = !flip;
                    }

                    if (count==0) {
                        if (!flip && a1 < alphaThreshhold1 && (a2 > alphaThreshhold2 || u > alphaThreshhold2)) {
                            showAlpha = false;
                        } else if (flip && a1 > alphaThreshhold2 && (a2 < alphaThreshhold1 || u < alphaThreshhold1)) {
                            showAlpha = false;
                        }
                    }

                    if (showAlpha) {
                        outPixels[index++] = alphaColor;
                        count = 0;
                    } else {
                        outPixels[index++] = outlineColor;
                        count++;
                    }
                } else {
                    //-- left edge case
                    int a2 = inPixels[y * width + x] & 0xff000000;
                    if (a2 < alphaThreshhold1) {
                        outPixels[index++] = alphaColor;
                    } else {
                        outPixels[index++] = outlineColor;
                        count++;
                        showAlpha = false;
                        flip = !flip;
                    }
                }
            }
            showAlpha = true;
            //-- right edge case
            for (x=width-outlineWidth; x < width; x++) {
                int a2 = inPixels[y * width + x] & 0xff000000;
                if (showAlpha && a2 < alphaThreshhold1) {
                    outPixels[index++] = alphaColor;
                } else {
                    outPixels[index++] = outlineColor;
                    showAlpha = false;
                }
            }
        }
        //--bottom edge
        for (x = 0; x < width; x++) {
            int a2 = inPixels[y * width + x] & 0xff000000;
            if (a2 > alphaThreshhold2) {
                outPixels[index++] = outlineColor;
            } else {
                outPixels[index++] = alphaColor;
            }
        }
        return outPixels;
    }

    public String toString() {
        return "Filters/AlphaOutlineFilter";
    }
}
