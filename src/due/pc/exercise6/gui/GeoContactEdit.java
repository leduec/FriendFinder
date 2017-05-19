package due.pc.exercise6.gui;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import due.pc.exercise6.R;
import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.contact.GeoContact;
import due.pc.exercise6.contact.GeoContactSaver;


public class GeoContactEdit extends Activity {
  static final String IN_PARAM_CONTACT_ID = "CONTACT_ID";

  private GeoContactSaver contactSaver;

  private long geoContactId;
  private GeoContact geoContact;

  @Override
  protected void onCreate(final Bundle icicle) {
    super.onCreate(icicle);

    setContentView(R.layout.geocontact_edit);
    
    final Bundle extras = getIntent().getExtras();
    if (extras != null && extras.containsKey(IN_PARAM_CONTACT_ID)) {
      geoContactId = extras.getLong(IN_PARAM_CONTACT_ID);
    }

    contactSaver = new GeoContactSaver(this);
  }

  @Override
  protected void onStart() {

    if (geoContactId != 0) {
      loadContact();
    } else {
      geoContact = new GeoContact();
      geoContact.setStartValues();
    }

    showDetails();

    super.onStart();
  }

  @Override
  protected void onDestroy() {
    contactSaver.close();
    super.onDestroy();
  }

  
  @Override
  protected void onPause() {
    super.onPause();
    final SharedPreferences pref = getPreferences(MODE_PRIVATE);
    final Editor editor = pref.edit();
    if (geoContactId != 0) {
      editor.putLong(IN_PARAM_CONTACT_ID, geoContactId);
    }
    editor.putString("" + R.id.txt_name_edit, "" + ((EditText) findViewById(R.id.txt_name_edit)).getText());
    editor.putString("" + R.id.txt_telephone_edit, "" + ((EditText) findViewById(R.id.txt_telephone_edit)).getText());
    editor.commit();
  }

  private void loadContact() {
    geoContact = contactSaver.loadGeoContact(geoContactId);
  }

  
  private void showDetails() {
	final EditText fldName = (EditText)findViewById(R.id.txt_name_edit);
    fldName.setText(geoContact.name);

    final EditText fldTelephone = (EditText)findViewById(R.id.txt_telephone_edit);
    fldTelephone.setText(geoContact.cellnumber);

    final TextView fldNote = (TextView)findViewById(R.id.txt_note);
    final GeoMarking mark = geoContact.lastPosition;
    fldNote.setText(mark.note);

    final long timestamp = mark.gpsData.timestamp;
    final TextView fldDate = (TextView)findViewById(R.id.txt_date);
    if (timestamp > 0) {
      fldDate.setText(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(new Date(timestamp)));
    } else {
      fldDate.setText("unknown");
    }
    final double latitude = mark.gpsData.latitude;
    final double longitude = mark.gpsData.longitude;
    final TextView fldPosition = (TextView)findViewById(R.id.txt_last_position);
    if (latitude > 0 && longitude > 0) {
      fldPosition.setText(MessageFormat.format("{0}'' Longitude, {1}'' Latitude", longitude, latitude));
    } else {
      fldPosition.setText("");
    }

  }

 
  private void saveContact() {

    final EditText fldName = (EditText) findViewById(R.id.txt_name_edit);
    geoContact.name = fldName.getText().toString();

    final EditText fldTelephone = (EditText) findViewById(R.id.txt_telephone_edit);
    geoContact.cellnumber = fldTelephone.getText().toString();

    contactSaver.saveGeoContact(geoContact);
  }

  //Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.geocontact_edit, menu);

    return super.onCreateOptionsMenu(menu);
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_save:

        saveContact();
        finish();
        return true;

      case R.id.menu_showHelp:
        return true;

      default:
        
    }
    return super.onOptionsItemSelected(item);
  }

  
  public void setContactId(final long contactId) {
    geoContactId = contactId;
  }
  
}
