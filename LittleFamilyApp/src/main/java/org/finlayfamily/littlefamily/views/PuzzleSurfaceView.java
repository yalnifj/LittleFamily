package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import org.finlayfamily.littlefamily.games.PuzzleGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 5/14/2015.
 */
public class PuzzleSurfaceView extends AbstractTouchAnimatedSurfaceView {

    private PuzzleGame game;
    private Bitmap bitmap;
    private Paint outlinePaint;

    private int sRow;
    private int sCol;


    public PuzzleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1);
    }

    @Override
    public void doStep() {

    }

    @Override
    public void doDraw(Canvas canvas) {
        if (bitmap!=null) {
            int width = getWidth();
            int height = getHeight();
            int pieceWidth = width / game.getCols();
            int pieceHeight = height / game.getRows();
            int bWidth = bitmap.getWidth() / game.getCols();
            int bHeight = bitmap.getHeight() / game.getRows();

            //-- maintain image aspect ratio
            if (width > height) {
                float ratio = pieceHeight / bHeight;
                pieceWidth = (int)(pieceWidth * ratio);
            } else {
                float ratio = ((float) pieceWidth) / (float)(bWidth);
                pieceHeight = (int)(pieceHeight * ratio);
            }

            for(int r=0; r<game.getRows(); r++) {
                int y = r * pieceHeight;
                for(int c=0; c<game.getCols(); c++) {
                    int x = c * pieceWidth;

                    int loc = game.getSection(r, c);
                    int by = (loc/game.getCols()) * bHeight;
                    int bx = (loc%game.getCols()) * bWidth;
                    Rect src = new Rect();
                    src.set(bx, by, bx + bWidth, by + bHeight);
                    Rect dst = new Rect();
                    dst.set(x, y, x+pieceWidth, y+pieceHeight);
                    canvas.drawBitmap(bitmap, src, dst, outlinePaint);
                    canvas.drawRect(dst, outlinePaint);
                }
            }
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {

    }

    public PuzzleGame getGame() {
        return game;
    }

    public void setGame(PuzzleGame game) {
        this.game = game;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    private List<PuzzleCompleteListener> listeners = new ArrayList<>();
    public void registerListener(PuzzleCompleteListener l) {
        listeners.add(l);
    }
    public interface PuzzleCompleteListener {
        public void onPuzzleComplete();
    }
}
