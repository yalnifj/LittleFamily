package com.yellowforktech.littlefamilytree.filters;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by jfinlay on 1/27/2016.
 */
public class GPUImageAlphaMaskFilter extends GPUImageFilter {
    public static final String ALPHAMASK_FRAGMENT_SHADER = "" +
            "  varying highp vec2 textureCoordinate;\n" +
            "  \n" +
            "  uniform sampler2D inputImageTexture;\n" +
            "  uniform lowp float threshhold;\n" +
            "  uniform lowp vec3 maskColor;\n" +
            "  \n" +
            "  void main()\n" +
            "  {\n" +
            "      lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "      \n" +
            "      if (textureColor.a > threshhold) {\n" +
            "          gl_FragColor = vec4(maskColor, textureColor.a);\n" +
            "      } else {\n" +
            "          gl_FragColor = vec4(0, 0, 0, 0);\n" +
            "      }\n" +
            "  }\n";

    private int mThreshholdLocation;
    private int mMaskColorLocation;
    private float mThreshhold;
    private float[] maskColor;

    public GPUImageAlphaMaskFilter() {
        this(0.7f, new float[]{1f,1f,1f});

    }

    public GPUImageAlphaMaskFilter(final float threshhold, float[] color) {
        super(NO_FILTER_VERTEX_SHADER, ALPHAMASK_FRAGMENT_SHADER);
        mThreshhold = threshhold;
        this.maskColor = color;
    }

    @Override
    public void onInit() {
        super.onInit();
        mThreshholdLocation = GLES20.glGetUniformLocation(getProgram(), "threshhold");
        mMaskColorLocation = GLES20.glGetUniformLocation(getProgram(), "maskColor");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setThreshhold(mThreshhold);
        setMaskColor(maskColor);

    }

    public void setThreshhold(final float threshhold) {
        mThreshhold = threshhold;
        setFloat(mThreshholdLocation, mThreshhold);
    }

    public void setMaskColor(final float[] maskColor) {
        this.maskColor = maskColor;
        setFloatVec3(mMaskColorLocation, maskColor);
    }
}
