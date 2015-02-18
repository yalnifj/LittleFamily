package org.finlayfamily.littlefamily.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.finlayfamily.littlefamily.data.LittlePerson;
import org.finlayfamily.littlefamily.data.Relationship;
import org.finlayfamily.littlefamily.data.RelationshipType;
import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "LittleFamily.db";

	private static final String TABLE_LITTLE_PERSON = "littleperson";
	private static final String TABLE_RELATIONSHIP = "relationship";
	private static final String TABLE_MEDIA = "media";
	private static final String TABLE_TAGS = "tags";
	
	private static final String COL_ID = "id";
	private static final String COL_NAME = "name";
	private static final String COL_GIVEN_NAME = "givenName";
	private static final String COL_FAMILY_SEARCH_ID = "familySearchId";
	private static final String COL_PHOTO_PATH = "photopath";
	private static final String COL_BIRTH_DATE = "birthDate";
	private static final String COL_AGE = "age";
	private static final String COL_GENDER = "gender";
	private static final String COL_ID1 = "id1";
	private static final String COL_ID2 = "id2";
	private static final String COL_TYPE = "type";
	private static final String COL_LOCAL_PATH = "localpath";
    private static final String COL_MEDIA_ID = "media_id";
    private static final String COL_LEFT = "left";
    private static final String COL_TOP = "top";
    private static final String COL_RIGHT = "right";
    private static final String COL_BOTTOM = "bottom";
    private static final String COL_PERSON_ID = "person_id";
    private static final String COL_LAST_SYNC = "last_sync";

	
	private static final String CREATE_LITTLE_PERSON = "create table "+TABLE_LITTLE_PERSON+" ( " +
			" "+COL_ID+" integer primary key, "+COL_GIVEN_NAME+" text, "+COL_NAME+" text, " +
			" "+COL_FAMILY_SEARCH_ID+" text, "+COL_PHOTO_PATH+" text, "+COL_BIRTH_DATE+" integer, "
			+COL_AGE+" integer, "+COL_GENDER+" char, "+COL_LAST_SYNC+" INTEGER );";

	private static final String CREATE_RELATIONSHIP = "create table " + TABLE_RELATIONSHIP + " ( "
            +COL_ID +" integer primary key, "
			+COL_ID1+" integer, "+COL_ID2+" integer, "+COL_TYPE+" integer, "
            +"foreign key("+COL_ID1+") references "+TABLE_LITTLE_PERSON+"("+COL_ID+"), "
            +"foreign key("+COL_ID2+") references "+TABLE_LITTLE_PERSON+"("+COL_ID+"));";

	private static final String CREATE_MEDIA = "create table "+TABLE_MEDIA+" ( "
			+COL_ID+" integer primary key, "+COL_FAMILY_SEARCH_ID+" text, "
			+COL_TYPE+" integer, "+COL_LOCAL_PATH+" text );";

    private static final String CREATE_TAGS = "create table " + TABLE_TAGS + " ( "
            +COL_ID+" integer primary key, "+COL_MEDIA_ID+" integer, "+COL_PERSON_ID+" integer, "+COL_LEFT+" integer, "
            +COL_TOP+" integer, "+COL_RIGHT+" integer, "+COL_BOTTOM+" integer, "
            +"foreign key("+COL_MEDIA_ID+") references "+TABLE_MEDIA+" ("+COL_ID+"), "
            +"foreign key("+COL_PERSON_ID+") references "+TABLE_LITTLE_PERSON+" ("+COL_ID+"));";
			
	private Context context;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}
			
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_LITTLE_PERSON);
		db.execSQL(CREATE_RELATIONSHIP);
		db.execSQL(CREATE_MEDIA);
        db.execSQL(CREATE_TAGS);
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
        values.put(COL_LAST_SYNC, person.getLastSync().getTime());
		
		// -- add
		if (person.getId() == 0) {
            LittlePerson existing = getPersonByFamilySearchId(person.getFamilySearchId());
            if (existing!=null) {
                person.setId(existing.getId());
            } else {
                long rowid = db.insert(TABLE_LITTLE_PERSON, null, values);
                person.setId((int) rowid);
                Log.d("DBHelper", "persistLittlePerson added person with id " + rowid);
            }
		}
		// --update
		else {
			String selection = COL_ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(person.getId()) };

			int count = db.update(TABLE_LITTLE_PERSON, values, selection, selectionArgs);
			Log.d("DBHelper", "persistLittlePerson updated " + count + " rows");
		}
		db.close();
	}
	
	public LittlePerson getPersonById(int id) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_NAME, COL_AGE, COL_BIRTH_DATE, COL_FAMILY_SEARCH_ID, 
			COL_ID, COL_LAST_SYNC
		};
		String selection = COL_ID + " LIKE ?";
		String[] selectionArgs = { String.valueOf(id) };
		String tables = TABLE_LITTLE_PERSON;
		
		LittlePerson person = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
		while (c.moveToNext()) {
			person = personFromCursor(c);
		}
		
		c.close();
		db.close();
		
		return person;
	}

    public LittlePerson getFirstPerson() {
        SQLiteDatabase db = getReadableDatabase();
        LittlePerson person = null;

        Cursor c = db.rawQuery("select * from "+TABLE_LITTLE_PERSON+" order by "+COL_ID+" LIMIT 1", null);
        while (c.moveToNext()) {
            person = personFromCursor(c);
        }

        c.close();
        db.close();

        return person;
    }

    public void deletePersonById(int id) {
        if (id>0) {
            SQLiteDatabase db = getWritableDatabase();
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(id) };
            int count = db.delete(TABLE_LITTLE_PERSON, selection, selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_LITTLE_PERSON);
            db.close();
        }
    }
	
	public LittlePerson getPersonByFamilySearchId(String fsid) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_NAME, COL_AGE, COL_BIRTH_DATE, COL_FAMILY_SEARCH_ID, 
			COL_ID
		};
		String selection = COL_FAMILY_SEARCH_ID + " LIKE ?";
		String[] selectionArgs = { fsid };
		String tables = TABLE_LITTLE_PERSON;

		LittlePerson person = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_FAMILY_SEARCH_ID);
		while (c.moveToNext()) {
			person = personFromCursor(c);
		}

		c.close();
		db.close();

		return person;
	}
	
	public List<LittlePerson> getRelativesForPerson(int id) {
		List<LittlePerson> people = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_NAME, COL_AGE, COL_BIRTH_DATE, COL_FAMILY_SEARCH_ID, 
			COL_ID
		};
		String selection = "r."+COL_ID1 + " LIKE ? or r."+COL_ID2+" LIKE ?";

		String[] selectionArgs = { String.valueOf(id), String.valueOf(id) };
		String tables = TABLE_LITTLE_PERSON + " p join " + TABLE_RELATIONSHIP + " r on r."+COL_ID1+"=p."+COL_ID
					+" or r."+COL_ID2+"=p."+COL_ID;

		Map<Integer, LittlePerson> personMap = new HashMap<>();
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
		while (c.moveToNext()) {
			LittlePerson p = personFromCursor(c);
			personMap.put(p.getId(), p);
		}

		c.close();
		db.close();
		people.addAll(personMap.values());

		return people;
	}
	
	public LittlePerson personFromCursor(Cursor c) {
		LittlePerson person = new LittlePerson();
		person.setAge(c.getInt( c.getColumnIndexOrThrow(COL_AGE) ));
		if (!c.isNull(c.getColumnIndexOrThrow(COL_BIRTH_DATE))) {
			long birthtime = c.getLong(c.getColumnIndexOrThrow(COL_BIRTH_DATE));
			person.setBirthDate(new Date(birthtime));
		}
		person.setFamilySearchId(c.getString(c.getColumnIndexOrThrow(COL_FAMILY_SEARCH_ID)));
		if (!c.isNull(c.getColumnIndexOrThrow(COL_GENDER))) {
			String gender = c.getString(c.getColumnIndexOrThrow(COL_GENDER));
			switch(gender) {
				case "M":
					person.setGender(GenderType.Male);
					break;
				case "F":
					person.setGender(GenderType.Female);
					break;
				default:
					person.setGender(GenderType.Unknown);
					break;
			}
		}
		person.setGivenName(c.getString(c.getColumnIndexOrThrow(COL_GIVEN_NAME)));
		person.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
		person.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
		person.setPhotoPath(c.getString(c.getColumnIndexOrThrow(COL_PHOTO_PATH)));
        if (!c.isNull(c.getColumnIndexOrThrow(COL_LAST_SYNC))) {
            long synctime = c.getLong(c.getColumnIndexOrThrow(COL_LAST_SYNC));
            person.setLastSync(new Date(synctime));
        }
		return person;
	}
	
	public long persistRelationship(Relationship r) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_ID1, r.getId1());
		values.put(COL_ID2, r.getId2());
		values.put(COL_TYPE, r.getType().getId());

        long rowid = 0;
        if (r.getId()==0) {
            // dont allow duplicate relationships
            if (this.getRelationship(r.getId1(), r.getId2(), r.getType()) != null)
                return -1;
            // -- add
            rowid = db.insert(TABLE_RELATIONSHIP, null, values);
            Log.d("DBHelper", "persistRelationship added relationship id " + rowid);
        } else {
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(r.getId()) };

            int count = db.update(TABLE_RELATIONSHIP, values, selection, selectionArgs);
            Log.d("DBHelper", "persistRelationship updated " + count + " rows");
            rowid = r.getId();
        }
			
		db.close();
		
		return rowid;
	}
	
	public Relationship getRelationship(int id1, int id2, RelationshipType type) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_ID1, COL_ID2, COL_TYPE
		};
		String selection = "(("+COL_ID1 + " LIKE ? and "+COL_ID2+" LIKE ?) "
				+" OR ("+COL_ID1 + " LIKE ? and "+COL_ID2+" LIKE ?)) "
				+" AND "+COL_TYPE+" LIKE ?";
		String[] selectionArgs = { String.valueOf(id1), String.valueOf(id2), 
				String.valueOf(id2), String.valueOf(id1), String.valueOf(type.getId()) };
		String tables = TABLE_RELATIONSHIP;

		Relationship r = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
		while (c.moveToNext()) {
			r = relationshipFromCursor(c);
		}

		c.close();
		db.close();

		return r;
	}
	
	public List<Relationship> getRelationshipsForPerson(int id) {
		List<Relationship> relationships = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_ID1, COL_ID2, COL_TYPE
		};
		String selection = COL_ID1 + " LIKE ? or "+COL_ID2+" LIKE ?";
			
		String[] selectionArgs = { String.valueOf(id), String.valueOf(id) };
		String tables = TABLE_RELATIONSHIP;

		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
		while (c.moveToNext()) {
			Relationship r = relationshipFromCursor(c);
			relationships.add(r);
		}

		c.close();
		db.close();
		
		return relationships;
	}
	
	public Relationship relationshipFromCursor(Cursor c) {
		Relationship r = new Relationship();
		r.setId1(c.getInt(c.getColumnIndexOrThrow(COL_ID1)));
		r.setId2(c.getInt(c.getColumnIndexOrThrow(COL_ID2)));
		int type = c.getInt(c.getColumnIndexOrThrow(COL_TYPE));
		RelationshipType rt = RelationshipType.getTypeFromId(type);
		r.setType(rt);
		return r;
	}
}
