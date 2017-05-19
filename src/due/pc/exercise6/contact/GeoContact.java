package due.pc.exercise6.contact;

import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.common.GpsData;


public class GeoContact {

  public static final String KEY_CELLNUMBER = "cellnumber";
  
  public String name;
  public String cellnumber;
  public long id;
  public String lookupKey;
  public GeoMarking lastPosition;
  
  public boolean isNew() {
    return id == 0;
  }

  public void setStartValues() {
    final GpsData gpsData = new GpsData(0, 0, 0, System.currentTimeMillis());
    final GeoMarking startPosition = new GeoMarking("", gpsData);
    this.lastPosition = startPosition;
  }
}
