package com.hyunnyapp.brainylivingroom.singleton;

import android.content.Context;
import android.content.SharedPreferences;

public class BrainyLivingroom {
    private boolean mUseGPS = true;
    private String mMyCity = "null";

    private final String mDeviceName = "BrainyL";
    private final String SHAREAD_DATA_NAME = "sharedData";
    private final String SHAREAD_DATA_USE_GPS= "UseGPS";
    private final String SHAREAD_DATA_SEARCH_CITY= "SearchCity";

    public void setMyCity(Context context, String myCity) {
        mMyCity = myCity;

        SharedPreferences pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SHAREAD_DATA_SEARCH_CITY, myCity);
        editor.commit();
    }

    public String getMyCity(Context context) {
        SharedPreferences pref;

        if(mMyCity.equals("null"))
        {
            pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
            mMyCity = pref.getString(SHAREAD_DATA_SEARCH_CITY, null);
        }
        return mMyCity;
    }

    public void setUseGPS(Context context, boolean useGPS) {
        mUseGPS = useGPS;

        SharedPreferences pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(SHAREAD_DATA_USE_GPS, useGPS);
        editor.commit();
    }


    public boolean getUseGPS(Context context)
    {
        SharedPreferences pref;

        pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
        mUseGPS = pref.getBoolean(SHAREAD_DATA_USE_GPS, true);

        return mUseGPS;
    }

    public volatile static BrainyLivingroom instance = null;
    public static BrainyLivingroom getInstance() {
        if (instance == null) {
            synchronized (BrainyLivingroom.class) {
                if (instance == null) {
                    instance = new BrainyLivingroom();
                }
            }
        }
        return instance;
    }

}
