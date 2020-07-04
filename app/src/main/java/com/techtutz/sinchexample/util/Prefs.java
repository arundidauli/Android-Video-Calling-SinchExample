package com.techtutz.sinchexample.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.techtutz.sinchexample.R;


public class Prefs {

    private static Prefs prefs;
    private SharedPreferences sharedPreferences;

    public static Prefs getInstance(Context context) {
        if (prefs == null) {
            prefs = new Prefs(context);
        }
        return prefs;
    }


    private Prefs(Context context) {

        sharedPreferences = context.getSharedPreferences(String.valueOf(R.string.app_name), Context.MODE_PRIVATE);
    }

    public void SetValue(String key, String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putString(key, value);
        prefsEditor.apply();
    }

    public String GetValue(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, null);
        }
        return null;
    }

    public void ClearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        editor.commit();

    }


}
