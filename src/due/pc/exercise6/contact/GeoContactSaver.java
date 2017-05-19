package due.pc.exercise6.contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import due.pc.exercise6.FriendFinderDatabase;
import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.common.GpsData;
import due.pc.exercise6.contact.db.GeoContactTbl;


public class GeoContactSaver {

  private static final String WHERE_CALLNUMBER_EQUALS = GeoContactTbl.CELLNUMBER + "=?";
  private static final String ORDER_BY_TIMESTAMP = GeoContactTbl.TIMESTAMP + " DESC";

  private FriendFinderDatabase mDb;

  
  public GeoContactSaver(Context context) {
    mDb = new FriendFinderDatabase(context);
    open();
  }

  //restrict object generation
  private GeoContactSaver() {
  }

 
  public long saveGeoContact(String name, String lookupKey, String cellnumber, String note, double longitude, double latitude, double altitude, long timestamp) {


    final ContentValues data = new ContentValues();
    data.put(GeoContactTbl.NAME, name);
    data.put(GeoContactTbl.LOOKUP_KEY, lookupKey);
    data.put(GeoContactTbl.CELLNUMBER, cellnumber);
    if (note != null) {
      data.put(GeoContactTbl.NOTE, note);
      data.put(GeoContactTbl.LONGITUDE, longitude);
      data.put(GeoContactTbl.LATITUDE, latitude);
      data.put(GeoContactTbl.ALTITUDE, altitude);
      data.put(GeoContactTbl.TIMESTAMP, timestamp);
    }

    final SQLiteDatabase dbCon = mDb.getWritableDatabase();

    try {
      final long id = dbCon.insertOrThrow(GeoContactTbl.TABLE_NAME, null, data);
      return id;
    } finally {
      dbCon.close();
    }
  }

 
  public long saveGeoContact(GeoContact contact) {
    if (contact.lastPosition == null) {
      if (contact.isNew()) {
        return saveGeoContact(
            contact.name,
            contact.lookupKey,
            contact.cellnumber,
            null, 0, 0, 0, 0);
      } else {
        editGeoContact(
            contact.id,
            contact.name,
            contact.lookupKey,
            contact.cellnumber,
            null, 0, 0, 0, 0);
        return contact.id;
      }
    }
    if (contact.isNew()) {
      return saveGeoContact(
          contact.name,
          contact.lookupKey,
          contact.cellnumber,
          contact.lastPosition.note,
          contact.lastPosition.gpsData.longitude,
          contact.lastPosition.gpsData.latitude,
          contact.lastPosition.gpsData.altitude,
          contact.lastPosition.gpsData.timestamp);
    } else {
      editGeoContact(
          contact.id,
          contact.name,
          contact.lookupKey,
          contact.cellnumber,
          contact.lastPosition.note,
          contact.lastPosition.gpsData.longitude,
          contact.lastPosition.gpsData.latitude,
          contact.lastPosition.gpsData.altitude,
          contact.lastPosition.gpsData.timestamp);
      return contact.id;
    }
  }


