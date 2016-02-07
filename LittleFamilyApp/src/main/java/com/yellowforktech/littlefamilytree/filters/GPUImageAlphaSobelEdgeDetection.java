package com.yellowforktech.littlefamilytree.filters;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImage3x3TextureSamplingFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;

/**
 * Created by jfinlay on 1/27/2016.
 */
public class GPUImageAlphaSobelEdgeDetection extends GPUImageFilterGroup {
    public static final String SOBEL_EDGE_DETECTION = "" +
            "precision mediump float;\n" +
            "\n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 leftTextureCoordinate;\n" +
            "varying vec2 rightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 topTextureCoordinate;\n" +
            "varying vec2 topLeftTextureCoordinate;\n" +
            "varying vec2 topRightTextureCoordinate;\n" +
            "\n" +
            "varying vec2 bottomTextureCoordinate;\n" +
            "varying vec2 bottomLeftTextureCoordinate;\n" +
            "varying vec2 bottomRightTextureCoordinate;\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform lowp vec3 outlineColor;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    float bottomLeftIntensity = texture2D(inputImageTexture, bottomLeftTextureCoordinate).r;\n" +
            "    float topRightIntensity = texture2D(inputImageTexture, topRightTextureCoordinate).r;\n" +
            "    float topLeftIntensity = texture2D(inputImageTexture, topLeftTextureCoordinate).r;\n" +
            "    float bottomRightIntensity = texture2D(inputImageTexture, bottomRightTextureCoordinate).r;\n" +
            "    float leftIntensity = texture2D(inputImageTexture, leftTextureCoordinate).r;\n" +
            "    float rightIntensity = texture2D(inputImageTexture, rightTextureCoordinate).r;\n" +
            "    float bottomIntensity = texture2D(inputImageTexture, bottomTextureCoordinate).r;\n" +
            "    float topIntensity = texture2D(inputImageTexture, topTextureCoordinate).r;\n" +
            "    float h = -topLeftIntensity - 2.0 * topIntensity - topRightIntensity + bottomLeftIntensity + 2.0 * bottomIntensity + bottomRightIntensity;\n" +
            "    float v = -bottomLeftIntensity - 2.0 * leftIntensity - topLeftIntensity + bottomRightIntensity + 2.0 * rightIntensity + topRightIntensity;\n" +
            "\n" +
            "    float mag = length(vec2(h, v));\n" +
            "\n" +
            "    if (mag > 0.2) {\n" +
            "        gl_FragColor = vec4(outlineColor, 1.0);\n" +
            "    } else {\n" +
            "        gl_FragColor = vec4(0, 0, 0, 0);\n" +
            "    }\n" +
            "}";

    private float[] outlineColor;
    private int mOutlineColorLocation;

    public GPUImageAlphaSobelEdgeDetection() {
        this(new float[]{0f, 0f, 0f});
    }

    public GPUImageAlphaSobelEdgeDetection(float[] outlineColor) {
        super();
        this.outlineColor = outlineColor;
        addFilter(new GPUImageGrayscaleFilter());
        addFilter(new GPUImage3x3TextureSamplingFilter(SOBEL_EDGE_DETECTION));
    }

    @Override
    public void onInit() {
        super.onInit();
        mOutlineColorLocation = GLES20.glGetUniformLocation(getProgram(), "outlineColor");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        setOutlineColor(outlineColor);
    }

    public void setLineSize(final float size) {
        ((GPUImage3x3TextureSamplingFilter) getFilters().get(1)).setLineSize(size);
    }

    public void setOutlineColor(final float[] outlineColor) {
        this.outlineColor = outlineColor;
        setFloatVec3(mOutlineColorLocation, outlineColor);
    }
}
