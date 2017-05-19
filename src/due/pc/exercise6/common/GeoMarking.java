package due.pc.exercise6.common;


public class GeoMarking {
	
  public static final String KEY_NOTE = "note";
  
	public String note;	
	public GpsData gpsData;
	
	
	public GeoMarking(final String note, final GpsData gpsData) {
		this.note = note;		
		this.gpsData = gpsData;
	}

  @Override
  public String toString() {
    return "GeoMarking [gpsData=" + gpsData.toString() + ", note=" + note + "]";
  }
}
