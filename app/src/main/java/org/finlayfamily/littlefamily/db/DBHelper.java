package org.finlayfamily.littlefamily.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.finlayfamily.littlefamily.data.*;
import android.content.*;
import android.util.*;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "LittleFamily.db";

	private static final String TABLE_LITTLE_PERSON = "littleperson";
	
	private static final String COL_ID = "id";
	private static final String COL_NAME = "name";
	private static final String COL_GIVEN_NAME = "givenName";
	private static final String COL_FAMILY_SEARCH_ID = "familySearchId";
	private static final String COL_PHOTO_PATH = "photopath";
	private static final String COL_BIRTH_DATE = "birthDate";
	private static final String COL_AGE = "age";
	private static final String COL_GENDER = "gender";
	
	private static final String CREATE_LITTLE_PERSON = "create table "+TABLE_LITTLE_PERSON+" ( " +
			" "+COL_ID+" integer primary key, "+COL_GIVEN_NAME+" text, "+COL_NAME+" text, " +
			" "+COL_FAMILY_SEARCH_ID+" text, "+COL_PHOTO_PATH+" text, "+COL_BIRTH_DATE+" integer, "
			+COL_AGE+" integer, "+COL_GENDER+" char );";
	
	private Context context;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
			
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_LITTLE_PERSON);
		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public void persistLittlePerson(LittlePerson person) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_NAME, person.getName());
		values.put(COL_GIVEN_NAME, person.getGivenName());
		values.put(COL_GENDER, person.getGender().toString());
		values.put(COL_PHOTO_PATH, person.getPhotoPath());
		values.put(COL_AGE, person.getAge());
		values.put(COL_BIRTH_DATE, person.getBirthDate().getTime());
		values.put(COL_FAMILY_SEARCH_ID, person.getFamilySearchId());
		
		// -- add
		if (person.getId() == 0) {
			long rowid = db.insert(TABLE_LITTLE_PERSON, null, values);
			person.setId((int) rowid);
		}
		// --update
		else {
			String selection = COL_ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(person.getId()) };

			int count = db.update(TABLE_LITTLE_PERSON, values, selection, selectionArgs);
			Log.d("DBHelper", "updated " + count + " rows");
		}
		db.close();
	}
}
