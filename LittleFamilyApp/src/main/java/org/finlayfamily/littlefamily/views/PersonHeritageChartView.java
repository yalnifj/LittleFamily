package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.finlayfamily.littlefamily.R;
import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.util.ImageHelper;
import org.gedcomx.types.GenderType;

import java.util.Map;
import java.util.SortedMap;

/**
 * Created by jfinlay on 3/27/2015.
 */
public class PersonHeritageChartView extends SurfaceView implements SurfaceHolder.Callback {
    private LittlePerson person;
    private Bitmap outlineBitmap;
    private SortedMap<String, Double> cultures;
    private Context context;
    private AnimationThread animationThread;

    private int distance = 0;

    public PersonHeritageChartView(Context context) {
        super(context);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        cultures = null;
    }

    public PersonHeritageChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        cultures = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (person!=null) {
            loadBitmap();
        }
    }

    public void setPerson(LittlePerson person) {
        this.person = person;
        loadBitmap();
        cultures = null;
    }

    private void loadBitmap() {
        if (person.getGender()== GenderType.Female) {
            outlineBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.girloutline, 0, this.getWidth(), this.getHeight());
        } else {
            outlineBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.boyoutline, 0, this.getWidth(), this.getHeight());
        }
    }

    public void setHeritageMap(SortedMap<String, Double> cultures) {
        this.cultures = cultures;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        animationThread = new AnimationThread(holder, context, this);
        animationThread.setRunning(true);
        animationThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        animationThread.setRunning(false);
        boolean retry = true;
        while(retry)  {
            try {
                animationThread.join();
                retry = false;
            } catch(Exception e) {
                Log.v("Exception Occured", e.getMessage());
            }
        }
    }

    public void doDraw(Canvas canvas) {
        if (outlineBitmap!=null) {
            canvas.drawARGB(255, 255, 255, 255);
            if (cultures!=null) {
                int top = 0;
                int r = 255;
                int g = 0;
                int b = 0;
                int count = 0;
                for(Map.Entry<String, Double> entry : cultures.entrySet()) {
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    int color1 = Color.argb((int)(255*entry.getValue()), r, g, b);
                    int color2 = Color.argb(255, r, g, b);
                    paint.setShader(new LinearGradient(0, top, 0, top + (int)(this.getHeight()*entry.getValue()), color1, color2, Shader.TileMode.MIRROR));
                    canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
                    int t = b;
                    b = g;
                    g = r;
                    r = t;
                    count++;
                    if (count % 3 == 0) {
                        g += 100;
                        if (g > 255) {
                            g = 255;
                            b += 100;
                            if (b>255) b = 255;
                        }
                    }

                    top += this.getHeight()*entry.getValue();
                }
            } else {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                paint.setShader(new LinearGradient(0, 0, 0, this.getHeight() - distance, Color.WHITE, Color.RED, Shader.TileMode.MIRROR));
                canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
            }
            canvas.drawBitmap(outlineBitmap, 0, 0, null);

            if (cultures !=null) {
                int top = 0;
                for(Map.Entry<String, Double> entry : cultures.entrySet()) {
                    Paint p = new Paint();
                    p.setTextSize(10);
                    p.setColor(Color.BLACK);
                    p.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(entry.getKey(), 0, top, p);
                    top += this.getHeight()*entry.getValue();
                }
            }
        }
    }

    public class AnimationThread extends Thread {
        private SurfaceHolder holder;
        private Context context;
        private PersonHeritageChartView view;

        private boolean running;

        public AnimationThread(SurfaceHolder holder, Context context, PersonHeritageChartView view) {
            this.holder = holder;
            this.view = view;
            this.context = context;
        }

        public void setRunning(boolean r) {
            this.running = r;
        }

        @Override
        public void run() {
            super.run();

            while(running) {
                Canvas canvas = holder.lockCanvas();
                if(canvas != null) {
                    distance++;
                    if (distance > getHeight()) distance = 0;
                    view.doDraw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }

                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
