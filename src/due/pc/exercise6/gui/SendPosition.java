package due.pc.exercise6.gui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import due.pc.exercise6.R;
import due.pc.exercise6.common.FriendFinderSmsUtil;
import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.common.GpsData;
import due.pc.exercise6.contact.GeoContactSaver;
import due.pc.exercise6.contact.db.GeoContactTbl;

public class SendPosition extends Activity {
	
  private GeoContactSaver contactSaver;
  private long geoContactId;
  private String note;
  private List<String> receiverCellnumbers = new ArrayList<String>();
  private LocationManager locationManager;
  private LocationListener listenerGps;
  
  public GpsData ownGps;
    
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.send_position);
    
    ownGps = new GpsData(0, 0, 0, 0);
    locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
    initGps();
    
    final Intent i = new Intent(this, GeoContactList.class);
    i.putExtra(GeoContactList.SELECT_CONTACT, true);    
    startActivityForResult(i, 0);
  }
  
  
  private void initGps(){
	  listenerGps = new LocationListener() {
			
			public void onLocationChanged(Location location) {
				ownGps.longitude = location.getLongitude();
				ownGps.latitude = location.getLatitude();
				ownGps.altitude = location.getAltitude();
				ownGps.timestamp = location.getTime();
			}
			
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}
			public void onProviderEnabled(String provider) {	
			}
			public void onProviderDisabled(String provider) {
			}
		};
  }
  
	  @Override
	protected void onResume() {
		super.onResume();
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0.1f, listenerGps);
	}

  @Override
  protected void onStart() {
        
    showDetails();

    super.onStart();
  }

  @Override
  protected void onDestroy() {
    if (contactSaver != null) {
      contactSaver.close();
    }
    
    locationManager.removeUpdates(listenerGps);
 
    super.onDestroy();
  }

  
  private void showDetails() {
    
    if (geoContactId <= 0) {
      return;
    }
    contactSaver = new GeoContactSaver(this);
    
    final Cursor contactCursor = contactSaver.loadGeoContactDetails(geoContactId);        
    if (!contactCursor.moveToFirst()) {
      return;
    }

    final TextView fldName = (TextView)findViewById(R.id.txt_name);
    fldName.setText(contactCursor.getString(contactCursor.getColumnIndex(GeoContactTbl.NAME)));

    final TextView fldCellnumber = (TextView)findViewById(R.id.txt_telephone);
    receiverCellnumbers.add(contactCursor.getString(contactCursor.getColumnIndex(GeoContactTbl.CELLNUMBER)));
    fldCellnumber.setText(receiverCellnumbers.get(0));

    final TextView fldNote = (TextView)findViewById(R.id.txt_note);
    fldNote.setText("Find me");
    
    final double latitude = contactCursor.getDouble(contactCursor.getColumnIndex(GeoContactTbl.LATITUDE));
    final double longitude = contactCursor.getDouble(contactCursor.getColumnIndex(GeoContactTbl.LONGITUDE));
    final TextView fldPosition = (TextView) findViewById(R.id.txt_last_position);
    if (latitude > 0 && longitude > 0) {
      fldPosition.setText(MessageFormat.format("{0}'' Longitude, {1}'' Latitude", longitude, 
          latitude));
    } else {
      fldPosition.setText("");
    }
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (resultCode) {
      case Activity.RESULT_OK:
        geoContactId = data.getExtras().getLong(GeoContactList.IN_PARAM_CONTACT_ID);
        break;
      case Activity.RESULT_CANCELED:
        finish();
        break;
      default:
        break;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
  
  
  public void onClickSendPosition(final View view) {
	    final EditText fldNote = (EditText)findViewById(R.id.txt_note);
	    note = fldNote.getText().toString();
	    
	    final GeoMarking geoMarking = new GeoMarking(note, ownGps);

	    FriendFinderSmsUtil.sendSms(receiverCellnumbers, geoMarking, FriendFinderSmsUtil.TYP_TEXT_SMS);
	    
	    //after send return to main
	    final Intent i = new Intent(view.getContext(), Main.class);
        startActivity(i);

	  }

}
