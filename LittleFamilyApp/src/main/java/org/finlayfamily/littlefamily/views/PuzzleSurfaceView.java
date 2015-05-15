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
    private Paint shadowPaint;
    private Paint backPaint;

    private int sRow;
    private int sCol;
    private int sLoc;
    private boolean selected = false;
    private int sx;
    private int sy;
    private int pieceWidth;
    private int pieceHeight;

    public PuzzleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1);

        backPaint = new Paint();
        backPaint.setColor(Color.WHITE);
        backPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.GRAY);
        //shadowPaint.setAlpha(100);
        shadowPaint.setStyle(Paint.Style.FILL);

        setTouchTolerance(10);
    }

    @Override
    public void doStep() {

    }

    @Override
    public void doDraw(Canvas canvas) {
        if (bitmap!=null) {
            int width = getWidth();
            int height = getHeight();
            pieceWidth = width / game.getCols();
            pieceHeight = height / game.getRows();
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
                    int loc = game.getSection(r, c);
                    int by = (loc / game.getCols()) * bHeight;
                    int bx = (loc % game.getCols()) * bWidth;
                    Rect src = new Rect();
                    src.set(bx, by, bx + bWidth, by + bHeight);
                    int x = c * pieceWidth;
                    Rect dst = new Rect();
                    dst.set(x, y, x + pieceWidth, y + pieceHeight);
                    if (!selected || loc!=sLoc) {
                        canvas.drawBitmap(bitmap, src, dst, outlinePaint);
                        if (!game.inPlace(r, c))
                            canvas.drawRect(dst, outlinePaint);
                    } else {
                        canvas.drawRect(dst, backPaint);
                    }
                }
            }

            if (selected) {
                //int loc = game.getSection(sRow, sCol);
                int by = (sLoc / game.getCols()) * bHeight;
                int bx = (sLoc % game.getCols()) * bWidth;
                Rect src = new Rect();
                src.set(bx, by, bx + bWidth, by + bHeight);
                Rect dst = new Rect();
                canvas.drawRect((float)(dst.left-5), (float)(dst.top-5), (float)(dst.right+5), (float)(dst.bottom+5), shadowPaint);
                dst.set(sx, sy, sx + pieceWidth, sy + pieceHeight);
                canvas.drawBitmap(bitmap, src, dst, outlinePaint);
                canvas.drawRect(dst, outlinePaint);
            }
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);
        int col = (int) (x / pieceWidth);
        int row = (int) (y / pieceHeight);
        if (col >= game.getCols()) col = game.getCols()-1;
        if (row >= game.getRows()) row = game.getRows()-1;
        sLoc = game.getLoc(row, col);
        int val = game.getSection(row, col);
        if (val!=sLoc) {
            sRow = row;
            sCol = col;
            selected = true;
            sx = col * pieceWidth;
            sy = row * pieceHeight;
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
        if (selected) {
            int col = (int) (x / pieceWidth);
            int row = (int) (y / pieceHeight);
            if (col >= game.getCols()) col = game.getCols()-1;
            if (row >= game.getRows()) row = game.getRows()-1;
            int loc = game.getLoc(row, col);
            if (!game.inPlace(row, col) && loc!=sLoc) {
                game.swap(row, col, sRow, sCol);
            }

            if (game.isCompleted()) {
                for(PuzzleCompleteListener l : listeners) {
                    l.onPuzzleComplete();
                }
            }
        }
        selected = false;

    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        int dx = (int) (newX - oldX);
        int dy = (int) (newY - oldY);
        sx = sx + dx;
        sy = sy + dy;
        if (sx < 0) sx = 0;
        if (sy < 0) sy = 0;
        if (sx + pieceWidth > getWidth()) sx = getWidth() - pieceWidth;
        if (sy + pieceHeight > getHeight()) sy = getHeight() - pieceHeight;
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
