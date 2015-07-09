package org.finlayfamily.littlefamily.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import org.finlayfamily.littlefamily.data.PuzzlePiece;
import org.finlayfamily.littlefamily.games.PuzzleGame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfinlay on 5/14/2015.
 */
public class PuzzleSurfaceView extends AbstractTouchAnimatedSurfaceView {

    public static final int thumbnailHeight = 75;

    private PuzzleGame game;
    private Bitmap bitmap;
    private Paint outlinePaint;
    private Paint shadowPaint;
    private Paint backPaint;

    private int sRow;
    private int sCol;
    private PuzzlePiece selected = null;
    private int sx;
    private int sy;
    private int pieceWidth;
    private int pieceHeight;
    private boolean animating = false;
    private boolean checkGame = false;
    private boolean showHint = false;

    public PuzzleSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outlinePaint = new Paint();
        outlinePaint.setColor(Color.WHITE);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(1);

        backPaint = new Paint();
        backPaint.setColor(Color.WHITE);
        //backPaint.setAlpha(0);
        backPaint.setStyle(Paint.Style.FILL);

        shadowPaint = new Paint();
        shadowPaint.setColor(Color.GRAY);
        shadowPaint.setAlpha(200);
        shadowPaint.setStyle(Paint.Style.FILL);

        setTouchTolerance(10);
    }

    @Override
    public void doStep() {
        animating = false;
        for(int r=0; r<game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                PuzzlePiece pp = game.getPiece(r, c);
                if (pp.isAnimating()) {
                    if (pp.getX()==pp.getToX() && pp.getY()==pp.getToY()) {
                        pp.setAnimating(false);
                    } else {
                        int dx = 0;
                        int dy = 0;
                        if (pp.getX() != pp.getToX()) {
                            dx = (int) ((pp.getToX() - pp.getX()) / 3);
                            if (dx==0) {
                                if (pp.getToX() < pp.getX()) dx = -1;
                                else dx = 1;
                            }
                        }
                        if (pp.getY() != pp.getToY()) {
                            dy = (int) ((pp.getToY() - pp.getY()) / 3);
                            if (dy==0) {
                                if (pp.getToY() < pp.getY()) dy = -1;
                                else dy = 1;
                            }
                        }
                        pp.setX(pp.getX() + dx);
                        pp.setY(pp.getY() + dy);
                        //Log.d("PuzzleSurfaceView", "Animating r=" + r + " c=" + c + " x="+pp.getX()+" y="+pp.getY());
                        animating = true;
                    }
                }
            }
        }

        if (!animating && checkGame) {
            if (game.isCompleted()) {
                for(PuzzleCompleteListener l : listeners) {
                    l.onPuzzleComplete();
                }
            }
            checkGame = false;
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawRect(0, 0, getWidth(), getHeight(), backPaint);
        if (bitmap!=null) {
            int width = getWidth();
            int height = getHeight() - thumbnailHeight;
            pieceWidth = width / game.getCols();
            pieceHeight = height / game.getRows();
            int bWidth = bitmap.getWidth() / game.getCols();
            int bHeight = bitmap.getHeight() / game.getRows();

            //-- maintain image aspect ratio
            if (width > height) {
                float ratio = ((float) bWidth) / (float)bHeight;
                pieceWidth = (int)(pieceHeight * ratio);
            } else {
                float ratio = ((float) bHeight) / (float)(bWidth);
                pieceHeight = (int)(pieceWidth * ratio);
            }

            for(int r=0; r<game.getRows(); r++) {
                for(int c=0; c<game.getCols(); c++) {
                    int y = r * pieceHeight;
                    int x = c * pieceWidth;

                    PuzzlePiece pp = game.getPiece(r, c);
                    int by = pp.getRow() * bHeight;
                    int bx = pp.getCol() * bWidth;
                    Rect src = new Rect();
                    src.set(bx, by, bx + bWidth, by + bHeight);

                    if (!pp.isAnimating() && !pp.isSelected()) {
                        pp.setX(x);
                        pp.setY(y);
                    } else {
                        x = pp.getX();
                        y = pp.getY();
                    }
                    Rect dst = new Rect();
                    dst.set(x, y, x + pieceWidth, y + pieceHeight);
                    if (!pp.isSelected()) {
                        canvas.drawBitmap(bitmap, src, dst, outlinePaint);
                        if (!pp.isInPlace())
                            canvas.drawRect(dst, outlinePaint);
                    }
                }
            }

            if (selected!=null) {
                int by = selected.getRow() * bHeight;
                int bx = selected.getCol() * bWidth;
                Rect src = new Rect();
                src.set(bx, by, bx + bWidth, by + bHeight);
                Rect dst = new Rect();
                dst.set(selected.getX(), selected.getY(), selected.getX() + pieceWidth, selected.getY() + pieceHeight);
                canvas.drawRect((float) (dst.left + 10), (float) (dst.top + 10), (float) (dst.right + 10), (float) (dst.bottom + 10), shadowPaint);
                canvas.drawBitmap(bitmap, src, dst, outlinePaint);
                canvas.drawRect(dst, outlinePaint);
            }

            Rect dst = new Rect();
            float ratio = (float)bWidth / bHeight;
            dst.set(0, height, (int) (thumbnailHeight * ratio), height+thumbnailHeight);
            canvas.drawBitmap(bitmap, null, dst, null);

            if (showHint) {
                Rect dst1 = new Rect();
                dst1.set(0, 0, pieceWidth*game.getCols(), pieceHeight*game.getRows());
                canvas.drawBitmap(bitmap, null, dst1, null);
            }
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);
        selected = null;
        for(int r=0; r<game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                PuzzlePiece pp = game.getPiece(r, c);
                if (!pp.isInPlace() && !pp.isAnimating()) {
                    if (x>=pp.getX() && x <= pp.getX()+pieceWidth && y>=pp.getY() && y<=pp.getY()+pieceHeight) {
                        selected = pp;
                        pp.setSelected(true);
                        sRow = r;
                        sCol = c;
                        Log.d("PuzzleSurfaceView", "Selecting r=" + r + " c=" + c);
                        return;
                    }
                }
            }
        }

        if (bitmap!=null) {
            float ratio = (float) bitmap.getWidth() / bitmap.getHeight();
            if (x <= thumbnailHeight * ratio && y >= thumbnailHeight) {
                showHint = true;
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);
        showHint = false;
        if (selected!=null) {
            int col = (int) (x / pieceWidth);
            int row = (int) (y / pieceHeight);
            if (col >= game.getCols()) col = game.getCols()-1;
            if (row >= game.getRows()) row = game.getRows()-1;
            PuzzlePiece pp = game.getPiece(row, col);
            if (!pp.isInPlace()) {
                game.swap(row, col, sRow, sCol);
                pp.setToX(sCol * pieceWidth);
                pp.setToY(sRow * pieceHeight);
                if (sRow==pp.getRow() && sCol==pp.getCol()) {
                    pp.setInPlace(true);
                    checkGame = true;
                }
                pp.setAnimating(true);
                selected.setToX(col * pieceWidth);
                selected.setToY(row * pieceHeight);
                if (row==selected.getRow() && col==selected.getCol()) {
                    selected.setInPlace(true);
                    checkGame = true;
                }
            } else {
                selected.setToX(sCol * pieceWidth);
                selected.setToY(sRow * pieceHeight);
            }
            selected.setSelected(false);
            selected.setAnimating(true);
        }
        selected = null;

    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        if (selected!=null) {
            int dx = (int) (newX - oldX);
            int dy = (int) (newY - oldY);
            sx = selected.getX() + dx;
            sy = selected.getY() + dy;
            if (sx < 0) sx = 0;
            if (sy < 0) sy = 0;
            if (sx + pieceWidth > getWidth()) sx = getWidth() - pieceWidth;
            if (sy + pieceHeight > getHeight()) sy = getHeight() - pieceHeight;
            selected.setX(sx);
            selected.setY(sy);
            //Log.d("PuzzleSurfaceView", "Moving selected to sx="+sx+" sy="+sy);
        }
    }

    public PuzzleGame getGame() {
        return game;
    }

    public void setGame(PuzzleGame game) {
        this.game = game;
        this.invalidate();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        this.invalidate();
    }
    private List<PuzzleCompleteListener> listeners = new ArrayList<>();
    public void registerListener(PuzzleCompleteListener l) {
        listeners.add(l);
    }
    public interface PuzzleCompleteListener {
        public void onPuzzleComplete();
    }
}
