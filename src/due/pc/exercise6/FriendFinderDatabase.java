package due.pc.exercise6;

import due.pc.exercise6.contact.db.GeoContactTbl;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class FriendFinderDatabase extends SQLiteOpenHelper {

  private static final String DATABASENAME = "ff.db";
  private static final int DATABASE_VERSION = 1;


  public FriendFinderDatabase(Context context) {
    super(context, DATABASENAME, null, DATABASE_VERSION);    
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(GeoContactTbl.SQL_CREATE);;
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    
    db.execSQL(GeoContactTbl.SQL_DROP);
    onCreate(db);
  }  
}
