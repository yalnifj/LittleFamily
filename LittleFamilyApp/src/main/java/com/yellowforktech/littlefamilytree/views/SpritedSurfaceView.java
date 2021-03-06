package com.yellowforktech.littlefamilytree.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.SurfaceHolder;

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
public class SpritedSurfaceView extends AbstractTouchAnimatedSurfaceView {
    protected List<Sprite> sprites;
    protected Bitmap backgroundBitmap;
	protected Bitmap starBitmap;
    protected Paint basePaint;
    protected List<Sprite> selectedSprites;
    protected boolean multiSelect = true;
	protected int starCount = 0;
	protected int starDelay = 3;
	protected int starDelayCount = 3;
	protected Rect starRect;
	protected boolean starsInRect;
	protected Random random;
    protected int spriteCacheSize = 3;
    protected SpriteLruCache spriteCache;
    protected long spriteCacheCount = 0;

    public SpritedSurfaceView(Context context) {
        super(context);
        sprites = new ArrayList<>();
        selectedSprites = new ArrayList<>();
        basePaint = new Paint();
		random = new Random();
        spriteCache = new SpriteLruCache(spriteCacheSize);
    }

    public SpritedSurfaceView(Context context, AttributeSet attrs) {
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

    public Bitmap getBackgroundBitmap() {
        return backgroundBitmap;
    }

    public void setBackgroundBitmap(Bitmap background) {
        this.backgroundBitmap = background;
    }

    public void addSprite(Sprite s) {
        synchronized (sprites) {
            sprites.add(s);
            s.setSurfaceView(this);
        }
    }

    public void addSprite(Sprite s, Sprite before) {
        synchronized (sprites) {
            int pos = sprites.indexOf(before);
            if (pos >=0) {
                sprites.add(pos, s);
            } else {
                sprites.add(s);
            }
            s.setSurfaceView(this);
        }
    }

    public void removeSprite(Sprite s) {
        synchronized (sprites) {
            sprites.remove(s);
            s.setSurfaceView(null);
        }
    }

    @Override
    public void doStep() {
        synchronized (sprites) {
            try {
                Iterator<Sprite> i = sprites.iterator();
                while (i.hasNext()) {
                    Sprite s = i.next();
                    s.doStep();
                    if (s.isRemoveMe()) i.remove();
                }
            } catch(Exception e) {
                Log.e("SpritedSurfaceView", "error stepping sprites", e);
                return;
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
    }

    @Override
    public void doDraw(Canvas canvas) {
        if (backgroundBitmap!=null) {
            Rect rect = new Rect();
            rect.set(0,0,getWidth(),getHeight());
            canvas.drawBitmap(backgroundBitmap, null, rect, basePaint);
        } else {
            basePaint.setColor(Color.WHITE);
            canvas.drawRect(0,0,getWidth(),getHeight(),basePaint);
        }

        synchronized (sprites) {
            for (Sprite s : sprites) {
                if (s.getX() + s.getWidth() >= 0 && s.getX() <= getWidth() && s.getY() + s.getHeight() >= 0 && s.getY() <= getHeight()) {
                    s.doDraw(canvas);
                }
            }
        }
    }

    @Override
    public void doMove(float oldX, float oldY, float newX, float newY) {
        for(Sprite s : selectedSprites) {
            s.onMove(oldX, oldY, newX, newY);
        }
    }

    @Override
    protected void touch_start(float x, float y) {
        super.touch_start(x, y);

        for(int i=sprites.size()-1; i>=0; i--) {
            Sprite s = sprites.get(i);
            if (s.inSprite(x, y)) {
                selectedSprites.add(s);
                s.onSelect(x, y);
                if (!multiSelect) break;
            }
        }
    }

    @Override
    protected void touch_up(float x, float y) {
        super.touch_up(x, y);

        for(Sprite s : selectedSprites) {
            s.onRelease(x, y);
            if (s instanceof AnimatedBitmapSprite) {
                spriteCache.put(spriteCacheCount++, (AnimatedBitmapSprite)s);
            }
        }
        selectedSprites.clear();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        synchronized (sprites) {
            if (sprites != null) {
                for (Sprite s : sprites) {
                    s.onDestroy();
                }
            }
            sprites.clear();
        }
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
