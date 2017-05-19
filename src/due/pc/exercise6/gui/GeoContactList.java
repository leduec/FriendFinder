package due.pc.exercise6.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import due.pc.exercise6.R;
import due.pc.exercise6.contact.GeoContact;
import due.pc.exercise6.contact.GeoContactSaver;
import due.pc.exercise6.contact.db.GeoContactTbl;


public class GeoContactList extends ListActivity {

  static final String IN_PARAM_CONTACT_ID = "CONTACT_ID";

  static final String SELECT_CONTACT = "SELECT_CONTACT";
  
  
  public static final int CONTACT_CHOOSEN = 1;
  
  private static final String[] SHOW_CONTACT = new String[] {GeoContactTbl.NAME, GeoContactTbl.NOTE};

  private static final int[] SIMPLE_LIST_VIEW_IDS = new int[] {android.R.id.text1, android.R.id.text2};

  private boolean selectionMode;
  
  private GeoContactSaver contactSaver;
  
  
  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    
    setContentView(R.layout.geocontact_list);    
    
    final Bundle extras = getIntent().getExtras();
    if (extras != null && extras.containsKey(SELECT_CONTACT)) {
      selectionMode = true;
      setTitle(R.string.txt_geocontact_list_chooseContact);
    } else {
      setTitle(R.string.txt_geocontact_list_title);     
    }

    contactSaver = new GeoContactSaver(this);
  }
  
  @Override
  protected void onStart() {
    showGeoContact();
    super.onStart();    
  }
  
  @Override
  protected void onDestroy() {
    contactSaver.close();
    super.onDestroy();
  }

  private void showGeoContact() {
    final Cursor contactCursor = contactSaver.loadGeoContactList();
    startManagingCursor(contactCursor);

    final SimpleCursorAdapter contactAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, contactCursor, SHOW_CONTACT, SIMPLE_LIST_VIEW_IDS);
    setListAdapter(contactAdapter);
  }

  //Menu
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.geocontact_list, menu);
    return super.onCreateOptionsMenu(menu);
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_geocontactNew: 
        final GeoContact newContact = new GeoContact();
        newContact.setStartValues();
        final Intent i = new Intent(this, GeoContactEdit.class);    
        startActivity(i);
        return true;
      
      case R.id.menu_geocontactImport:
        final Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_CHOOSEN);        
        return true;
            
      case R.id.menu_showHelp:       
        return true;
        
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
    
    if (!selectionMode) { 
      final Intent i = new Intent(this, GeoContactShow.class);
      i.putExtra(GeoContactShow.IN_PARAM_CONTACT_ID, id);
      startActivity(i);
    } else {
      final Intent intent = new Intent();
      intent.putExtra(IN_PARAM_CONTACT_ID, id);
      setResult(Activity.RESULT_OK, intent);
      finish();
    }
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CONTACT_CHOOSEN) {
      if (resultCode == RESULT_OK) {  
        importChoosenContact(data.getData());
        return;
      }
    }  
  }

  private void importChoosenContact(final Uri contactData) {
    final Cursor cursor = managedQuery(contactData, null, null, null, null);

    if (cursor.moveToFirst()) {
      final GeoContact geoContact = new GeoContact();
      final String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)); 
      geoContact.lookupKey = lookupKey;
      final String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); 
      geoContact.name = displayName; 
      
      final String hasCellNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); 
      if ("1".equals(hasCellNumber)) {   
        final String kontaktId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)); 
        final String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + kontaktId + 
        		" and " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        final Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, whereClause, null, null);
        //more number => choose first
        if (phones.moveToNext()) {              
          final String cellNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          geoContact.cellnumber = cellNumber;
        } else {
          noCellnumberNote();
          return;
        }
        phones.close(); 
        
        geoContact.setStartValues();
        contactSaver.saveGeoContact(geoContact);
      }          
    } // end if (cursor.moveToFirst())
    cursor.close();
  }
 
  private void noCellnumberNote() {
    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(
        "Cannot choose this Contact - no cellnumber was founded!")
           .setCancelable(false)
           .setPositiveButton("OK", 
               new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                 dialog.dismiss();
               }
           });
    builder.create().show();
  }
  
}
