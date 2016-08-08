package com.hyunnyapp.brainylivingroom.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.hyunnyapp.brainylivingroom.R;
import com.hyunnyapp.brainylivingroom.singleton.BrainyLivingroom;
import com.hyunnyapp.brainylivingroom.weathergatter.WeatherInfo;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeather;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeatherExceptionListener;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeatherInfoListener;

public class WeatherSettingDialog extends Activity {
    public static final String TAG = WeatherSettingDialog.class.getSimpleName();

    public final String COMMAND_UPDATE_WEATHER = "com.hyunnyapp.brainylivingroom.COMMAND_UPDATE_WEATHER";

    private CheckBox mCheckBoxUseGps;
    private TextView mTextViewCity;
    private EditText mEditTextCity;
    private Button mButtonSearch;
    private Button mButtonConfirm;
    private Button mButtonCancel;
    private TextView mTextViewResult;

    private boolean mUseGPS;

    private YahooWeather mYahooWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_weather_setting);

        mCheckBoxUseGps = (CheckBox) findViewById(R.id.checkBox_use_gps);
        mTextViewCity = (TextView) findViewById(R.id.textView_city);
        mEditTextCity = (EditText) findViewById(R.id.editText_city);
        mButtonSearch = (Button) findViewById(R.id.button_search);
        mButtonConfirm = (Button) findViewById(R.id.button_confirm);
        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mTextViewResult = (TextView) findViewById(R.id.textView_result);

        mUseGPS = BrainyLivingroom.getInstance().getUseGPS(this);

        if(mUseGPS == true) {
            mCheckBoxUseGps.setChecked(true);
            mTextViewCity.setVisibility(View.INVISIBLE);
            mEditTextCity.setVisibility(View.INVISIBLE);
            mButtonSearch.setVisibility(View.INVISIBLE);
        }
        else {
            mCheckBoxUseGps.setChecked(false);
            mTextViewCity.setVisibility(View.VISIBLE);
            mEditTextCity.setVisibility(View.VISIBLE);
            mButtonSearch.setVisibility(View.VISIBLE);
        }

        mCheckBoxUseGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    mUseGPS = true;
                    mTextViewCity.setVisibility(View.INVISIBLE);
                    mEditTextCity.setVisibility(View.INVISIBLE);
                    mButtonSearch.setVisibility(View.INVISIBLE);
                }
                else {
                    mUseGPS = false;
                    mTextViewCity.setVisibility(View.VISIBLE);
                    mEditTextCity.setVisibility(View.VISIBLE);
                    mButtonSearch.setVisibility(View.VISIBLE);
                }
                BrainyLivingroom.getInstance().setUseGPS(WeatherSettingDialog.this, mUseGPS);
                updateWeather();
            }
        });

        mEditTextCity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                return false;
            }
        });

        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                v.setFocusable(false);
                v.setFocusableInTouchMode(false);
                updateWeather();
            }
        });

        mButtonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BrainyLivingroom.getInstance().setMyCity(WeatherSettingDialog.this, mEditTextCity.getText().toString());
                finish();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update Weather
        setWeatherGatter();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterWeatherAlarmBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private AlarmManager mWeatherAlarmManager;
    private PendingIntent mPendingIntent;
    BroadcastReceiver mReceiver;

    private void registerWeatherAlarmBroadcast() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Alarm time has been reached");
                updateWeather();
            }
        };

        registerReceiver(mReceiver, new IntentFilter(COMMAND_UPDATE_WEATHER));
        mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(COMMAND_UPDATE_WEATHER), 0);
        mWeatherAlarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));

        mWeatherAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30 * 60 * 1000, mPendingIntent);
    }

    private void unregisterWeatherAlarmBroadcast() {
        mWeatherAlarmManager.cancel(mPendingIntent);
        getBaseContext().unregisterReceiver(mReceiver);
    }

    void updateWeather() {
        if(mUseGPS == true) {
            mTextViewResult.setText("");
            searchByGPS();
        }
        else {
            mTextViewResult.setText("");
            searchByPlaceName(mEditTextCity.getText().toString());
        }
    }

    private void setWeatherGatter() {
        mYahooWeather = YahooWeather.getInstance(5000, 5000, false);
        mYahooWeather.setExceptionListener(yahooWeatherExceptionListener);
        updateWeather();
        registerWeatherAlarmBroadcast();
    }

    private void searchByGPS() {
        Log.d(TAG,"searchByGPS");
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(this, yahooWeatherInfoListener);
    }

    private void searchByPlaceName(String location) {
        Log.d(TAG,"searchByPlaceName city: " + location);
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(this, location, yahooWeatherInfoListener);
    }

    YahooWeatherInfoListener yahooWeatherInfoListener = new YahooWeatherInfoListener() {
        @Override
        public void gotWeatherInfo(WeatherInfo weatherInfo) {
            if (weatherInfo != null) {
                String currentWeather = "====== CURRENT ======" + "\n"+
                        "City: " + weatherInfo.getLocationCity() + "\n" +
                        "date: " + weatherInfo.getCurrentConditionDate() + "\n" +
                        "weather: " + weatherInfo.getCurrentText() + "\n" +
                        "temperature in ºC: "+ weatherInfo.getCurrentTemp() +"ºC"+ "\n"+
                        "Humidity: " + weatherInfo.getAtmosphereHumidity() + "%";

                mTextViewResult.setText(currentWeather);

                Log.d(TAG,currentWeather);

            }
        }
    };

    YahooWeatherExceptionListener yahooWeatherExceptionListener = new YahooWeatherExceptionListener() {
        @Override
        public void onFailConnection(Exception e) {
            Log.d(TAG, "Fail Connection");
        }

        @Override
        public void onFailParsing(Exception e) {
            Log.d(TAG, "Fail Parsing");
        }

        @Override
        public void onFailFindLocation(Exception e) {
            Log.d(TAG, "Fail Find Location");
        }
    };

}