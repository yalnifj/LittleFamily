package com.yellowforktech.littlefamilytree.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.yellowforktech.littlefamilytree.activities.LittleFamilyActivity;
import com.yellowforktech.littlefamilytree.data.DataService;
import com.yellowforktech.littlefamilytree.data.LittlePerson;
import com.yellowforktech.littlefamilytree.data.LocalResource;
import com.yellowforktech.littlefamilytree.data.Media;
import com.yellowforktech.littlefamilytree.data.Relationship;
import com.yellowforktech.littlefamilytree.data.RelationshipType;
import com.yellowforktech.littlefamilytree.data.Tag;

import org.gedcomx.types.GenderType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 6;
	public static final String UUID_PROPERTY = "UUID";
	private static final String DATABASE_NAME = "LittleFamily.db";

	private static final String TABLE_LITTLE_PERSON = "littleperson";
	private static final String TABLE_RELATIONSHIP = "relationship";
	private static final String TABLE_MEDIA = "media";
	private static final String TABLE_TAGS = "tags";
    private static final String TABLE_PROPERTIES = "properties";
	private static final String TABLE_SYNCQ = "syncq";
	private static final String TABLE_LOCAL_RESOURCES = "localresources";
	
	private static final String COL_ID = "id";
	public static final String COL_NAME = "name";
	public static final String COL_GIVEN_NAME = "givenName";
	private static final String COL_FAMILY_SEARCH_ID = "familySearchId";
	private static final String COL_PHOTO_PATH = "photopath";
	private static final String COL_BIRTH_DATE = "birthDate";
	private static final String COL_AGE = "age";
	private static final String COL_GENDER = "gender";
    private static final String COL_ALIVE = "alive";
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
    private static final String COL_ACTIVE = "active";
    private static final String COL_BIRTH_PLACE = "birthPlace";
    private static final String COL_NATIONALITY = "nationality";
    private static final String COL_HAS_PARENTS = "hasParents";
	private static final String COL_HAS_CHILDREN = "hasChildren";
	private static final String COL_HAS_SPOUSES = "hasSpouses";
	private static final String COL_HAS_MEDIA = "hasMedia";
	private static final String COL_TREE_LEVEL = "treeLevel";
	private static final String COL_OCCUPATION = "occupation";

	public static final String TYPE_GIVEN_AUDIO = "givenAudio";
	
	private static final String CREATE_LITTLE_PERSON = "create table "+TABLE_LITTLE_PERSON+" ( " +
			" "+COL_ID+" integer primary key, "+COL_GIVEN_NAME+" text, "+COL_NAME+" text, " +
			" "+COL_FAMILY_SEARCH_ID+" text, "+COL_PHOTO_PATH+" text, "+COL_BIRTH_DATE+" integer, " + COL_BIRTH_PLACE+" text, "+
			" "+COL_NATIONALITY+" text, "+COL_AGE+" integer, "+COL_GENDER+" char, "+COL_ALIVE+" char, "+
            " "+COL_HAS_PARENTS+" char, "+COL_HAS_CHILDREN+" char, "+COL_HAS_SPOUSES+" char, "+COL_HAS_MEDIA+" char, "+
			" "+COL_ACTIVE+" char, "+COL_LAST_SYNC+" INTEGER, "+COL_TREE_LEVEL+" INTEGER, "+COL_OCCUPATION+" text );";

	private static final String CREATE_RELATIONSHIP = "create table " + TABLE_RELATIONSHIP + " ( "
            +COL_ID +" integer primary key, "
			+COL_ID1+" integer, "+COL_ID2+" integer, "+COL_TYPE+" integer, "
            +"foreign key("+COL_ID1+") references "+TABLE_LITTLE_PERSON+"("+COL_ID+"), "
            +"foreign key("+COL_ID2+") references "+TABLE_LITTLE_PERSON+"("+COL_ID+"));";

	private static final String CREATE_MEDIA = "create table "+TABLE_MEDIA+" ( "
			+COL_ID+" integer primary key, "+COL_FAMILY_SEARCH_ID+" text, "
			+COL_TYPE+" text, "+COL_LOCAL_PATH+" text );";

    private static final String CREATE_TAGS = "create table " + TABLE_TAGS + " ( "
            +COL_ID+" integer primary key, "+COL_MEDIA_ID+" integer, "+COL_PERSON_ID+" integer, "+COL_LEFT+" real, "
            +COL_TOP+" real, "+COL_RIGHT+" real, "+COL_BOTTOM+" real, "
            +"foreign key("+COL_MEDIA_ID+") references "+TABLE_MEDIA+" ("+COL_ID+"), "
            +"foreign key("+COL_PERSON_ID+") references "+TABLE_LITTLE_PERSON+" ("+COL_ID+"));";

    private static final String CREATE_PROPERTIES = "create table "+ TABLE_PROPERTIES + " ("
            + " property text primary key, value text, "+COL_LAST_SYNC+" integer"
            + " )";

	private static final String CREATE_SYNCQ = "create table "+TABLE_SYNCQ + " ( "
			+ COL_ID + " integer )";

	private static final String CREATE_LOCAL_RESOURCES = "create table "+TABLE_LOCAL_RESOURCES+" ( " +
			COL_ID +" integer primary key, " +
			COL_PERSON_ID+" integer, "+
			COL_TYPE+" text, "+
			COL_LOCAL_PATH+" text, " +
			" foreign key("+COL_PERSON_ID+") references "+TABLE_LITTLE_PERSON+" ("+COL_ID+")"+
			" )";
			
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
        db.execSQL(CREATE_PROPERTIES);
		db.execSQL(CREATE_SYNCQ);
		db.execSQL(CREATE_LOCAL_RESOURCES);

		//-- save a random installation ID
		//saveProperty(UUID_PROPERTY, UUID.randomUUID().toString());
		
        ContentValues values = new ContentValues();
        values.put("property", UUID_PROPERTY);
        values.put("value", UUID.randomUUID().toString());
        values.put(COL_LAST_SYNC, (new Date()).getTime());

        long rowid = db.insert(TABLE_PROPERTIES, null, values);
        Log.d("DBHelper", "saveProperty rowid " + rowid);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion!=newVersion) {
			if (oldVersion < 2) {
				String sql = "alter table " + TABLE_LITTLE_PERSON + " add column " + COL_HAS_MEDIA + " char default ''";
				db.execSQL(sql);
			}
			if (oldVersion < 3) {
				String sql = "alter table " + TABLE_LITTLE_PERSON + " add column " + COL_TREE_LEVEL + " INTEGER default NULL";
				db.execSQL(sql);

				String sqlu = "update " + TABLE_LITTLE_PERSON + " set " + COL_TREE_LEVEL + "=0 where "+COL_ID+"=0";
				db.execSQL(sqlu);
			}
			if (oldVersion < 4) {
				String sql = "alter table " + TABLE_LITTLE_PERSON + " add column " + COL_OCCUPATION + " text default NULL";
				db.execSQL(sql);
			}
			if (oldVersion < 5) {
				db.execSQL(CREATE_LOCAL_RESOURCES);
			}
			if (oldVersion < 6) {
				saveProperty(LittleFamilyActivity.PROP_HAS_PREMIUM, "true");
				fireCreateOrUpdateUser(true);
			}
		}
	}

    public Long dateToLong(Date date) {
        if (date==null) return null;
        return date.getTime();
    }

    private String getYorNForBoolean(boolean b) {
        if (b) return "Y";
        return "N";
    }

    private String getYorNorNullForBoolean(Boolean b) {
        if (b==null) return "";
        if (b) return "Y";
        return "N";
    }

	public void fireCreateOrUpdateUser(boolean isPremium) {
		try {
			String username = DataService.getInstance().getEncryptedProperty(DataService.SERVICE_USERNAME);
			String serviceType = getProperty(DataService.SERVICE_TYPE);
			FirebaseDatabase database = FirebaseDatabase.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void persistLittlePerson(LittlePerson person) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_NAME, person.getName());
		values.put(COL_GIVEN_NAME, person.getGivenName());
        switch(person.getGender()) {
            case Male:
		        values.put(COL_GENDER, "M");
                break;
            case Female:
                values.put(COL_GENDER, "F");
                break;
            default:
                values.put(COL_GENDER, "U");
                break;
        }
		values.put(COL_PHOTO_PATH, person.getPhotoPath());
		values.put(COL_AGE, person.getAge());
		values.put(COL_BIRTH_DATE, dateToLong(person.getBirthDate()));
		values.put(COL_FAMILY_SEARCH_ID, person.getFamilySearchId());
        values.put(COL_LAST_SYNC, dateToLong(person.getLastSync()));
        values.put(COL_ALIVE, getYorNForBoolean(person.isAlive()));
        values.put(COL_ACTIVE, getYorNForBoolean(person.isActive()));
        values.put(COL_BIRTH_PLACE, person.getBirthPlace());
        values.put(COL_NATIONALITY, person.getNationality());
        values.put(COL_HAS_PARENTS, getYorNorNullForBoolean(person.isHasParents()));
		values.put(COL_HAS_CHILDREN, getYorNorNullForBoolean(person.isHasChildren()));
		values.put(COL_HAS_SPOUSES, getYorNorNullForBoolean(person.isHasSpouses()));
		values.put(COL_HAS_MEDIA, getYorNorNullForBoolean(person.isHasMedia()));
		values.put(COL_TREE_LEVEL, person.getTreeLevel());
		values.put(COL_OCCUPATION, person.getOccupation());
		
		// -- add
		if (person.getId() == 0) {
            LittlePerson existing = getPersonByFamilySearchId(person.getFamilySearchId());
            if (existing!=null) {
                person.setId(existing.getId());
            } else {
                long rowid = db.insert(TABLE_LITTLE_PERSON, null, values);
                person.setId((int) rowid);
                Log.d("DBHelper", "persistLittlePerson added person with id " + rowid+" "+person.getId()+" "+person.getName() + " " + person.getFamilySearchId());
            }
		}
		// --update
		if (person.getId() > 0) {
			String selection = COL_ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(person.getId()) };

			int count = db.update(TABLE_LITTLE_PERSON, values, selection, selectionArgs);
			Log.d("DBHelper", "persistLittlePerson updated " + count + " rows "+person.getId()+" "+person.getName() + " " + person.getFamilySearchId());
		}
	}
	
	public LittlePerson getPersonById(int id) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID,
			"p."+COL_ID, COL_LAST_SYNC, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN,
			COL_HAS_SPOUSES, COL_HAS_MEDIA, COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = "p."+COL_ID + " LIKE ?";
		String[] selectionArgs = { String.valueOf(id) };
		String tables = TABLE_LITTLE_PERSON + " p left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";
		
		LittlePerson person = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "p."+COL_ID);
		while (c.moveToNext()) {
			person = personFromCursor(c);
		}
		
		c.close();

		return person;
	}

    public LittlePerson getFirstPerson() {
        SQLiteDatabase db = getReadableDatabase();
        LittlePerson person = null;

        Cursor c = db.rawQuery("select p.*, l."+COL_LOCAL_PATH+" from " + TABLE_LITTLE_PERSON + " p "+
				"left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' where active='Y' order by p." + COL_ID + " LIMIT 1", null);
        while (c.moveToNext()) {
            person = personFromCursor(c);
        }

        c.close();

        return person;
    }

	public LittlePerson getRandomPersonWithMedia() {
        SQLiteDatabase db = getReadableDatabase();
        LittlePerson person = null;

        Cursor c = db.rawQuery("select p.*, l."+COL_LOCAL_PATH+" from " + TABLE_LITTLE_PERSON + " p join "+TABLE_TAGS+" t on t."+COL_PERSON_ID+"=p."+COL_ID+
				" left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"'" +
				" where p.active='Y' order by RANDOM() LIMIT 1", null);
        while (c.moveToNext()) {
            person = personFromCursor(c);
        }

        c.close();

        return person;
    }
	
	public  List<LittlePerson> getNextBirthdays(int maxNumber, int maxLevel) {
        SQLiteDatabase db = getReadableDatabase();
		
		List<LittlePerson> people = new ArrayList<LittlePerson>();

        Cursor c = db.rawQuery("select a.* from (select p.*, strftime('%s','now') as todaysecs, "+
								" cast(((strftime('%s','now') - (604800 + (p." + COL_BIRTH_DATE + " / 1000))) / 31557600) as int) as yeardiff, "+
								" l."+COL_LOCAL_PATH+" "+
								" from " + TABLE_LITTLE_PERSON + " p "+
								" left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' "+
							   	" where p.active='Y' and p."+COL_BIRTH_DATE+" is not null and p."+COL_TREE_LEVEL+" < "+maxLevel+" ) a " +
								" order by a."+COL_BIRTH_DATE + " + (a.yeardiff * 31557600000) + (86400000 * 5 * a."+COL_TREE_LEVEL+") "+
								" LIMIT "+maxNumber, null);
        while (c.moveToNext()) {
            LittlePerson person = personFromCursor(c);
			Log.d("getNextBirthdays", "Person "+person.getName()+" birthdate="+person.getBirthDate()+" todaysecs="+c.getLong(c.getColumnIndex("todaysecs"))+" birthmillis="+c.getLong(c.getColumnIndex(COL_BIRTH_DATE))+" yeardiff="+c.getLong(c.getColumnIndex("yeardiff")));
			people.add(person);
        }

        c.close();

        return people;
    }
	
    public void deletePersonById(int id) {
        if (id>0) {
            SQLiteDatabase db = getWritableDatabase();
            String[] selectionArgs = { String.valueOf(id) };

            int count = db.delete(TABLE_TAGS, COL_PERSON_ID+" LIKE ?", selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_TAGS);

			count = db.delete(TABLE_LOCAL_RESOURCES, COL_PERSON_ID+" LIKE ?", selectionArgs);
			Log.d("DBHelper", "deleted "+count+" from "+TABLE_LOCAL_RESOURCES);

            String[] selectionArgs2 = { String.valueOf(id), String.valueOf(id) };
            count = db.delete(TABLE_RELATIONSHIP, COL_ID1+" LIKE ? OR "+COL_ID2+ " LIKE ? ", selectionArgs2);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_RELATIONSHIP);

            String selection = COL_ID + " LIKE ?";
            count = db.delete(TABLE_LITTLE_PERSON, selection, selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_LITTLE_PERSON);

        }
    }
	
	public LittlePerson getPersonByFamilySearchId(String fsid) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
			"p."+COL_ID, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES, COL_HAS_MEDIA,
			COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = COL_FAMILY_SEARCH_ID + " LIKE ?";
		String[] selectionArgs = { fsid };
		String tables = TABLE_LITTLE_PERSON + " p left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

		LittlePerson person = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_FAMILY_SEARCH_ID);
		while (c.moveToNext()) {
			person = personFromCursor(c);
		}

		c.close();

		return person;
	}

	public List<LittlePerson> search(Map<String, String> params) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
				COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
				"p."+COL_ID, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES, COL_HAS_MEDIA,
				COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = "";
		String[] selectionArgs = new String[params.size()];
		int count=0;
		for(String key : params.keySet()) {
			if (params.get(key)!=null) {
				if (count > 0) selection += " AND ";
				selection += key + " LIKE ?";
				selectionArgs[count] = params.get(key);
				count++;
			}
		}
		String tables = TABLE_LITTLE_PERSON + " p left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

		List<LittlePerson> people = new ArrayList<>();
		LittlePerson person = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_FAMILY_SEARCH_ID);
		while (c.moveToNext()) {
			person = personFromCursor(c);
			people.add(person);
		}

		c.close();

		return people;
	}

    public List<LittlePerson> getRelativesForPerson(int id) {
		List<LittlePerson> people = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
			COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
			"p."+COL_ID, COL_ALIVE, COL_ACTIVE, "r."+COL_TYPE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES,
			COL_HAS_MEDIA, COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = "(r."+COL_ID1 + " LIKE ? or r."+COL_ID2+" LIKE ?) and p."+COL_ACTIVE+"='Y'";

		String[] selectionArgs = { String.valueOf(id), String.valueOf(id) };
		String tables = TABLE_LITTLE_PERSON + " p join " + TABLE_RELATIONSHIP + " r on r."+COL_ID1+"=p."+COL_ID
					+" or r."+COL_ID2+"=p."+COL_ID
				+ " left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

		Map<Integer, LittlePerson> personMap = new HashMap<>();
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "p."+COL_ID);
		while (c.moveToNext()) {
			LittlePerson p = personFromCursor(c);
			personMap.put(p.getId(), p);
		}

		c.close();
		people.addAll(personMap.values());

		return people;
	}

    public List<LittlePerson> getParentsForPerson(int id) {
        List<LittlePerson> people = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
                COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
                "p."+COL_ID, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES,
				COL_HAS_MEDIA, COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
        };
        String selection = "r."+COL_ID2+" LIKE ? and r."+COL_TYPE+"=? and p."+COL_ACTIVE+"='Y'";

        String[] selectionArgs = { String.valueOf(id), String.valueOf(RelationshipType.PARENTCHILD.getId()) };
        String tables = TABLE_LITTLE_PERSON + " p join " + TABLE_RELATIONSHIP + " r on r."+COL_ID1+"=p."+COL_ID
				+ " left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

        Map<Integer, LittlePerson> personMap = new HashMap<>();
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "p."+COL_ID);
        while (c.moveToNext()) {
            LittlePerson p = personFromCursor(c);
            personMap.put(p.getId(), p);
        }

        c.close();
        people.addAll(personMap.values());

        return people;
    }

	public List<LittlePerson> getChildrenForPerson(int id) {
		List<LittlePerson> people = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
				COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
				"p."+COL_ID, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES,
				COL_HAS_MEDIA, COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = "r."+COL_ID1+" LIKE ? and r."+COL_TYPE+"=? and p."+COL_ACTIVE+"='Y'";

		String[] selectionArgs = { String.valueOf(id), String.valueOf(RelationshipType.PARENTCHILD.getId()) };
		String tables = TABLE_LITTLE_PERSON + " p join " + TABLE_RELATIONSHIP + " r on r."+COL_ID2+"=p."+COL_ID
				+ " left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

		Map<Integer, LittlePerson> personMap = new HashMap<>();
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "p." + COL_AGE + " desc");
		while (c.moveToNext()) {
			LittlePerson p = personFromCursor(c);
			if (!personMap.containsKey(p.getId())) {
				personMap.put(p.getId(), p);
				people.add(p);
			}
		}

		c.close();

		return people;
	}

	public List<LittlePerson> getSpousesForPerson(int id) {
		List<LittlePerson> people = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_GIVEN_NAME, COL_GENDER, COL_PHOTO_PATH, COL_NAME,
				COL_AGE, COL_BIRTH_DATE, COL_BIRTH_PLACE, COL_NATIONALITY, COL_FAMILY_SEARCH_ID, COL_LAST_SYNC,
				"p."+COL_ID, COL_ALIVE, COL_ACTIVE, COL_HAS_PARENTS, COL_HAS_CHILDREN, COL_HAS_SPOUSES,
				COL_HAS_MEDIA, COL_TREE_LEVEL, COL_OCCUPATION, "l."+COL_LOCAL_PATH
		};
		String selection = "(r."+COL_ID1+" LIKE ? or r."+COL_ID2+" LIKE ?) and r."+COL_TYPE+"=? and p."+COL_ACTIVE+"='Y'";

		String[] selectionArgs = { String.valueOf(id), String.valueOf(id), String.valueOf(RelationshipType.SPOUSE.getId()) };
		String tables = TABLE_LITTLE_PERSON + " p join " + TABLE_RELATIONSHIP + " r on r."+COL_ID1+"=p."+COL_ID+" or r."+COL_ID2+"=p."+COL_ID
				+ " left outer join "+TABLE_LOCAL_RESOURCES+" l on l."+COL_PERSON_ID+"=p."+COL_ID+" and l."+COL_TYPE+"='"+TYPE_GIVEN_AUDIO+"' ";

		Map<Integer, LittlePerson> personMap = new HashMap<>();
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "p." + COL_ID);
		while (c.moveToNext()) {
			LittlePerson p = personFromCursor(c);
			if (p.getId()!=id) {
				personMap.put(p.getId(), p);
			}
		}

		c.close();
		people.addAll(personMap.values());

		return people;
	}
	
	public LittlePerson personFromCursor(Cursor c) {
		LittlePerson person = new LittlePerson();
		person.setAge(c.getInt(c.getColumnIndexOrThrow(COL_AGE)));
		if (!c.isNull(c.getColumnIndexOrThrow(COL_BIRTH_DATE))) {
			long birthtime = c.getLong(c.getColumnIndexOrThrow(COL_BIRTH_DATE));
			person.setBirthDate(new Date(birthtime));
		}
        person.setBirthPlace(c.getString(c.getColumnIndexOrThrow(COL_BIRTH_PLACE)));
        person.setNationality(c.getString(c.getColumnIndexOrThrow(COL_NATIONALITY)));
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
		if (person.getPhotoPath()!=null && !person.getPhotoPath().contains(".LittleFamilyTreeData")) {
			person.setPhotoPath(person.getPhotoPath().replace("LittleFamilyData", ".LittleFamilyTreeData"));
		}
        if (!c.isNull(c.getColumnIndexOrThrow(COL_LAST_SYNC))) {
            long synctime = c.getLong(c.getColumnIndexOrThrow(COL_LAST_SYNC));
            person.setLastSync(new Date(synctime));
        }
        person.setAlive(c.getString(c.getColumnIndexOrThrow(COL_ALIVE)).equals("Y") ? true : false);
        person.setActive(c.getString(c.getColumnIndexOrThrow(COL_ACTIVE)).equals("Y") ? true : false);
        String hasParentsStr = c.getString(c.getColumnIndexOrThrow(COL_HAS_PARENTS));
        if (hasParentsStr.equals("Y")) person.setHasParents(true);
        else if (hasParentsStr.equals("N")) person.setHasParents(false);
        else person.setHasParents(null);
		String hasChildrenStr = c.getString(c.getColumnIndexOrThrow(COL_HAS_CHILDREN));
		if (hasChildrenStr.equals("Y")) person.setHasChildren(true);
		else if (hasChildrenStr.equals("N")) person.setHasChildren(false);
		else person.setHasChildren(null);
		String hasSpousesStr = c.getString(c.getColumnIndexOrThrow(COL_HAS_SPOUSES));
		if (hasSpousesStr.equals("Y")) person.setHasSpouses(true);
		else if (hasSpousesStr.equals("N")) person.setHasSpouses(false);
		else person.setHasSpouses(null);
		String hasMediaStr = c.getString(c.getColumnIndexOrThrow(COL_HAS_MEDIA));
		if (hasMediaStr!=null && hasMediaStr.equals("Y")) person.setHasMedia(true);
		else if (hasMediaStr!=null && hasMediaStr.equals("N")) person.setHasMedia(false);
		else person.setHasMedia(null);
		if (!c.isNull(c.getColumnIndexOrThrow(COL_TREE_LEVEL))) {
			person.setTreeLevel(c.getInt(c.getColumnIndexOrThrow(COL_TREE_LEVEL)));
		}
		person.setOccupation(c.getString(c.getColumnIndexOrThrow(COL_OCCUPATION)));
		person.setGivenNameAudioPath(c.getString(c.getColumnIndexOrThrow(COL_LOCAL_PATH)));
		person.updateAge();

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
			Relationship oldR = this.getRelationship(r.getId1(), r.getId2(), r.getType());
            if (oldR != null) {
				//-- check id old relationship is different
				if (!oldR.getType().equals(r.getType()) || oldR.getId1()!=r.getId1() || oldR.getId2()!=r.getId2()) {
					deleteRelationshipById(oldR.getId());
				} else {
					return -1;
				}
			}
            // -- add
            rowid = db.insert(TABLE_RELATIONSHIP, null, values);
            Log.d("DBHelper", "persistRelationship added relationship id " + rowid + " id1="+r.getId1()+" id2="+r.getId2()+" type="+r.getType());
        } else {
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(r.getId()) };

            int count = db.update(TABLE_RELATIONSHIP, values, selection, selectionArgs);
            Log.d("DBHelper", "persistRelationship updated " + count + " rows");
            rowid = r.getId();
        }
			
		return rowid;
	}
	
	public Relationship getRelationship(int id1, int id2, RelationshipType type) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_ID, COL_ID1, COL_ID2, COL_TYPE
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

		return r;
	}
	
	public List<Relationship> getRelationshipsForPerson(int id) {
		List<Relationship> relationships = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
			COL_ID, COL_ID1, COL_ID2, COL_TYPE
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

		return relationships;
	}
	
	public Relationship relationshipFromCursor(Cursor c) {
		Relationship r = new Relationship();
		r.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
		r.setId1(c.getInt(c.getColumnIndexOrThrow(COL_ID1)));
		r.setId2(c.getInt(c.getColumnIndexOrThrow(COL_ID2)));
		int type = c.getInt(c.getColumnIndexOrThrow(COL_TYPE));
		RelationshipType rt = RelationshipType.getTypeFromId(type);
		r.setType(rt);
		return r;
	}

    public void deleteRelationshipById(int id) {
        if (id>0) {
            SQLiteDatabase db = getWritableDatabase();
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(id) };
            int count = db.delete(TABLE_RELATIONSHIP, selection, selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_RELATIONSHIP);
        }
    }

    public void persistMedia(Media media) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_FAMILY_SEARCH_ID, media.getFamilySearchId());
        values.put(COL_TYPE, media.getType());
        values.put(COL_LOCAL_PATH, media.getLocalPath());

        // -- add
        if (media.getId() == 0) {
            Media existing = getMediaByFamilySearchId(media.getFamilySearchId());
            if (existing!=null) {
                media.setId(existing.getId());
				String selection = COL_ID + " LIKE ?";
				String[] selectionArgs = { String.valueOf(media.getId()) };

				int count = db.update(TABLE_MEDIA, values, selection, selectionArgs);
				Log.d("DBHelper", "persistMedia updated " + count + " rows");
            } else {
                long rowid = db.insert(TABLE_MEDIA, null, values);
                media.setId((int) rowid);
                Log.d("DBHelper", "persistMedia added media with id " + rowid);
            }
        }
        // --update
        else {
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(media.getId()) };

            int count = db.update(TABLE_MEDIA, values, selection, selectionArgs);
            Log.d("DBHelper", "persistMedia updated " + count + " rows");
        }
    }

    public Media getMediaByFamilySearchId(String fsid) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COL_LOCAL_PATH, COL_TYPE, COL_FAMILY_SEARCH_ID,
                COL_ID
        };
        String selection = COL_FAMILY_SEARCH_ID + " LIKE ?";
        String[] selectionArgs = { fsid };
        String tables = TABLE_MEDIA;

        Media media = null;
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_FAMILY_SEARCH_ID);
        while (c.moveToNext()) {
            media = mediaFromCursor(c);
        }

        c.close();

        return media;
    }

    public List<Media> getMediaForPerson(int personId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COL_LOCAL_PATH, COL_TYPE, COL_FAMILY_SEARCH_ID,
                "m."+COL_ID
        };
        String selection = COL_PERSON_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(personId) };
        String tables = TABLE_MEDIA + " m join "+TABLE_TAGS+" t on m."+COL_ID+"=t."+COL_MEDIA_ID;

        List<Media> mediaList = new ArrayList<>();
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, "m."+COL_ID);
        while (c.moveToNext()) {
            Media media = mediaFromCursor(c);
            mediaList.add(media);
        }

        c.close();

        return mediaList;
    }

    public void deleteMediaById(int mid) {
        if (mid>0) {
            SQLiteDatabase db = getWritableDatabase();
            String[] selectionArgs = { String.valueOf(mid) };

            int count = db.delete(TABLE_TAGS, COL_MEDIA_ID+" LIKE ?", selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_TAGS);

            String selection = COL_ID + " LIKE ?";
            count = db.delete(TABLE_MEDIA, selection, selectionArgs);
            Log.d("DBHelper", "deleted "+count+" from "+TABLE_MEDIA);
        }
    }

    private Media mediaFromCursor(Cursor c) {
        Media m = new Media();
        m.setFamilySearchId(c.getString(c.getColumnIndexOrThrow(COL_FAMILY_SEARCH_ID)));
        m.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        m.setLocalPath(c.getString(c.getColumnIndexOrThrow(COL_LOCAL_PATH)));
        m.setType(c.getString(c.getColumnIndexOrThrow(COL_TYPE)));
        return m;
    }

	public long getMediaCount() {
		long totalMedia = 0;
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select count(*) from " + TABLE_MEDIA, null);
		while (c.moveToNext()) {
			totalMedia = c.getLong(0);
		}
		c.close();

		c = db.rawQuery("select count(*) from " + TABLE_LITTLE_PERSON+" where "+COL_PHOTO_PATH+" is not NULL", null);
		while (c.moveToNext()) {
			totalMedia += c.getLong(0);
		}
		c.close();

		return totalMedia;
	}

    public void persistTag(Tag tag) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_MEDIA_ID, tag.getMediaId());
        values.put(COL_PERSON_ID, tag.getPersonId());
        values.put(COL_LEFT, tag.getLeft());
        values.put(COL_TOP, tag.getTop());
        values.put(COL_RIGHT, tag.getRight());
        values.put(COL_BOTTOM, tag.getBottom());

        // -- add
        Tag oldTag = getTagForPersonMedia(tag.getPersonId(), tag.getMediaId());
        if (oldTag==null && tag.getId() == 0) {
            long rowid = db.insert(TABLE_TAGS, null, values);
            tag.setId((int) rowid);
            Log.d("DBHelper", "persistTag added tag with id " + rowid);
        }
        // --update
        else {
            if (oldTag!=null) {
                tag.setId(oldTag.getId());
            }
            String selection = COL_ID + " LIKE ?";
            String[] selectionArgs = { String.valueOf(tag.getId()) };

            int count = db.update(TABLE_TAGS, values, selection, selectionArgs);
            Log.d("DBHelper", "persistTag updated " + count + " rows");
        }
    }

    public Tag getTagById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COL_MEDIA_ID, COL_PERSON_ID, COL_LEFT, COL_RIGHT, COL_TOP, COL_BOTTOM,
                COL_ID
        };
        String selection = COL_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(id) };
        String tables = TABLE_TAGS;

        Tag tag = null;
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
        while (c.moveToNext()) {
            tag = tagFromCursor(c);
        }

        c.close();

        return tag;
    }

    public Tag getTagForPersonMedia(int pid, int mid) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                COL_MEDIA_ID, COL_PERSON_ID, COL_LEFT, COL_RIGHT, COL_TOP, COL_BOTTOM,
                COL_ID
        };
        String selection = COL_PERSON_ID + " LIKE ? AND "+COL_MEDIA_ID+" LIKE ?";
        String[] selectionArgs = { String.valueOf(pid), String.valueOf(mid) };
        String tables = TABLE_TAGS;

        Tag tag = null;
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
        while (c.moveToNext()) {
            tag = tagFromCursor(c);
        }

        c.close();

        return tag;
    }

    private Tag tagFromCursor(Cursor c) {
        Tag tag = new Tag();
        tag.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        tag.setMediaId(c.getInt(c.getColumnIndexOrThrow(COL_MEDIA_ID)));
        tag.setPersonId(c.getInt(c.getColumnIndexOrThrow(COL_PERSON_ID)));
        if (!c.isNull(c.getColumnIndexOrThrow(COL_LEFT)))
            tag.setLeft(c.getDouble(c.getColumnIndexOrThrow(COL_LEFT)));
        if (!c.isNull(c.getColumnIndexOrThrow(COL_TOP)))
            tag.setLeft(c.getDouble(c.getColumnIndexOrThrow(COL_TOP)));
        if (!c.isNull(c.getColumnIndexOrThrow(COL_RIGHT)))
            tag.setLeft(c.getDouble(c.getColumnIndexOrThrow(COL_RIGHT)));
        if (!c.isNull(c.getColumnIndexOrThrow(COL_BOTTOM)))
            tag.setLeft(c.getDouble(c.getColumnIndexOrThrow(COL_BOTTOM)));
        return tag;
    }

	public void persistLocalResource(LocalResource media) {
		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COL_PERSON_ID, media.getPersonId());
		values.put(COL_TYPE, media.getType());
		values.put(COL_LOCAL_PATH, media.getLocalPath());

		// -- add
		if (media.getId() == 0) {
			LocalResource existing = getLocalResource(media);
			if (existing!=null) {
				media.setId(existing.getId());
				String selection = COL_ID + " LIKE ?";
				String[] selectionArgs = { String.valueOf(media.getId()) };

				int count = db.update(TABLE_LOCAL_RESOURCES, values, selection, selectionArgs);
				Log.d("DBHelper", "persistLocalResource updated " + count + " rows");
			} else {
				long rowid = db.insert(TABLE_LOCAL_RESOURCES, null, values);
				media.setId((int) rowid);
				Log.d("DBHelper", "persistLocalResource added local resource with id " + rowid);
			}
		}
		// --update
		else {
			String selection = COL_ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(media.getId()) };

			int count = db.update(TABLE_LOCAL_RESOURCES, values, selection, selectionArgs);
			Log.d("DBHelper", "persistLocalResource updated " + count + " rows");
		}
	}

	public LocalResource getLocalResource(LocalResource media) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_ID, COL_PERSON_ID, COL_TYPE, COL_LOCAL_PATH
		};
		String selection = COL_PERSON_ID + " = ? and "+COL_TYPE+"=?";
		String[] selectionArgs = { String.valueOf(media.getPersonId()), media.getType() };
		String tables = TABLE_LOCAL_RESOURCES;

		LocalResource lr = null;
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, null);
		while (c.moveToNext()) {
			lr = localResourceFromCursor(c);
		}

		c.close();

		return lr;
	}

	public List<LocalResource> getLocalResourcesForPerson(int personId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_PERSON_ID, COL_LOCAL_PATH, COL_TYPE, COL_ID
		};
		String selection = COL_PERSON_ID + " LIKE ?";
		String[] selectionArgs = { String.valueOf(personId) };
		String tables = TABLE_LOCAL_RESOURCES;

		List<LocalResource> mediaList = new ArrayList<>();
		Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_ID);
		while (c.moveToNext()) {
			LocalResource media = localResourceFromCursor(c);
			mediaList.add(media);
		}

		c.close();

		return mediaList;
	}

	public void deleteLocalResourceById(int mid) {
		if (mid > 0) {
			SQLiteDatabase db = getWritableDatabase();
			String[] selectionArgs = {String.valueOf(mid)};

			String selection = COL_ID + " LIKE ?";
			int count = db.delete(TABLE_LOCAL_RESOURCES, selection, selectionArgs);
			Log.d("DBHelper", "deleted " + count + " from " + TABLE_LOCAL_RESOURCES);
		}
	}

	private LocalResource localResourceFromCursor(Cursor c) {
		LocalResource m = new LocalResource();
		m.setPersonId(c.getInt(c.getColumnIndexOrThrow(COL_PERSON_ID)));
		m.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
		m.setLocalPath(c.getString(c.getColumnIndexOrThrow(COL_LOCAL_PATH)));
		m.setType(c.getString(c.getColumnIndexOrThrow(COL_TYPE)));
		return m;
	}

    public void saveProperty(String property, String value) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = "property LIKE ?";
        String[] selectionArgs = { property };
        int count = db.delete(TABLE_PROPERTIES, selection, selectionArgs);
        Log.d("DBHelper", "deleted " + count + " from " + TABLE_PROPERTIES);

        ContentValues values = new ContentValues();
        values.put("property", property);
        values.put("value", value);
        values.put(COL_LAST_SYNC, (new Date()).getTime());

        long rowid = db.insert(TABLE_PROPERTIES, null, values);
        Log.d("DBHelper", "saveProperty rowid " + rowid + " "+property+"="+value);

    }

    public String getProperty(String property) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                "value", COL_LAST_SYNC
        };
        String selection = "property LIKE ?";
        String[] selectionArgs = { property };
        String tables = TABLE_PROPERTIES;

        String value = null;
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_LAST_SYNC);
        while (c.moveToNext()) {
            value = c.getString(c.getColumnIndexOrThrow("value"));
        }

        c.close();

        return value;
    }

    public String getTokenForSystemId(String systemId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                "value", COL_LAST_SYNC
        };
        String selection = "property LIKE ?";
        String[] selectionArgs = { systemId };
        String tables = TABLE_PROPERTIES;

        String value = null;
        Cursor c = db.query(tables, projection, selection, selectionArgs, null, null, COL_LAST_SYNC);
        while (c.moveToNext()) {
            value = c.getString(c.getColumnIndexOrThrow("value"));
            Date date = new Date(c.getLong(c.getColumnIndexOrThrow(COL_LAST_SYNC)));
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            if (date.before(cal.getTime())) {
                value = null;
            }
        }

        c.close();

        return value;
    }

	public void addToSyncQ(int id) {
		SQLiteDatabase db = getWritableDatabase();

		String selection = COL_ID+" LIKE ?";
		String[] selectionArgs = { String.valueOf(id) };
		int count = db.delete(TABLE_SYNCQ, selection, selectionArgs);
		Log.d("DBHelper", "deleted " + count + " from " + TABLE_SYNCQ);

		ContentValues values = new ContentValues();
		values.put(COL_ID, id);

		long rowid = db.insert(TABLE_SYNCQ, null, values);
		Log.d("DBHelper", "addToSyncQ " + id);
	}

	public void removeFromSyncQ(int id) {
		SQLiteDatabase db = getWritableDatabase();

		String selection = COL_ID+" LIKE ?";
		String[] selectionArgs = { String.valueOf(id) };
		int count = db.delete(TABLE_SYNCQ, selection, selectionArgs);
		Log.d("DBHelper", "deleted " + count + " from " + TABLE_SYNCQ);
	}

	public List<Integer> getSyncQ() {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = {
				COL_ID
		};

		List<Integer> list = new ArrayList<>();
		Cursor c = db.query(TABLE_SYNCQ, projection, null, null, null, null, COL_ID);
		while (c.moveToNext()) {
			Integer id = c.getInt(c.getColumnIndexOrThrow(COL_ID));
			list.add(id);
		}

		c.close();

		return list;
	}
}
