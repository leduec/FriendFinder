package due.pc.exercise6.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import due.pc.exercise6.R;
import due.pc.exercise6.contact.GeoContact;
import due.pc.exercise6.contact.GeoContactSaver;
import due.pc.exercise6.contact.db.GeoContactTbl;


public class ShowMap extends MapActivity {
	
  static final String IN_PARAM_CONTACT_ID = "CONTACT_ID";
  
  public static final String IN_PARAM_GEO_POSITION = "location";

  private long geoContactId;
  private GeoContact friendContact;
  
  private MapView mapView;
  private MapController mapController;
  
  private MyLocationOverlay myLocationOverlay;
  private GeoKontaktOverlay mapViewOverlay;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
      
    setContentView(R.layout.show_map);
       
    useIntentBundle(getIntent().getExtras());
    
    initMapView();
    initGeocontactOverlay();
    initMyLocationOverlay();
  }
  
  
  private void useIntentBundle(final Bundle extras) {
    if (extras != null) {
      if (extras.containsKey(IN_PARAM_CONTACT_ID)) {
        geoContactId = extras.getLong(IN_PARAM_CONTACT_ID);
      } 
    }
  }

 
  private void initMapView() {
    mapView = (MapView)findViewById(R.id.mapview_map); 
    mapController = mapView.getController();
  
    final int maxZoomlevel = mapView.getMaxZoomLevel();
   
    mapController.setZoom(maxZoomlevel - 4); 
    mapView.setBuiltInZoomControls(true);
    mapView.setStreetView(true);
  }

  private void initMyLocationOverlay() { 
	  myLocationOverlay = new MyLocationOverlay(this, mapView) {
      
      @Override
      public void onLocationChanged(Location newPosition) {
        super.onLocationChanged(newPosition);
        
        final GeoPoint ownPosition = new GeoPoint((int)(newPosition.getLatitude() * 1E6), (int)(newPosition.getLongitude() * 1E6));
  
       
        mapController.animateTo(ownPosition);
      }
    };
    
    mapView.getOverlays().add(myLocationOverlay);
    myLocationOverlay.enableMyLocation();
    myLocationOverlay.enableCompass();
    
    
    myLocationOverlay.runOnFirstFix(new Runnable() {
      public void run() {
        mapController.animateTo(myLocationOverlay.getMyLocation());
      }
    });
  }

  
  private void initGeocontactOverlay() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String myNick = prefs.getString("nickname", "me");
    mapViewOverlay = new GeoKontaktOverlay(myNick); 
    mapView.getOverlays().add(mapViewOverlay);
    mapView.postInvalidate();
  }


  @Override
  protected void onStart() {
    super.onStart();
    
    loadGeoContact();    
  }

  
  private void loadGeoContact() {
    final GeoContactSaver contactSaver = new GeoContactSaver(this);   
    
    if (geoContactId != 0) {
      friendContact = contactSaver.loadGeoContact(geoContactId);     
    } else {
      final Cursor c = contactSaver.loadGeoContactList();
      c.moveToFirst();
      friendContact = contactSaver.loadGeoContact(c.getLong(c.getColumnIndex(GeoContactTbl.ID)));
    }
    contactSaver.close();
  }
  
  
  @Override
  protected void onResume() {
    
    myLocationOverlay.enableMyLocation();
    myLocationOverlay.enableCompass();
    
    mapView.invalidate();

    super.onResume();
  }

  
  
  @Override
  protected void onPause() {
    myLocationOverlay.disableMyLocation();
    myLocationOverlay.disableCompass();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  
  public class GeoKontaktOverlay extends Overlay {
    
    
    private final String myName;
    private final Point friendPoint = new Point();
    private final Point myPoint = new Point();
    private final RectF rect = new RectF();
    private final Paint paint = new Paint();
    
    
    public GeoKontaktOverlay(final String myNickname) {
      myName = myNickname;
    }
    
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
      super.draw(canvas, mapView, shadow);
  
      final GeoPoint friendPosition = friendContact.lastPosition.gpsData.toGeoPoint();
  
      GeoPoint ownPosition = myLocationOverlay.getMyLocation();
      
      if (ownPosition == null) {
        ownPosition = new GeoPoint((int)(51.3 * 1E6), (int)(7.2 * 1E6));
      }  
  
      mapView.getProjection().toPixels(friendPosition, friendPoint);
  
      mapView.getProjection().toPixels(ownPosition, myPoint);
      
      float textWidth = paint.measureText(myName);
      float textHeight = paint.getTextSize();
      rect.set(myPoint.x + 13, 
          myPoint.y - textHeight,
          myPoint.x + 13 + 8 + textWidth, 
          myPoint.y + 4);
      paint.setARGB(128, 255, 255, 255);
      paint.setStyle(Style.FILL);
      canvas.drawRect(rect, paint);
      
      paint.setARGB(255, 0, 0, 0);
      canvas.drawText(myName, myPoint.x + 13 + 4, myPoint.y, paint);
   
      canvas.drawLine(myPoint.x, myPoint.y, friendPoint.x, friendPoint.y, paint);
  
      rect.set(friendPoint.x - 5, friendPoint.y + 5, friendPoint.x + 5, friendPoint.y - 5);
      
      paint.setARGB(255, 200, 0, 30);
      paint.setStyle(Style.FILL);
      canvas.drawOval(rect, paint);
  
      paint.setARGB(255, 0, 0, 0);
      paint.setStyle(Style.STROKE);
      canvas.drawCircle(friendPoint.x, friendPoint.y, 5, paint);
  
      String friendName = friendContact.name;
      if (friendContact == null) {
        friendName = "Unknown";
      } 
      textWidth = paint.measureText(friendName);
      textHeight = paint.getTextSize();
      rect.set(friendPoint.x + 8, 
          friendPoint.y - textHeight,
          friendPoint.x + 8 + 8 + textWidth, 
          friendPoint.y + 4);
      paint.setARGB(128, 64, 64, 64);
      paint.setStyle(Style.FILL);
      canvas.drawRect(rect, paint);
      paint.setARGB(255, 255, 255, 255);
      canvas.drawText(friendContact.name, friendPoint.x + 8 + 4, friendPoint.y, paint);
      
    }
  }

  //Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.show_map, menu);

    return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_sendPosition: 
        final Intent i = new Intent(this, SendPosition.class);
        startActivity(i);
        return true;
      
      case R.id.menu_startNavigation: 
        startNavigation();
        return true;
  
      case R.id.menu_geocontactCall: 
        callGeoContact();
        return true;
      
      case R.id.menu_showHelp:
        return true;
  
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  protected void startNavigation()  {
    final String geoContactPosition = friendContact.lastPosition.gpsData.latitude + "," + friendContact.lastPosition.gpsData.longitude;
    
    final String myPosition = myLocationOverlay.getMyLocation().getLatitudeE6() / 1E6 + "," + myLocationOverlay.getMyLocation().getLongitudeE6() / 1E6;
  
    final Intent navigation = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + myPosition + "&daddr=" + geoContactPosition));
    startActivity(navigation);
  }
  
  
  protected void callGeoContact() {
    if (friendContact == null || 
        friendContact.cellnumber == null ||
        friendContact.cellnumber.length() == 0
        ) {
      return;
    }
    final Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + friendContact.cellnumber));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }
  
  @Override
  protected boolean isLocationDisplayed() {
    return super.isLocationDisplayed();
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

}
