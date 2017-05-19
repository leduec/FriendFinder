package due.pc.exercise6.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import due.pc.exercise6.R;
import due.pc.exercise6.common.FriendFinderSmsUtil;
import due.pc.exercise6.common.GeoMarking;
import due.pc.exercise6.common.GpsData;
import due.pc.exercise6.contact.GeoContact;
import due.pc.exercise6.gui.Main;


public class SmsBroadcastReceiver 
    extends BroadcastReceiver {

  public static final String KEY_CLIENT_INFO = "clientInfo";
  public static final String KEY_NOTIFICATION_NR = "notificationNr";
  public static final int NOTIFICATION_ID = 12345;
  static final String DATA_SMS_ACTION = "android.intent.action.DATA_SMS_RECEIVED";

  public static final short DATA_SMS_PORT = 15873;


  @Override
  public final void onReceive(final Context context, final Intent intent) {
	  
    final Bundle bundle = intent.getExtras();
    if (bundle != null) {
      if (intent.getAction().equals(DATA_SMS_ACTION)) {
        useDataSms(context, intent);
      } else {
    	  useTextSms(context, intent);
      }
    } 
    
  }
    
  
  private void useDataSms(final Context context, final Intent intent) {
    final String uricontent = intent.getDataString();
    final String[] str = uricontent.split(":");
    final String strPort = str[str.length - 1];
    final int port = Integer.parseInt(strPort);

    if (port == DATA_SMS_PORT) {
      final Bundle bundle = intent.getExtras();
      if (bundle != null) {
        // PDU: Protocol Description Unit
        final Object[] pdUnitsAsObjects = (Object[]) bundle.get("pdus");
        for (Object pduAsObject : pdUnitsAsObjects) {
          final SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pduAsObject);
          if (smsMessage != null) {
            final String smsText = new String(smsMessage.getUserData());
            if (smsText != null && smsText.startsWith(FriendFinderSmsUtil.FriendFinder_SMS_KEY)) {
              final String senderCellnumber = smsMessage.getOriginatingAddress();
              sendNotification(context, senderCellnumber, smsText);
            }
          }
        } // ende for
      }
    }
  }

  private void useTextSms(final Context context, final Intent intent) {
    final Bundle bundle = intent.getExtras();
    if (bundle != null) {
      // PDU: Protocol Description Unit
      final Object[] pdUnitsAsObjects = (Object[]) bundle.get("pdus");
      for (Object pduAsObject : pdUnitsAsObjects) {
        final SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pduAsObject);
        if (smsMessage != null) {
          final String smsText = new String(smsMessage.getMessageBody());
          if (smsText != null && smsText.startsWith(FriendFinderSmsUtil.FriendFinder_SMS_KEY)) {
            final String senderCellnumber = smsMessage.getOriginatingAddress();
            sendNotification(context, senderCellnumber, smsText);
          }
        } // ende if
      } // ende for
    }
  }


  public static void sendNotification(final Context context, final String senderCellnumber, final String smsText) {
    final String[] token = smsText.split("#");
    final String note = token[1];

    final GpsData gpsData = new GpsData(Double.parseDouble(token[2]), Double.parseDouble(token[3]), Double.parseDouble(token[4]), Long.parseLong(token[5]));
   
    final StringBuffer text = new StringBuffer();
    text.append("Sender: ");
    text.append(senderCellnumber);

    final Notification notification = new Notification(R.drawable.notification_icon, text.toString(), System.currentTimeMillis());

    final Intent activityIntent = new Intent(context, Main.class);

    final Bundle bundle = new Bundle();
    bundle.putString(GeoContact.KEY_CELLNUMBER, senderCellnumber);
    bundle.putString(GeoMarking.KEY_NOTE, note);
    bundle.putDouble(GpsData.KEY_LONGITUDE, gpsData.longitude);
    bundle.putDouble(GpsData.KEY_LATITUDE, gpsData.latitude);
    bundle.putDouble(GpsData.KEY_ALTITUDE, gpsData.altitude);
    bundle.putLong(GpsData.KEY_TIMESTAMP, gpsData.timestamp);

    activityIntent.putExtra(KEY_CLIENT_INFO, bundle);
    activityIntent.putExtra(KEY_NOTIFICATION_NR, NOTIFICATION_ID);

    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, activityIntent, 0);
    
    notification.setLatestEventInfo(context, "Received new Position!", text.toString(), contentIntent);

    // Vibration:
    notification.defaults |= Notification.DEFAULT_VIBRATE;
    notification.vibrate = new long[] { 100, 250 };

    // LED:
    notification.defaults |= Notification.DEFAULT_LIGHTS;
    notification.ledARGB = 0xff00ff00;
    notification.ledOnMS = 300;
    notification.ledOffMS = 1000;
    notification.flags |= Notification.FLAG_SHOW_LIGHTS;

    final NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(NOTIFICATION_ID, notification);

    
    context.startActivity(activityIntent);
  }

}
