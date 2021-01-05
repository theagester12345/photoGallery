package com.example.photogallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class QueryPreferences {

    private static final String PREF_QUERY = "searchQuery";

    public static String getStoredQuery (Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_QUERY,null);
    }

    public static void storeQuery (Context context, String query){

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_QUERY,query)
                .apply();
    }
}
