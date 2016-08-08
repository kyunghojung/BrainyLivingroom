package com.hyunnyapp.brainylivingroom.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunnyapp.brainylivingroom.R;
import com.hyunnyapp.brainylivingroom.ble.BleConnectionManager;
import com.hyunnyapp.brainylivingroom.ble.BleConnectionManagerDelegate;
import com.hyunnyapp.brainylivingroom.ble.BleMultiConnector;

import com.hyunnyapp.brainylivingroom.singleton.BrainyLivingroom;
import com.hyunnyapp.brainylivingroom.weathergatter.WeatherInfo;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeatherExceptionListener;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeather;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeather.SEARCH_MODE;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeather.UNIT;
import com.hyunnyapp.brainylivingroom.weathergatter.YahooWeatherInfoListener;

import com.hyunnyapp.brainylivingroom.calendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class DashboardActivity extends Activity implements BleConnectionManagerDelegate {
    public static final String TAG = DashboardActivity.class.getSimpleName();

    public final static String DEVICE_NAME = "BrainyL";

    public final String COMMAND_CONNECT_DEVICE = "com.hyunnyapp.brainylivingroom.COMMAND_CONNECT_DEVICE";
    public final String COMMAND_CONNECTED_DEVICE = "com.hyunnyapp.brainylivingroom.COMMAND_CONNECTED_DEVICE";
    public final String COMMAND_DISCONNECT_DEVICE = "com.hyunnyapp.brainylivingroom.COMMAND_DISCONNECT_DEVICE";
    public final String COMMAND_DISCONNECTED_DEVICE = "com.hyunnyapp.brainylivingroom.COMMAND_DISCONNECTED_DEVICE";
    public final String COMMAND_RECEIVE_DATA = "com.hyunnyapp.brainylivingroom.COMMAND_RECEIVE_DATA";
    public final String COMMAND_SEND_DATA = "com.hyunnyapp.brainylivingroom.COMMAND_SEND_DATA";

    public final String COMMAND_UPDATE_WEATHER = "com.hyunnyapp.brainylivingroom.COMMAND_UPDATE_WEATHER";

    private final int REQUEST_ENABLE_BT = 1;

    private final int UART_PROFILE_CONNECTED = 20;
    private final int UART_PROFILE_DISCONNECTED = 21;

    private final long SCAN_PERIOD = 10000; //10 seconds
    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mConnectingDevice = null;
    private BluetoothDevice mWaitToConnectDevice = null;
    private HashMap<String, Integer> devRssiValues = new HashMap<String, Integer>();
    private int mState = UART_PROFILE_DISCONNECTED;
    private boolean isConnectingDevice = false;

    private BleConnectionManager mBleConnectionManager = null;
    private static BlockingQueue<ArrayList<Object>> mCommendQueue;
    private ArrayList<Object> mCommendArrayList;

    private Handler mHandler = new Handler();
    private Handler mScanHandler = new Handler();


    /************************************
     *  Bluetooth
     *************************************/
    private boolean initBle() {
        if (mBluetoothAdapter == null) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }

            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void startScanLeDevice(final boolean enable) {
        Log.d(TAG, "scanLeDevice() " + enable);

        this.setProgressBarIndeterminateVisibility(true);
        if (enable) {
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanAndConnectDevice();
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void stopScanAndConnectDevice() {
        this.setProgressBarIndeterminateVisibility(false);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDevices(mDeviceList);
            }
        }, 2000);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device, rssi);
                }
            });
        }
    };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : mDeviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        if (!deviceFound) {
            devRssiValues.put(device.getAddress(), rssi);
            Log.d(TAG, "Device Found! " + device.getName() + ", rssi: " + rssi);
            mDeviceList.add(device);
        }
    }


    Thread mCommendThread = new Thread() {
        public void run() {
            while(true) {
                try {
                    mCommendArrayList = mCommendQueue.take();
                    Log.d(TAG,"take commend commend: "+mCommendArrayList.get(0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    eventProcess(mCommendArrayList);
                }
            }
        }
    };

    private void eventProcess(ArrayList<Object> receivedArrayList) {
        String commend = (String) receivedArrayList.get(0);
        BluetoothDevice device = (BluetoothDevice) receivedArrayList.get(1);
        String data;

        Log.d(TAG,"eventProcess: "+commend+", device: "+device.getName());
        switch (commend) {
            case COMMAND_CONNECT_DEVICE:
                Log.d(TAG, "COMMAND_CONNECT_DEVICE: device: " + device.getName());
                if(isConnectingDevice == true) {
                    Log.d(TAG, "isConnectingDevice add again " + device.getName());
                    mWaitToConnectDevice = device;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addCommand(COMMAND_CONNECT_DEVICE, mWaitToConnectDevice, null);
                            mWaitToConnectDevice = null;
                        }
                    }, 5000);
                }
                else {
                    isConnectingDevice = true;
                    mConnectingDevice = device;
                    mBleConnectionManager.connect(mConnectingDevice);
                }
                break;
            case COMMAND_DISCONNECT_DEVICE:
                Log.d(TAG, "COMMAND_DISCONNECT_DEVICE ");
                break;
            case COMMAND_RECEIVE_DATA:
                Log.d(TAG, "COMMAND_RECEIVE_DATA ");
                data = (String) receivedArrayList.get(2);
                Log.d(TAG, "received data: " + data);

                processMessage(device, data);
                break;
            case COMMAND_SEND_DATA:
                Log.d(TAG, "COMMAND_SEND_DATA ");
                data = (String) receivedArrayList.get(1);
                mBleConnectionManager.sendData(device, data);
                Log.d(TAG, "send data: " + data);
                break;

            default:
                break;
        }
    }

    public static void addCommand(String command, Object object1, Object object2){
        ArrayList<Object> commandArrayList = new ArrayList<Object>();
        commandArrayList.add(command);
        if(object1 != null) {
            commandArrayList.add(object1);
        }
        if(object2 != null) {
            commandArrayList.add(object2);
        }

        try {
            mCommendQueue.put(commandArrayList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Connected " + device.getName());
        Log.d(TAG, "isConnectingDevice " + isConnectingDevice);
        if(isConnectingDevice == true && mConnectingDevice.getAddress().equals(device.getAddress())) {
            mConnectingDevice = null;
            isConnectingDevice = false;
        }

        processMessage(device, COMMAND_CONNECTED_DEVICE);
    }

    @Override
    public void disconnected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Disonnected " + device.getName());
        processMessage(device, COMMAND_DISCONNECTED_DEVICE);
    }

    @Override
    public void failToConnect(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Fail To Connect!  " + device.getName());

    }

    @Override
    public void receivedData(BleConnectionManager manager, BluetoothDevice device, String data) {
        addCommand(COMMAND_RECEIVE_DATA, device, data);
    }

    public void sendData(BluetoothDevice device, String data) {
        if(data != null && data.length() != 0 ) {
            addCommand(COMMAND_SEND_DATA, device, data);
        }
    }

    public void connectDevices(ArrayList<BluetoothDevice> deviceList) {
        Log.d(TAG, "connectDevices: " + deviceList.size());
        for(BluetoothDevice device:deviceList) {
            Log.d(TAG,"COMMAND_CONNECT_DEVICE "+device.getName());
            addCommand(COMMAND_CONNECT_DEVICE, device, null);
        }
    }

    private void processMessage(BluetoothDevice device, String message) {
        switch(device.getName()) {
            case DEVICE_NAME:
                if(message.equals(COMMAND_CONNECTED_DEVICE) || message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                    if (message.equals(COMMAND_CONNECTED_DEVICE)) {
                        displayBTStatus(true);
                        addCommand(COMMAND_SEND_DATA, device, "UV");   // sendData "Update Values"
                    } else if (message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                        displayBTStatus(false);
                    }
                }
                else{
                    if (message.startsWith("L")) {
                      /*
                      // lux
                      0.0001 lux	Moonless, overcast night sky (starlight)[3]
                      0.002 lux	Moonless clear night sky with airglow[3]
                      0.27 - 1.0 lux	Full moon on a clear night[3][4]
                      3.4 lux	Dark limit of civil twilight under a clear sky[5]
                      50 lux	Family living room lights (Australia, 1998)[6]
                      80 lux	Office building hallway/toilet lighting[7][8]
                      100 lux	Very dark overcast day[3]
                      320 - 500 lux	Office lighting[6][9][10][11]
                      400 lux	Sunrise or sunset on a clear day.
                      1000 lux	Overcast day;[3] typical TV studio lighting
                      10000 - 25000 lux	Full daylight (not direct sun)[3]
                      32000 - 100000 lux	Direct sunlight
                      */
                        String tempStr[] = message.split(":");
                        if(tempStr.length < 2) {
                            return;
                        }
                        displayBTValue(mTextViewLux, tempStr[1] + "lx");
                    } else if (message.startsWith("T")) {
                        String tempStr[] = message.split(":");
                        if(tempStr.length < 2) {
                            return;
                        }
                        displayBTValue(mTextViewTemp, tempStr[1] + getString(R.string.symbol_celsius));
                    } else if (message.startsWith("H")) {
                        String tempStr[] = message.split(":");
                        if(tempStr.length < 2) {
                            return;
                        }
                        displayBTValue(mTextViewHumi, tempStr[1] + "%");
                    }
                }
                break;
            default:
                break;
        }
    }

    /************************************
     *  Activity Life Cycle & UI
     *************************************/
    private TextView mTextViewLux;
    private TextView mTextViewTemp;
    private TextView mTextViewHumi;
    private TextView mTextViewClock;
    private TextView mTextViewDate;
    private TextView mTextViewWeatherTemp;
    private TextView mTextViewWeatherCity;
    private TextView mTextViewWeatherCondition;
    private TextView mTextView_weather_highlow;
    private TextView mTextViewWeatherHumi;
    private TextView mTextViewWeatherGeo;
    private ImageView mImageViewWeatherRefresh;
    private ImageView mImageViewWeatherIcon;
    private ImageView mImageViewWeatherSetting;

    private MaterialCalendarView mCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextViewLux = (TextView) findViewById(R.id.textView_lux);
        mTextViewTemp = (TextView) findViewById(R.id.textView_temp);
        mTextViewHumi = (TextView) findViewById(R.id.textView_humi);
        mTextViewClock = (TextView) findViewById(R.id.textView_clock);
        mTextViewDate = (TextView) findViewById(R.id.textView_date);
        mTextViewWeatherTemp = (TextView) findViewById(R.id.textView_weather_temp);
        mTextViewWeatherCity = (TextView) findViewById(R.id.textView_weather_city);
        mTextViewWeatherCondition = (TextView) findViewById(R.id.textView_weather_condition);
        mTextView_weather_highlow = (TextView) findViewById(R.id.textView_weather_highlow);
        mTextViewWeatherHumi = (TextView) findViewById(R.id.textView_weather_humi);
        mTextViewWeatherGeo = (TextView) findViewById(R.id.textView_weather_geo);
        mImageViewWeatherRefresh = (ImageView) findViewById(R.id.imageView_weather_refresh);
        mImageViewWeatherIcon = (ImageView) findViewById(R.id.imageView_weather_icon);
        mImageViewWeatherSetting = (ImageView) findViewById(R.id.imageView_weather_setting);

        mImageViewWeatherRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWeather();
            }
        });

        mImageViewWeatherSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, WeatherSettingDialog.class);
                startActivity(intent);
            }
        });

        mCalendarView = (MaterialCalendarView) findViewById(R.id.calendarView);
        //mCalendarView.setCurrentDate(new Date());
        mCalendarView.setSelectedDate(new Date());

        displayBTStatus(false);

        // Bluetooth init
        initBle();
        mBleConnectionManager = new BleConnectionManager(this);
        BleMultiConnector.getInstance().setBleConnectionManager(mBleConnectionManager);
        mBleConnectionManager.setDelegate(this);

        mScanHandler = new Handler();

        mCommendQueue = new ArrayBlockingQueue<>(1024);
        mCommendThread.start();

        startScanLeDevice(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Date, Clock update
        registerTimeService();

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
        mBleConnectionManager.close();
        unregisterTimeService();
    }

    void displayBTStatus(boolean status) {
        if(status == true) {
            mTextViewTemp.setBackgroundColor(getResources().getColor(R.color.tile_light_green));
            mTextViewHumi.setBackgroundColor(getResources().getColor(R.color.tile_light_green));
            mTextViewLux.setBackgroundColor(getResources().getColor(R.color.tile_light_green));
        }
        else {
            mTextViewTemp.setBackgroundColor(getResources().getColor(R.color.tile_pink));
            mTextViewHumi.setBackgroundColor(getResources().getColor(R.color.tile_pink));
            mTextViewLux.setBackgroundColor(getResources().getColor(R.color.tile_pink));
            mTextViewTemp.setText("");
            mTextViewHumi.setText("");
            mTextViewLux.setText("");
        }
    }

    void displayBTValue(final TextView textView, final String string) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(string);
            }
        });
    }


    // Update Date, Time, Calendar
    void registerTimeService() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        mIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        registerReceiver(mTimeChangedReceiver, mIntentFilter);

        updateTimeDate();
    }

    void unregisterTimeService() {
        unregisterReceiver(mTimeChangedReceiver);
    }

    private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeDate();
        }
    };

    void updateTimeDate() {
        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();
        mTextViewClock.setText(now.format("%H:%M"));
        mTextViewDate.setText(now.format("%Y %B %d, %A"));

        /*
        String currentTime = DateFormat.getTimeInstance().format(new Date());
        String currentDate = DateFormat.getDateInstance().format(new Date());

        mTextViewClock.setText(currentTime);
        mTextViewDate.setText(currentDate);
        */
    }




    // Update Weather
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

        mWeatherAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 30*60*1000 , mPendingIntent);
    }

    private void unregisterWeatherAlarmBroadcast() {
        mWeatherAlarmManager.cancel(mPendingIntent);
        getBaseContext().unregisterReceiver(mReceiver);
    }

    void updateWeather() {
        if(BrainyLivingroom.getInstance().getUseGPS(this) == true) {
            searchByGPS();
        }
        else {
            String myCity = BrainyLivingroom.getInstance().getMyCity(this);
            searchByPlaceName(myCity);
        }
    }

    private YahooWeather mYahooWeather;

    private void setWeatherGatter() {
        mYahooWeather = YahooWeather.getInstance(5000, 5000, false);
        mYahooWeather.setExceptionListener(yahooWeatherExceptionListener);
        registerWeatherAlarmBroadcast();
        updateWeather();
    }

    private void searchByGPS() {
        Log.d(TAG, "searchByGPS()");
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(UNIT.CELSIUS);
        mYahooWeather.setSearchMode(SEARCH_MODE.GPS);
        mYahooWeather.queryYahooWeatherByGPS(this, yahooWeatherInfoListener);
    }

    private void searchByPlaceName(String location) {
        Log.d(TAG, "searchByPlaceName() location: "+location);
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(UNIT.CELSIUS);
        mYahooWeather.setSearchMode(SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(this, location, yahooWeatherInfoListener);
    }

    YahooWeatherInfoListener yahooWeatherInfoListener = new YahooWeatherInfoListener() {
        @Override
        public void gotWeatherInfo(WeatherInfo weatherInfo) {
            if (weatherInfo != null) {
                /*
                Log.d(TAG,
                        weatherInfo.getTitle() + "\n"
                                + weatherInfo.getWOEIDneighborhood() + ", "
                                + weatherInfo.getWOEIDCounty() + ", "
                                + weatherInfo.getWOEIDState() + ", "
                                + weatherInfo.getWOEIDCountry());
                Log.d(TAG, "====== CURRENT ======" + "\n" +
                        "City: " + weatherInfo.getLocationCity() + "\n" +
                        "date: " + weatherInfo.getCurrentConditionDate() + "\n" +
                        "weather: " + weatherInfo.getCurrentText() + "\n" +
                        "temperature in ºC: " + weatherInfo.getCurrentTemp() + "\n" +
                        "wind chill: " + weatherInfo.getWindChill() + "\n" +
                        "wind direction: " + weatherInfo.getWindDirection() + "\n" +
                        "wind speed: " + weatherInfo.getWindSpeed() + "\n" +
                        "Humidity: " + weatherInfo.getAtmosphereHumidity() + "\n" +
                        "Pressure: " + weatherInfo.getAtmospherePressure() + "\n" +
                        "Visibility: " + weatherInfo.getAtmosphereVisibility() + "\n" +
                        "URL: " + weatherInfo.getCurrentConditionIconURL());
                */
                String todayLowHigh = "";

                for (int i = 0; i < YahooWeather.FORECAST_INFO_MAX_SIZE; i++) {
                    final WeatherInfo.ForecastInfo forecastInfo = weatherInfo.getForecastInfoList().get(i);
                    if(i == 0) {
                        todayLowHigh = new StringBuilder().append(forecastInfo.getForecastTempLow())
                                .append("º/ ").append(forecastInfo.getForecastTempHigh()).append("º").toString();
                    }
                    /*
                    Log.d(TAG, "====== FORECAST " + (i + 1) + " ======" + "\n" +
                                    "date: " + forecastInfo.getForecastDate() + "\n" +
                                    "weather: " + forecastInfo.getForecastText() + "\n" +
                                    "low  temperature in ºC: " + forecastInfo.getForecastTempLow() + "\n" +
                                    "high temperature in ºC: " + forecastInfo.getForecastTempHigh() + "\n"
						           "low  temperature in ºF: " + forecastInfo.getForecastTempLowF() + "\n" +
				                   "high temperature in ºF: " + forecastInfo.getForecastTempHighF() + "\n"
                    );
                    */
                }

                mTextViewWeatherTemp.setText(weatherInfo.getCurrentTemp() + getString(R.string.symbol_celsius));
                mTextViewWeatherCity.setText(weatherInfo.getLocationCity());
                mTextViewWeatherGeo.setText("(" + weatherInfo.getConditionLat() + "/" + weatherInfo.getConditionLon() + ")");
                mTextViewWeatherCondition.setText(weatherInfo.getCurrentText());
                mTextView_weather_highlow.setText(todayLowHigh);
                mTextViewWeatherHumi.setText(weatherInfo.getAtmosphereHumidity() + "%");
                /*
                if (weatherInfo.getCurrentConditionIcon() != null) {
                    imageViewWeatherIcon.setImageBitmap(weatherInfo.getCurrentConditionIcon());
                }
                */
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