  public void editGeoContact(long id, String name, String lookupKey, String cellnumber, String note, double longitude, double latitude, double altitude, long timestamp) {
    if (id == 0) {
      return;
    }

    final ContentValues data = new ContentValues();
    data.put(GeoContactTbl.NAME, name);
    data.put(GeoContactTbl.LOOKUP_KEY, lookupKey);
    data.put(GeoContactTbl.CELLNUMBER, cellnumber);
    if (note != null) {
      data.put(GeoContactTbl.NOTE, note);
      data.put(GeoContactTbl.LONGITUDE, longitude);
      data.put(GeoContactTbl.LATITUDE, latitude);
      data.put(GeoContactTbl.ALTITUDE, altitude);
      data.put(GeoContactTbl.TIMESTAMP, timestamp);
    }

    final SQLiteDatabase dbCon = mDb.getWritableDatabase();

    try {
      dbCon.update(GeoContactTbl.TABLE_NAME, data, GeoContactTbl.WHERE_ID_EQUALS, new String[] {String.valueOf(id)});
    } finally {
      dbCon.close();
    }
  }

 
  public void editGeoContact(long id, double longitude, double latitude, double altitude, long timestamp) {
    if (id == 0) {
      return;
    }

    final ContentValues data = new ContentValues();
    data.put(GeoContactTbl.LONGITUDE, longitude);
    data.put(GeoContactTbl.LATITUDE, latitude);
    data.put(GeoContactTbl.ALTITUDE, altitude);
    data.put(GeoContactTbl.TIMESTAMP, timestamp);

    final SQLiteDatabase dbCon = mDb.getWritableDatabase();

    try {
      dbCon.update(GeoContactTbl.TABLE_NAME, data, GeoContactTbl.WHERE_ID_EQUALS, new String[] {String.valueOf(id)});
    } finally {
      dbCon.close();
    }
  }

  
  public boolean deleteGeoContact(long id) {
    final SQLiteDatabase dbCon = mDb.getWritableDatabase();

    int deleteCount = 0;
    try {
      deleteCount = dbCon.delete(GeoContactTbl.TABLE_NAME, GeoContactTbl.WHERE_ID_EQUALS, new String[] {String.valueOf(id)});
    } finally {
      dbCon.close();
    }
    return deleteCount == 1;
  }

 
  public Cursor loadGeoContactDetails(long id) {
    return mDb.getReadableDatabase().query(GeoContactTbl.TABLE_NAME, GeoContactTbl.ALL_COLUMNS, GeoContactTbl.WHERE_ID_EQUALS, new String[] {String.valueOf(id)}, null, null, null);
  }

  
  public GeoContact loadGeoContact(long id) {
    GeoContact contact = null;
    Cursor c = null;
    try {
      c = mDb.getReadableDatabase().query(
          GeoContactTbl.TABLE_NAME, 
          GeoContactTbl.ALL_COLUMNS,
          GeoContactTbl.WHERE_ID_EQUALS, new String[] {String.valueOf(id)}, null, null, null);
      if (c.moveToFirst() == false) {
        return null;
      }
      contact = loadGeoContact(c);
    } finally {
      if (c != null) {
        c.close();
      }
    }
    return contact;
  }

 
  public Cursor loadGeoContactDetails(String cellnumber) {
    if (cellnumber == null) {
      return null;
    }
    return mDb.getReadableDatabase().query(GeoContactTbl.TABLE_NAME, GeoContactTbl.ALL_COLUMNS, WHERE_CALLNUMBER_EQUALS, new String[] {cellnumber}, null, null, ORDER_BY_TIMESTAMP);
  }
  
  public Cursor loadGeoContactList() {
    final SQLiteQueryBuilder contactSearch = new SQLiteQueryBuilder();
    contactSearch.setTables(GeoContactTbl.TABLE_NAME);
    String[] whereAttribs = null;
    
    return contactSearch.query(mDb.getReadableDatabase(), GeoContactTbl.ALL_COLUMNS, null, whereAttribs, null, null, null);
  }

  
  public GeoContact loadGeoContact(Cursor c) {
    final GeoContact contact = new GeoContact();

    contact.id = c.getLong(c.getColumnIndex(GeoContactTbl.ID));
    contact.name = c.getString(c.getColumnIndex(GeoContactTbl.NAME));
    contact.lookupKey = c.getString(c.getColumnIndex(GeoContactTbl.LOOKUP_KEY));
    contact.cellnumber = c.getString(c.getColumnIndex(GeoContactTbl.CELLNUMBER));
    final GpsData gpsData = new GpsData(
        c.getDouble(c.getColumnIndex(GeoContactTbl.LONGITUDE)),
        c.getDouble(c.getColumnIndex(GeoContactTbl.LATITUDE)),
        c.getDouble(c.getColumnIndex(GeoContactTbl.ALTITUDE)),
        c.getLong(c.getColumnIndex(GeoContactTbl.TIMESTAMP)));
    final GeoMarking lastPosition = new GeoMarking(
        c.getString(c.getColumnIndex(GeoContactTbl.NOTE)), gpsData);
    contact.lastPosition = lastPosition;
    return contact;
  }
  
  
  public void close() {
    mDb.close();
  }
  
  
  public void open() {
    mDb.getReadableDatabase();
  }

  
  public int countGeoContacts() {
    final Cursor c = mDb.getReadableDatabase().rawQuery("select count(*) from " + GeoContactTbl.TABLE_NAME, null);
    if (c.moveToFirst() == false) {
      return 0;
    }
    return c.getInt(0);
  }

}
