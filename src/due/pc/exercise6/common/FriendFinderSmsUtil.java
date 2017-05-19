package due.pc.exercise6.common;

import java.util.List;

import android.telephony.SmsManager;

public final class FriendFinderSmsUtil {

  public static final int TYP_DATA_SMS = 1;
  public static final int TYP_TEXT_SMS = 2;
  public static final short SMS_DATA_PORT = 15873;
  public static final String FriendFinder_SMS_KEY = "friendFinderSmsKey";
  
  private FriendFinderSmsUtil() { }

  
  public static void sendSms(final List<String> receiverCellnumber, final GeoMarking geoMarking, int smsType) {
    final String smsText = generateSmsText(geoMarking);
    for (String cellnumber : receiverCellnumber) {
      switch (smsType) {
        case TYP_DATA_SMS:
          sendDataSms(cellnumber, smsText);
          break;
        case TYP_TEXT_SMS:
          sendTextSms(cellnumber, smsText);
          break;
        default:
          break;
      }
    }
  }

 
  private static void sendDataSms(final String cellnumber, final String smsText) {
    final SmsManager smsManager = SmsManager.getDefault();

    final byte[] udh = smsText.getBytes();
    smsManager.sendDataMessage(cellnumber, null, SMS_DATA_PORT, udh, null, null);
  }
  private static void sendTextSms(final String cellnumber, final String smsText) {
    final SmsManager smsManager = SmsManager.getDefault();

    smsManager.sendTextMessage(cellnumber, null, smsText, null, null);
  }
  

  private static String generateSmsText(final GeoMarking geoMarking) {
    final StringBuilder smsTextBuilder = new StringBuilder();
    smsTextBuilder.append(FriendFinder_SMS_KEY);
    smsTextBuilder.append("#");
    if (geoMarking.note != null) {
      smsTextBuilder.append(geoMarking.note.replaceAll("#", " "));
    }
    smsTextBuilder.append("#");
    smsTextBuilder.append(geoMarking.gpsData.longitude);
    smsTextBuilder.append("#");
    smsTextBuilder.append(geoMarking.gpsData.latitude);
    smsTextBuilder.append("#");
    smsTextBuilder.append(geoMarking.gpsData.altitude);
    smsTextBuilder.append("#");
    smsTextBuilder.append(geoMarking.gpsData.timestamp);

    return smsTextBuilder.toString();
  }

}
