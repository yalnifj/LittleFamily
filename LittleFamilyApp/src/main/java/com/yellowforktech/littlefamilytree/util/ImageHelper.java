package com.yellowforktech.littlefamilytree.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.MediaStore;
import android.util.Log;

import com.yellowforktech.littlefamilytree.R;
import com.yellowforktech.littlefamilytree.data.LittlePerson;

import org.gedcomx.types.GenderType;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageHelper {

	/**
	 * Load a contact photo thumbnail and return it as a Bitmap, resizing the
	 * image to the provided image dimensions as needed.
	 * 
	 * @param photoData
	 *            photo ID Prior to Honeycomb, the contact's _ID value. For
	 *            Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
	 * @return A thumbnail Bitmap, sized to the provided width and height.
	 *         Returns null if the thumbnail is not found.
	 */
	public static Bitmap loadContactPhotoThumbnail(Context context, String photoData) {
		// Creates an asset file descriptor for the thumbnail file.
		AssetFileDescriptor afd = null;
		// try-catch block for file not found
		try {
			// Creates a holder for the URI.
			Uri thumbUri;
			// If Android 3.0 or later
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// Sets the URI from the incoming PHOTO_THUMBNAIL_URI
				thumbUri = Uri.parse(photoData);
			} else {
				// Prior to Android 3.0, constructs a photo Uri using _ID
				/*
				 * Creates a contact URI from the Contacts content URI incoming
				 * photoData (_ID)
				 */
				final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);
				/*
				 * Creates a photo URI by appending the content URI of
				 * Contacts.Photo.
				 */
				thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
			}

			/*
			 * Retrieves an AssetFileDescriptor object for the thumbnail URI
			 * using ContentResolver.openAssetFileDescriptor
			 */
			afd = context.getContentResolver().openAssetFileDescriptor(thumbUri, "r");
			/*
			 * Gets a file descriptor from the asset file descriptor. This
			 * object can be used across processes.
			 */
			FileDescriptor fileDescriptor = afd.getFileDescriptor();
			// Decode the photo file and return the result as a Bitmap
			// If the file descriptor is valid
			if (fileDescriptor != null) {
				// Decodes the bitmap
				return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
			}
			// If the file isn't found
		} catch (FileNotFoundException e) {
			Log.e("ImageHelper", "Error loading contact image", e);
		}
		// In all cases, close the asset file descriptor
		finally {
			if (afd != null) {
				try {
					afd.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public static Bitmap loadBitmapFromFile(String path, int orientation, final int targetWidth, final int targetHeight, boolean forceSize) {
	    Bitmap bitmap = null;
	    try {
	        // First decode with inJustDecodeBounds=true to check dimensions
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

	        // Adjust extents
	        int sourceWidth, sourceHeight;
	        if (orientation == 90 || orientation == 270) {
	            sourceWidth = options.outHeight;
	            sourceHeight = options.outWidth;
	        } else {
	            sourceWidth = options.outWidth;
	            sourceHeight = options.outHeight;
	        }

	        // Calculate the maximum required scaling ratio if required and load the bitmap
	        if (forceSize || sourceWidth > targetWidth || sourceHeight > targetHeight) {
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
				try {
	            	bitmap = BitmapFactory.decodeFile(path, options);
				}
				catch (OutOfMemoryError e) {
					System.gc();
					Log.e("ImageHelper", "Out of memory trying to load image "+path, e);
					options.inSampleSize = options.inSampleSize*2;
					bitmap = BitmapFactory.decodeFile(path, options);
				}
	        } else {
	            bitmap = BitmapFactory.decodeFile(path);
	        }

	        // Rotate the bitmap if required
	        if (orientation > 0) {
	            Matrix matrix = new Matrix();
	            matrix.postRotate(orientation);
	            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	        }

            if (bitmap!=null) {
                // Re-scale the bitmap if necessary
                sourceWidth = bitmap.getWidth();
                sourceHeight = bitmap.getHeight();
                if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
                    float widthRatio = (float) sourceWidth / (float) targetWidth;
                    float heightRatio = (float) sourceHeight / (float) targetHeight;
                    float maxRatio = Math.max(widthRatio, heightRatio);
                    sourceWidth = (int) ((float) sourceWidth / maxRatio);
                    sourceHeight = (int) ((float) sourceHeight / maxRatio);
					try {
						bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
					}
					catch (OutOfMemoryError e) {
						System.gc();
						Log.e("ImageHelper", "Out of memory trying to resize image "+path, e);
					}
                }
            }

	    } catch (Exception e) {
	    }
	    return bitmap;
	}

    public static Bitmap loadBitmapFromResource(Context context, int resourceId, int orientation, final int targetWidth, final int targetHeight) {
        Bitmap bitmap = null;
        try {
            int sourceWidth, sourceHeight;
            if (bitmap==null) {
                // First decode with inJustDecodeBounds=true to check dimensions
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeResource(context.getResources(), resourceId, options);

                // Adjust extents
                if (orientation == 90 || orientation == 270) {
                    sourceWidth = options.outHeight;
                    sourceHeight = options.outWidth;
                } else {
                    sourceWidth = options.outWidth;
                    sourceHeight = options.outHeight;
                }

                // Calculate the maximum required scaling ratio if required and load the bitmap
                if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
                    bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
                } else {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
                }
            }

            // Rotate the bitmap if required
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            if (bitmap!=null) {
                // Re-scale the bitmap if necessary
                sourceWidth = bitmap.getWidth();
                sourceHeight = bitmap.getHeight();
                if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
                    float widthRatio = (float) sourceWidth / (float) targetWidth;
                    float heightRatio = (float) sourceHeight / (float) targetHeight;
                    float maxRatio = Math.max(widthRatio, heightRatio);
                    sourceWidth = (int) ((float) sourceWidth / maxRatio);
                    sourceHeight = (int) ((float) sourceHeight / maxRatio);
                    try {
						bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
					}
					catch (OutOfMemoryError e) {
						System.gc();
						Log.e("ImageHelper", "Out of memory trying to resize image "+resourceId, e);
					}
                }
            }
        } catch (Exception e) {
        }
        return bitmap;
    }

    public static Bitmap loadBitmapFromStream(InputStream is, int orientation, final int targetWidth, final int targetHeight) {
        Bitmap bitmap = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            // Adjust extents
            int sourceWidth, sourceHeight;
            if (orientation == 90 || orientation == 270) {
                sourceWidth = options.outHeight;
                sourceHeight = options.outWidth;
            } else {
                sourceWidth = options.outWidth;
                sourceHeight = options.outHeight;
            }

            // Calculate the maximum required scaling ratio if required and load the bitmap
            if (sourceWidth > targetWidth || sourceHeight > targetHeight) {
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
                is.reset();
                bitmap = BitmapFactory.decodeStream(is, null, options);
            } else {
                is.reset();
                bitmap = BitmapFactory.decodeStream(is);
            }

            // Rotate the bitmap if required
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(orientation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            if (bitmap!=null) {
                // Re-scale the bitmap if necessary
                sourceWidth = bitmap.getWidth();
                sourceHeight = bitmap.getHeight();
                if (sourceWidth != targetWidth || sourceHeight != targetHeight) {
                    float widthRatio = (float) sourceWidth / (float) targetWidth;
                    float heightRatio = (float) sourceHeight / (float) targetHeight;
                    float maxRatio = Math.max(widthRatio, heightRatio);
                    sourceWidth = (int) ((float) sourceWidth / maxRatio);
                    sourceHeight = (int) ((float) sourceHeight / maxRatio);
                    try {
						bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
					}
					catch (OutOfMemoryError e) {
						System.gc();
						Log.e("ImageHelper", "Out of memory trying to resize image ", e);
					}
                }
            }
        } catch (Exception e) {
        }
        return bitmap;
    }
	
	public static int getOrientation(Context context, Uri photoUri) {
	    /* it's on the external media. */
	    Cursor cursor = context.getContentResolver().query(photoUri,
	            new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

	    if (cursor.getCount() != 1) {
	        return -1;
	    }

	    cursor.moveToFirst();
	    return cursor.getInt(0);
	}
	
	public static int getOrientation(String filename) {
		if (filename==null) return 0;
		try {
			ExifInterface exif = new ExifInterface(filename);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
			switch(orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			default:
				return 0;
			}
		} catch(Exception e) {
		}
		return 0;
	}

    public static File getDataFolder(Context context) {
        File dataDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dataDir = new File(Environment.getExternalStorageDirectory(), ".LittleFamilyTreeData");

            File oldDataDir = new File(Environment.getExternalStorageDirectory(), "LittleFamilyData");
            if (oldDataDir!=null && oldDataDir.isDirectory()) {
                oldDataDir.renameTo(dataDir);
            }

            if(!dataDir.isDirectory()) {
                //-- for new installs, don't use .LittleFamilyDataDir
                //-- for upgrades, continue to use it
                dataDir = null;
                //dataDir.mkdirs();
            }
        }

        if(dataDir==null || !dataDir.isDirectory()) {
            dataDir = context.getFilesDir();
        }

        return dataDir;
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, int maxWidth, int maxHeight, Paint paint)
    {
        try
        {
            Bitmap bmOverlay = Bitmap.createBitmap(maxWidth, maxHeight,  bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);

            Rect rect = new Rect();
            int innerWidth = (int) (maxWidth*0.70);
            int innerHeight = (int) (maxHeight*0.70);

            int left = (maxWidth - innerWidth)/2;
            int top = (maxHeight - innerHeight)/2;
            int right = left + innerWidth;
            int bottom = top + innerHeight;

            double ratio = ((double)bmp1.getWidth())/((double)bmp1.getHeight());
            if (ratio > 1) {
				int diff = (int)(innerHeight - innerHeight/ratio)/2;
                bottom = bottom - diff;
				top = top + diff;
            }
            if (ratio < 1) {
				int diff = (int)(innerHeight - innerHeight*ratio)/2;
                right = right - diff;
				left = left + diff;
            }
			Log.i("ImageHelper", "ratio " + ratio + " w=" + innerWidth + " h=" + innerHeight);
            rect.set(left, top, right, bottom);
            Rect rect2 = new Rect();
            int w = maxWidth;
            int h = maxHeight;

            double ratio2 = ((double) bmp2.getWidth()) / ((double) bmp2.getHeight());
            int t = 0;
            int l = 0;
            if (ratio2 > 1) {
                h = (int) (h / ratio2);
                t = (maxHeight - h) / 2;
            }
            if (ratio2 < 1) {
                w = (int) (w * ratio2);
                l = (maxWidth - w) / 2;
            }
            Log.i("ImageHelper", "ratio2 " + ratio2 + " w=" + w + " h=" + h);
            rect2.set(l,t,l+w,t+h);

            if (paint!=null) {
                Bitmap background = fill(bmp2, paint);
                canvas.drawBitmap(background, null, rect2, paint);
            }
            canvas.drawBitmap(bmp1, null, rect, paint);
            canvas.drawBitmap(bmp2, null, rect2, null);

            return bmOverlay;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap fill(Bitmap bmp1, Paint paint)
    {
        try
        {
            Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(),  bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);

            int midX = bmp1.getWidth() / 2;
            int midY = bmp1.getHeight() / 2;

            int x = midX;
            int y = midY;

            boolean xCont = true;
            while(x > 0 && xCont) {
                int pix = bmp1.getPixel(x, midY);
                int alpha = Color.alpha(pix);
                int pix2 = bmp1.getPixel(bmp1.getWidth() - x, midY);
                int alpha2 = Color.alpha(pix2);
                if (alpha > 200 && alpha2 > 200) {
                    xCont = false;
                }
                x--;
            }
            boolean yCont = true;
            while(y > 0 && yCont) {
                int pix = bmp1.getPixel(midX, y);
                int alpha = Color.alpha(pix);
                int pix2 = bmp1.getPixel(midX, bmp1.getWidth() - y);
                int alpha2 = Color.alpha(pix2);
                if (alpha > 200 && alpha2 > 200) {
                    yCont = false;
                }
                y--;
            }

            canvas.drawRect(x, y, bmp1.getWidth() - x, bmp1.getHeight() - y, paint);

            return bmOverlay;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static List<String> wrapText(String text, float detailWidth, Paint textPaint) {
        List<String> texts = new ArrayList<>(1);
        if (textPaint.measureText(text) > detailWidth*1.5) {
            String[] words = text.split("\\s");
            float size = 0;
            String line = "";
            for (String word : words) {
                size += textPaint.measureText(line + word + " ");
                if (size >= detailWidth*1.5) {
                    texts.add(line);
                    line = word+" ";
                    size = (int)textPaint.measureText(line);
                } else {
                    line += word+" ";
                }
            }
            if (!line.isEmpty()) {
                texts.add(line);
            }
        } else {
            texts.add(text);
        }
        return texts;
    }

    public static int getPersonDefaultImage(Context context, LittlePerson person) {
        String skinColor = PreferenceManager.getDefaultSharedPreferences(context).getString("skin_color", "light");
        return getPersonCartoon(person, skinColor);
    }

    public static int getPersonCartoon(LittlePerson person, String skinColor) {
        if (person.getAge()!=null) {
            if (person.getAge() < 2) {
                switch(skinColor) {
                    case "mid":
                        return R.drawable.baby_mid;
                    case "dark":
                        return R.drawable.baby_dark;
                    default:
                        return R.drawable.baby;
                }
            }
            if (person.getAge() < 18) {
                if (person.getGender()== GenderType.Female) {
                    switch(skinColor) {
                        case "mid":
                            return com.yellowforktech.littlefamilytree.R.drawable.girl_mid;
                        case "dark":
                            return com.yellowforktech.littlefamilytree.R.drawable.girl_dark;
                        default:
                            return com.yellowforktech.littlefamilytree.R.drawable.girl;
                    }
                } else {
                    switch(skinColor) {
                        case "mid":
                            return com.yellowforktech.littlefamilytree.R.drawable.boy_mid;
                        case "dark":
                            return com.yellowforktech.littlefamilytree.R.drawable.boy_dark;
                        default:
                            return com.yellowforktech.littlefamilytree.R.drawable.boy;
                    }
                }
            }
            if (person.getAge() < 50) {
                if (person.getGender()==GenderType.Female) {
                    switch(skinColor) {
                        case "mid":
                            return com.yellowforktech.littlefamilytree.R.drawable.mom_mid;
                        case "dark":
                            return com.yellowforktech.littlefamilytree.R.drawable.mom_dark;
                        default:
                            return com.yellowforktech.littlefamilytree.R.drawable.mom;
                    }
                } else {
                    switch(skinColor) {
                        case "mid":
                            return com.yellowforktech.littlefamilytree.R.drawable.dad_mid;
                        case "dark":
                            return com.yellowforktech.littlefamilytree.R.drawable.dad_dark;
                        default:
                            return com.yellowforktech.littlefamilytree.R.drawable.dad;
                    }
                }
            }
            if (person.getGender()==GenderType.Female) {
                switch(skinColor) {
                    case "mid":
                        return com.yellowforktech.littlefamilytree.R.drawable.grandma_mid;
                    case "dark":
                        return com.yellowforktech.littlefamilytree.R.drawable.grandma_dark;
                    default:
                        return com.yellowforktech.littlefamilytree.R.drawable.grandma;
                }
            } else {
                switch(skinColor) {
                    case "mid":
                        return com.yellowforktech.littlefamilytree.R.drawable.grandpa_mid;
                    case "dark":
                        return com.yellowforktech.littlefamilytree.R.drawable.grandpa_dark;
                    default:
                        return com.yellowforktech.littlefamilytree.R.drawable.grandpa;
                }
            }
        } else {
            if (person.getGender()==GenderType.Female) {
                switch(skinColor) {
                    case "mid":
                        return com.yellowforktech.littlefamilytree.R.drawable.mom_mid;
                    case "dark":
                        return com.yellowforktech.littlefamilytree.R.drawable.mom_dark;
                    default:
                        return com.yellowforktech.littlefamilytree.R.drawable.mom;
                }
            } else {
                switch(skinColor) {
                    case "mid":
                        return com.yellowforktech.littlefamilytree.R.drawable.dad_mid;
                    case "dark":
                        return com.yellowforktech.littlefamilytree.R.drawable.dad_dark;
                    default:
                        return com.yellowforktech.littlefamilytree.R.drawable.dad;
                }
            }
        }
    }
}
