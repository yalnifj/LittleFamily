package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.LruCache;

import com.yellowforktech.littlefamilytree.sprites.AnimatedBitmapSprite;
import com.yellowforktech.littlefamilytree.sprites.Sprite;
import com.yellowforktech.littlefamilytree.sprites.StarSprite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by jfinlay on 5/8/2015.
 */
public class SpritedClippedSurfaceView extends AbstractTouchAnimatedSurfaceView {
    protected List<Sprite> sprites;
    protected Sprite backgroundSprite;
	protected Bitmap starBitmap;
    protected Bitmap redStarBitmap;
    protected Paint basePaint;
    protected List<Sprite> selectedSprites;

    protected int clipX;
    protected int clipY;
    protected int maxWidth;
    protected int maxHeight;
	protected int starCount = 0;
	protected int starDelay = 3;
	protected int starDelayCount = 3;
	protected Rect starRect;
    protected boolean starsInRect;
    protected int redStarCount = 0;
    protected int redStarDelay = 3;
    protected int redStarDelayCount = 3;
    protected Rect redStarRect;
    protected boolean redStarsInRect;
	protected Random random;
    protected int spriteCacheSize = 3;
    protected SpriteLruCache spriteCache;
    protected long spriteCacheCount = 0;

    public SpritedClippedSurfaceView(Context context) {
        super(context);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
        basePaint.setStyle(Paint.Style.FILL);
        basePaint.setColor(Color.WHITE);
		random = new Random();
        spriteCache = new SpriteLruCache(spriteCacheSize);
    }

    public SpritedClippedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
		random = new Random();
        spriteCache = new SpriteLruCache(spriteCacheSize);
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void setSprites(List<Sprite> sprites) {
        this.sprites = sprites;
    }

    public Sprite getBackgroundSprite() {
        return backgroundSprite;
    }

    public void setBackgroundSprite(Sprite backgroundSprite) {
        this.backgroundSprite = backgroundSprite;
    }

    public void addSprite(Sprite s) {
        if (sprites==null) {
            sprites = new ArrayList<>();
        }
        synchronized (sprites) {
            sprites.add(s);
            s.setSurfaceView(this);
        }
    }

