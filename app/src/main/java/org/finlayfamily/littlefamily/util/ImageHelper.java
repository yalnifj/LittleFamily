package org.finlayfamily.littlefamily.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

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
			Log.e("FamilyMembers", "Error loading contact image", e);
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
	            float widthRatio = (float)sourceWidth / (float)targetWidth;
	            float heightRatio = (float)sourceHeight / (float)targetHeight;
	            float maxRatio = Math.max(widthRatio, heightRatio);
	            options.inJustDecodeBounds = false;
	            options.inSampleSize = (int)maxRatio;
                options.outHeight = targetHeight;
                options.outWidth = targetWidth;
	            bitmap = BitmapFactory.decodeFile(path, options);
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
                    bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
                }
            }
	    } catch (Exception e) {
	    }
	    return bitmap;
	}

    public static Bitmap loadBitmapFromResource(Context context, int resourceId, int orientation, final int targetWidth, final int targetHeight) {
        Bitmap bitmap = null;
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

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
                float widthRatio = (float)sourceWidth / (float)targetWidth;
                float heightRatio = (float)sourceHeight / (float)targetHeight;
                float maxRatio = Math.max(widthRatio, heightRatio);
                options.inJustDecodeBounds = false;
                options.inSampleSize = (int)maxRatio;
                bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            } else {
                bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
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
                    bitmap = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
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
            dataDir = new File(Environment.getExternalStorageDirectory(), "LittleFamilyData");
            if(!dataDir.isDirectory()) {
                dataDir.mkdirs();
            }
        }

        if(!dataDir.isDirectory()) {
            dataDir = context.getFilesDir();
        }

        return dataDir;
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2, int maxWidth, int maxHeight)
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
			Log.i( "ImageHelper", "ratio " + ratio +" w="+innerWidth+" h="+innerHeight);
            rect.set(left, top, right, bottom);
            canvas.drawBitmap(bmp1, null, rect, null);

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
			Log.i( "ImageHelper", "ratio2 " + ratio2 + " w="+w+" h="+h);
            rect2.set(l,t,l+w,t+h);
            canvas.drawBitmap(bmp2, null, rect2, null);

            return bmOverlay;
        } catch (Exception e)
        {
            // TODO: handle exception
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
}
