package com.bike.security.bd;

import android.content.Context;
import android.content.SharedPreferences;

public class Utility {

    private static SharedPreferences sharedPreferences;

    private static synchronized SharedPreferences getSharedPreferenceInstance(Context context) {
        if (sharedPreferences==null){
            sharedPreferences=context.getSharedPreferences(Constants.sharedPreferenceName,Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }


    public static void setStringToStorage(Context context, String key, String value) {
        SharedPreferences.Editor editor=getSharedPreferenceInstance(context).edit();
        editor.putString(key,value);
        editor.apply();
    }

    public static String getStringFromStorage(Context context, String key, String defaultValue) {
        return getSharedPreferenceInstance(context).getString(key,defaultValue);
    }

    public static void setIntToStorage(Context context, String key, int value) {
        SharedPreferences.Editor editor=getSharedPreferenceInstance(context).edit();
        editor.putInt(key,value);
        editor.apply();
    }

    public static int getIntFromStorage(Context context, String key, int defaultValue) {
        return getSharedPreferenceInstance(context).getInt(key,defaultValue);
    }

    public static void setBooleanToStorage(Context context, String key, boolean value) {
        SharedPreferences.Editor editor=getSharedPreferenceInstance(context).edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static boolean getBooleanFromStorage(Context context, String key, boolean defaultValue) {
        return getSharedPreferenceInstance(context).getBoolean(key,defaultValue);
    }
}
