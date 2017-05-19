package due.pc.exercise6.gui;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import due.pc.exercise6.R;
import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.common.GpsData;
import due.pc.exercise6.contact.GeoContact;
import due.pc.exercise6.contact.GeoContactSaver;
import due.pc.exercise6.receiver.SmsBroadcastReceiver;


public class Main extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.main);                

    interpretIntent(this.getIntent());
  }
  private void interpretIntent(final Intent intent) {

    final int notificationNr = intent.getIntExtra(SmsBroadcastReceiver.KEY_NOTIFICATION_NR, 0);

    if (notificationNr != 0) {
      final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      nm.cancel(notificationNr);
    }

    final Bundle extras = intent.getBundleExtra(SmsBroadcastReceiver.KEY_CLIENT_INFO);
    if (extras != null) {
      final GpsData gpsData = new GpsData(
          extras.getDouble(GpsData.KEY_LONGITUDE),
          extras.getDouble(GpsData.KEY_LATITUDE),
          extras.getDouble(GpsData.KEY_ALTITUDE),
          extras.getLong(GpsData.KEY_TIMESTAMP));

      final String cellnumber = extras.getString(GeoContact.KEY_CELLNUMBER);
      
      final GeoMarking geoMarking = new GeoMarking(extras.getString(GeoMarking.KEY_NOTE), gpsData);    

      GeoContactSaver contactSaver = new GeoContactSaver(this);
      final Cursor contactCursor = contactSaver.loadGeoContactDetails(cellnumber);
      long geoContactId = 0;
      GeoContact geoContact = null;
      if (contactCursor.moveToFirst()) {
        geoContact = contactSaver.loadGeoContact(contactCursor);
        geoContact.lastPosition = geoMarking;
        geoContactId = contactSaver.saveGeoContact(geoContact);
      } else {
        geoContact = new GeoContact();
        geoContact.name = cellnumber;
        geoContact.cellnumber = cellnumber;
        geoContact.lastPosition = geoMarking;
        geoContactId = contactSaver.saveGeoContact(geoContact);
      }
      contactSaver.close();

      final Intent activityIntent = new Intent(this, ShowMap.class);
      activityIntent.putExtra(ShowMap.IN_PARAM_CONTACT_ID, geoContactId);
      startActivity(activityIntent);
    }
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  //Buttons
  public void onClickSendPosition(final View v) {
    final Intent i = new Intent(this, SendPosition.class);
    startActivity(i);
  }
  public void onClickGeoContactList(final View v) {
    final Intent i = new Intent(this, GeoContactList.class);
    startActivity(i);       
  }
  public void onClickShowMap(final View v) {
    final Intent i = new Intent(this, ShowMap.class);
    startActivity(i);
  }

  //Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    
    getMenuInflater().inflate(R.menu.main, menu);    
    
    return true;
  }
    
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_editSettings:
        return true;    
      case R.id.menu_showHelp:
        return true;    
      case R.id.menu_friendFinderExit: 
        finish();
        return true;      
      default:
        return super.onOptionsItemSelected(item);        
    }    
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
  
}