package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.HeritagePath;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.util.ColorHelper;
import com.yellowforktech.littlefamilytree.util.ImageHelper;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 3/27/2015.
 */
public class PersonHeritageChartView extends SurfaceView implements SurfaceHolder.Callback {
    private LittlePerson person;
    private Bitmap outlineBitmap;
    private List<HeritagePath> cultures;
    private Context context;
    private AnimationThread animationThread;
    private List<SelectedPathListener> listeners;
    private float density;
	
	private HeritagePath selectedPath;

    private int[] colors = {
            Color.BLUE, Color.RED, Color.CYAN, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.LTGRAY
    };

    private int distance = 0;

    public PersonHeritageChartView(Context context) {
        super(context);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        cultures = null;
        listeners = new ArrayList<>();
    }

    public PersonHeritageChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        cultures = null;
        listeners = new ArrayList<>();
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
        if (person!=null) loadBitmap();
        cultures = null;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    private void loadBitmap() {
        if (person.getGender()== GenderType.Female) {
            outlineBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.girloutline, 0, this.getWidth(), this.getHeight());
        } else {
            outlineBitmap = ImageHelper.loadBitmapFromResource(context, R.drawable.boyoutline, 0, this.getWidth(), this.getHeight());
        }
    }

    public void setHeritageMap(List<HeritagePath> cultures) {
        this.cultures = cultures;
		if (cultures!=null && cultures.size() > 0) {
			selectedPath = cultures.get(0);
		}
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

    public HeritagePath getPathByCoords(float x, float y) {
        HeritagePath selectedPath = null;
        if (cultures!=null) {
            int top = this.getHeight();
            for(int p=cultures.size()-1; p>=0; p--) {
                HeritagePath path = cultures.get(p);
                int height = (int)(this.getHeight()*path.getPercent());
                if (height < 15*density) height = (int) (15*density);
                top = top - height;
                if (top < 0) {
                    height = height+top;
                    top = 0;
                }
                if (y>=top && y< top+height) {
                    selectedPath = path;
                    break;
                }
            }
        }
        return selectedPath;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        selectedPath = getPathByCoords(x, y);
        if (selectedPath!=null) {
            //fire listeners
            for(SelectedPathListener l : listeners) {
                l.onSelectedPath(selectedPath);
            }
        }
        /*
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:

                break;
        }*/
        return true;
    }

    public void doDraw(Canvas canvas) {
        if (outlineBitmap!=null) {
            canvas.drawARGB(255, 255, 255, 255);
            if (cultures!=null) {
                int top = this.getHeight();
                int count = 0;
                for(int p=cultures.size()-1; p>=0; p--) {
                    HeritagePath path = cultures.get(p);
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    int color2 = colors[count];
                    if (path==selectedPath) {
                        color2 = ColorHelper.lightenColor2(color2, 0.5f);
                    }
                    int height = (int) (this.getHeight()*path.getPercent());
                    if (height < 15*density) height = (int) (15*density);
                    top = top - height;
                    if (top < 0) {
                        height = height+top;
                        top = 0;
                    }
                    paint.setColor(color2);
                    canvas.drawRect(0, top, outlineBitmap.getWidth(),
                            top + height,
                            paint);

                    count++;
                    if (count >= colors.length) {
                        count = 0;
                    }
                }
            } else {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                paint.setShader(new LinearGradient(0, distance, 0, distance+200, Color.WHITE, Color.RED, Shader.TileMode.MIRROR));
                canvas.drawRect(0, 0, outlineBitmap.getWidth(), this.getHeight(), paint);
            }
            canvas.drawBitmap(outlineBitmap, 0, 0, null);

            if (cultures !=null) {
                int top = this.getHeight();
                for(int p=cultures.size()-1; p>=0; p--) {
                    HeritagePath path = cultures.get(p);
                    Paint paint = new Paint();
					int textSize = (int)(this.getHeight()*0.05);
					if (textSize > 20*density) textSize = (int) (20*density);
                    paint.setTextSize(textSize);
                    paint.setColor(Color.BLACK);
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setStrokeWidth(2);
					if (path==selectedPath) {
                        paint.setShadowLayer(3, 3, 3, Color.GRAY);
                        paint.setStrokeWidth(5);
					}
                    int height = (int) (this.getHeight()*path.getPercent());
                    if (height < 15*density) height = (int) (15*density);
                    top = top - height;
                    if (top < 0) {
                        height = height+top;
                        top = 0;
                    }
                    String text = String.format("%1$.2f%% %2$s", (path.getPercent()*100), path.getPlace());
                    canvas.drawText(text, this.getWidth() / 2 + 10, top + (height / 3), paint);
                    canvas.drawLine(this.getWidth()/2.5f, top+(height/2), this.getWidth()/2, top+(height/3)+4, paint);
                    canvas.drawLine(this.getWidth()/2, top+(height/3)+4, this.getWidth(), top+(height/3)+4, paint);
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
                    distance+=2;
                    //if (distance > getHeight()+200) distance = 0;
                    //if (distance > getHeight()+200) distance = 0;
                    view.doDraw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }

                try {
                    sleep(33);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void addListener(SelectedPathListener l) {
        listeners.add(l);
    }

    public interface SelectedPathListener {
        public void onSelectedPath(HeritagePath path);
    }
}
