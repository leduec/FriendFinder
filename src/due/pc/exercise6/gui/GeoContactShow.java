package due.pc.exercise6.gui;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import due.pc.exercise6.R;
import due.pc.exercise6.contact.GeoContactSaver;
import due.pc.exercise6.contact.db.GeoContactTbl;


public class GeoContactShow extends Activity {

  static final String IN_PARAM_CONTACT_ID = "CONTACT_ID";

  private GeoContactSaver contactSaver;

  private long geoContactId;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    setContentView(R.layout.geocontact_show);
    final Bundle extras = getIntent().getExtras();
    if (extras != null && extras.containsKey(IN_PARAM_CONTACT_ID)) {
      geoContactId = extras.getLong(IN_PARAM_CONTACT_ID);
    }
    contactSaver = new GeoContactSaver(this);
  }

  @Override
  protected void onStart() {
    contactSaver.open();
    
    showDetails();

    super.onStart();
  }

  
  @Override
  protected void onDestroy() {
    contactSaver.close();
    super.onDestroy();
  }

  
  private void showDetails() {
    if (geoContactId == 0) {
      return;
    }
    final Cursor contactCursor = contactSaver.loadGeoContactDetails(geoContactId);
    if (!contactCursor.moveToFirst()) {
      return;
    }

    startManagingCursor(contactCursor);

    final TextView fldName = (TextView)findViewById(R.id.txt_name);
    fldName.setText(contactCursor.getString(contactCursor.getColumnIndex(GeoContactTbl.NAME)));

    final TextView fldTelephone = (TextView)findViewById(R.id.txt_telephone);
    fldTelephone.setText(contactCursor.getString(contactCursor.getColumnIndex(GeoContactTbl.CELLNUMBER)));

    final TextView fldNote = (TextView)findViewById(R.id.txt_note);
    fldNote.setText(contactCursor.getString(contactCursor.getColumnIndex(GeoContactTbl.NOTE)));

    final long timestamp = contactCursor.getLong(contactCursor.getColumnIndex(GeoContactTbl.TIMESTAMP));
    final TextView fldDate = (TextView)findViewById(R.id.txt_date);
    if (timestamp > 0) {
      fldDate.setText(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(new Date(timestamp))); 
    } else {
      fldDate.setText("unknown");
    }
    final double latitude = contactCursor.getDouble(contactCursor.getColumnIndex(GeoContactTbl.LATITUDE));
    final double longitude = contactCursor.getDouble(contactCursor.getColumnIndex(GeoContactTbl.LONGITUDE));
    final TextView fldPosition = (TextView) findViewById(R.id.txt_last_position);
    if (latitude > 0 && longitude > 0) {
      fldPosition.setText(MessageFormat.format("{0}'' Longitude, {1}'' Latitude", longitude, latitude));
    } else {
      fldPosition.setText("");
    }
  }

  //Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.geocontact_show, menu);

    return true;
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_geocontactEdit:
        final Intent i = new Intent(this, GeoContactEdit.class);
        i.putExtra(GeoContactEdit.IN_PARAM_CONTACT_ID, geoContactId);
        startActivity(i);
        return true;
      case R.id.menu_showHelp:
        return true;

      default:
    }
    return super.onOptionsItemSelected(item);
  }

}
