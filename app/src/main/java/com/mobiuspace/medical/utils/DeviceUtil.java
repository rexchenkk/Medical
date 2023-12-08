package com.mobiuspace.medical.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.UUID;

public class DeviceUtil {


  public static String getUdid(Context context) {
    String imei = getImei(context);
    if (TextUtils.isEmpty(imei)) {
      imei = getImsi(context);
    }
    if (TextUtils.isEmpty(imei)) {
      imei = getUniquePsuedoID();
    }
    if (TextUtils.isEmpty(imei)) {
      imei = getAndroidId(context);
    }
    return imei;
  }

  public static String getUniquePsuedoID() {
    try {
      String m_szDevIDShort = "35" +
              Build.BOARD.length()%10 +
              Build.BRAND.length()%10 +
              Build.CPU_ABI.length()%10 +
              Build.DEVICE.length()%10 +
              Build.DISPLAY.length()%10 +
              Build.HOST.length()%10 +
              Build.ID.length()%10 +
              Build.MANUFACTURER.length()%10 +
              Build.MODEL.length()%10 +
              Build.PRODUCT.length()%10 +
              Build.TAGS.length()%10 +
              Build.TYPE.length()%10 +
              Build.USER.length()%10 ; //13 digits
      return new UUID(m_szDevIDShort.hashCode(), Build.SERIAL.hashCode()).toString();
    } catch (Exception e) {
      return "";
    }
  }

  public static String getImei(Context context) {
    try {
      TelephonyManager telephonyManager = (TelephonyManager) context
              .getSystemService(Context.TELEPHONY_SERVICE);
      return telephonyManager.getDeviceId();
    } catch (Throwable e) {
      // In some devices, we are not able to get device id, and may cause some exception,
      // so catch it.
      return "";
    }
  }

  /**
   * 获取手机IMSI
   */
  public static String getImsi(Context context) {
    try {
      TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
              .TELEPHONY_SERVICE);
      return telephonyManager.getSubscriberId();
    } catch (Throwable e) {
      e.printStackTrace();
      return "";
    }
  }

  public static String getAndroidId(Context context) {
    try {
      return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    } catch (Exception e) {
      return "";
    }
  }

}
