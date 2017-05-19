package due.pc.exercise6.contact.db;


public final class GeoContactTbl implements GeoContactColumns {
  
  public static final String TABLE_NAME = "geocontacts";

  
  public static final String SQL_CREATE =
      "CREATE TABLE geocontacts (" +
      "_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
      "name TEXT NOT NULL," +
      "lookup_key TEXT," +
      "cellnumber TEXT," +
      "note TEXT," +
      "longitude REAL, " +
      "latitude REAL, " +
      "altitude REAL, " +
      "timestamp INTEGER " +
      ");";
  
  
  public static final String DEFAULT_SORT_ORDER = TIMESTAMP + " DESC";

  
  public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

  
  public static final String STMT_MIN_INSERT =
      "INSERT INTO geocontacts " +
      "(name) " +
      "VALUES (?)";

  
  public static final String STMT_CONTACT_INSERT =
      "INSERT INTO geocontacts " +
      "(name,mobilnummer) " +
      "VALUES (?,?)";

  
  public static final String STMT_CONTACT_DELETE = "DELETE geocontacts ";
  
  
  public static final String STMT_KONTAKT_DELETE_BY_ID =
    "DELETE geocontacts " +
    "WHERE _id = ?";

  public static final String[] ALL_COLUMNS = new String[] {
      ID,
      NAME,
      LOOKUP_KEY,
      CELLNUMBER,
      NOTE,
      LONGITUDE,
      LATITUDE,
      ALTITUDE,
      TIMESTAMP
      };

  
  public static final String WHERE_ID_EQUALS =
      ID + "=?";

  private GeoContactTbl() {
  }
}
