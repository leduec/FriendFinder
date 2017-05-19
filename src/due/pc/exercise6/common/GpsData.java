package due.pc.exercise6.common;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.maps.GeoPoint;


public class GpsData {
  
  public static final String KEY_LONGITUDE = "longitude";
  public static final String KEY_LATITUDE = "latitude";
  public static final String KEY_ALTITUDE = "altitude";
  public static final String KEY_TIMESTAMP = "timestamp";
  
  public double longitude;
  public double latitude;
  public double altitude;
  public long timestamp;

  
  public static GpsData valueOf(final Location position) {
    return new GpsData(position.getLongitude(), position.getLatitude(), position.getAltitude(), position.getTime());
  }

 
  public GpsData(final double longitude, final double latitude, final double altitude, final long timestamp) {
    this.longitude = longitude;
    this.latitude = latitude;
    this.altitude = altitude;
    this.timestamp = timestamp;
  }

  
  public GeoPoint toGeoPoint() {
    return new GeoPoint((int)(latitude * 1E6), (int)(longitude * 1E6));
  }
  
  public Location toLocation() {
    final Location location = new Location(LocationManager.GPS_PROVIDER);
    location.setLongitude(longitude); 
    location.setLatitude(latitude);    
    location.setAltitude(altitude);
    location.setTime(timestamp);
    
    return location;
  }
  
  @Override
  public String toString() {
    return "GpsData [latitude=" + latitude + ", altitude=" + altitude + ", longitude=" + longitude + ", timestamp=" + timestamp + "]";
  }

}