    public void removeSprite(Sprite s) {
        if (sprites != null) {
            synchronized (sprites) {
                sprites.remove(s);
            }
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getClipX() {
        return clipX;
    }

    public void setClipX(int clipX) {
        this.clipX = clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public void setClipY(int clipY) {
        this.clipY = clipY;
    }

    @Override
    public void doStep() {
        if (sprites!=null) {
            synchronized (sprites) {
                Iterator<Sprite> i = sprites.iterator();
                while (i.hasNext()) {
                    Sprite s = i.next();
                    s.doStep();
                    if (s.isRemoveMe()) i.remove();
                }
            }
        }
		
		if (starCount > 0) {
			if (starDelayCount<=0) {
				starDelayCount = starDelay;
				starCount--;
				if (starBitmap!=null && starRect != null) {
					StarSprite star = new StarSprite(starBitmap, true, true);
					int x=0, y=0;
					if (starsInRect) {
						x = (int) (starRect.left + random.nextInt(starRect.right - starRect.left));
						y = (int) (starRect.top + random.nextInt(starRect.bottom - starRect.top));
					} else {
						int side = random.nextInt(4);
						switch (side) {
							case 0:
								x = starRect.left + random.nextInt(starRect.right - starRect.left);
								y = starRect.top + random.nextInt(starBitmap.getHeight());
								break;
							case 1:
								x = starRect.right - random.nextInt(starBitmap.getWidth());
								y = starRect.top + random.nextInt(starRect.bottom - starRect.top);
								break;
							case 2:
								x = starRect.left + random.nextInt(starRect.right - starRect.left);
								y = starRect.bottom - random.nextInt(starBitmap.getHeight());
								break;
							case 3:
								x = starRect.left + random.nextInt(starBitmap.getWidth());
								y = starRect.top + random.nextInt(starRect.bottom - starRect.top);
								break;
						}
					}
					star.setX(x);
					star.setY(y);
					addSprite(star);
				}
			} else {
				starDelayCount--;
			}
		}

        if (redStarCount > 0) {
            if (redStarDelayCount<=0) {
                redStarDelayCount = redStarDelay;
                redStarCount--;
                if (redStarBitmap!=null && redStarRect != null) {
                    StarSprite star = new StarSprite(redStarBitmap, true, true);
                    int x=0, y=0;
                    if (starsInRect) {
                        x = (int) (redStarRect.left + random.nextInt(redStarRect.right - redStarRect.left));
                        y = (int) (redStarRect.top + random.nextInt(redStarRect.bottom - redStarRect.top));
                    } else {
                        int side = random.nextInt(4);
                        switch (side) {
                            case 0:
                                x = redStarRect.left + random.nextInt(redStarRect.right - redStarRect.left);
                                y = redStarRect.top + random.nextInt(redStarBitmap.getHeight());
                                break;
                            case 1:
                                x = redStarRect.right - random.nextInt(redStarBitmap.getWidth());
                                y = redStarRect.top + random.nextInt(redStarRect.bottom - redStarRect.top);
                                break;
                            case 2:
                                x = redStarRect.left + random.nextInt(redStarRect.right - redStarRect.left);
                                y = redStarRect.bottom - random.nextInt(redStarBitmap.getHeight());
                                break;
                            case 3:
                                x = redStarRect.left + random.nextInt(redStarBitmap.getWidth());
                                y = redStarRect.top + random.nextInt(redStarRect.bottom - redStarRect.top);
                                break;
                        }
                    }
                    star.setX(x);
                    star.setY(y);
                    addSprite(star);
                }
            } else {
                redStarDelayCount--;
            }
        }
    }

    @Override
    public void doDraw(Canvas canvas) {
        canvas.drawRect(0,0,getWidth(),getHeight(), basePaint);
        if (backgroundSprite!=null) {
            backgroundSprite.setWidth(this.getWidth());
            backgroundSprite.setHeight(this.getHeight());
            backgroundSprite.doDraw(canvas);
        }

        canvas.translate(-clipX, -clipY);
        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= clipX && s.getX() <= getWidth() + clipX && s.getY() + s.getHeight() >= clipY && s.getY() <= getHeight() + clipY) {
                    s.setVisible(true);
                    Matrix m = s.getMatrix();
                    Matrix old = null;
                    if (m != null) {
                        old = new Matrix();
                        old.set(m);
                        m.postTranslate(-clipX, -clipY);
                    }
                    s.doDraw(canvas);
                    if (m != null) {
                        m.set(old);
                    }
                } else {
                    s.setVisible(false);
                }
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        boolean selectedMoved = false;
        if (selectedSprites.size() > 0) {
            for (Sprite s : selectedSprites) {
                selectedMoved |= s.onMove(oldX+clipX, oldY+clipY, newX+clipX, newY+clipY);
            }
        }
        if (!selectedMoved) {
            float bNewX = newX;
            float bNewY = newY;
            if (maxWidth <= getWidth()) {
                bNewX = oldX;
            }
            if (maxHeight <= getHeight()) {
                bNewY = oldY;
            }
            backgroundSprite.onMove(oldX, oldY, bNewX, bNewY);
            clipX -= (newX-oldX);
            clipY -= (newY-oldY);

            if (maxWidth <= getWidth()) {
                clipX = 0;
            } else {
                if (clipX < 0) clipX = 0;
                else if (clipX + getWidth() > maxWidth) clipX = maxWidth - getWidth();
            }

            if (maxHeight <= getHeight()) {
                clipY = 0;
            } else {
                if (clipY < 0) clipY = 0;
                else if (clipY + getHeight() > maxHeight) clipY = maxHeight - getHeight();
            }
        }
    }

    @Override
    protected void touch_start(float x, float y){
            super.touch_start(x, y);

        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.inSprite(x + clipX, y + clipY)) {
                    selectedSprites.add(s);
                    s.onSelect(x + clipX, y + clipY);
                }
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        for(Sprite s : selectedSprites) {
            s.onRelease(x + clipX, y + clipY);
            if (s instanceof AnimatedBitmapSprite) {
                spriteCache.put(spriteCacheCount++, (AnimatedBitmapSprite)s);
            }
        }
        selectedSprites.clear();
    }

    public void onDestroy() {
        synchronized (sprites) {
            List<Sprite> spriteList = sprites;
            sprites = null;
            for (Sprite s : spriteList) {
                s.onDestroy();
            }
            spriteList.clear();
        }
    }
	
	public Bitmap getRedStarBitmap() {
        return redStarBitmap;
    }

    public void setRedStarBitmap(Bitmap starBitmap) {
        this.redStarBitmap = starBitmap;
    }

    public Bitmap getStarBitmap() {
        return starBitmap;
    }

    public void setStarBitmap(Bitmap starBitmap) {
        this.starBitmap = starBitmap;
    }
	
	public void addStars(Rect rect, boolean starsInRect, int count) {
		starRect = rect;
		this.starsInRect = starsInRect;
		this.starCount = count;
	}

    public void addRedStars(Rect rect, boolean starsInRect, int count) {
        redStarRect = rect;
        this.redStarsInRect = starsInRect;
        this.redStarCount = count;
    }

    public class SpriteLruCache extends LruCache<Long, AnimatedBitmapSprite> {
        public SpriteLruCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, Long key, AnimatedBitmapSprite oldValue, AnimatedBitmapSprite newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            oldValue.freeStates();
            System.gc();
        }
    }
}
