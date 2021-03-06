package com.swproject.fi.workshopa;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.aware.Magnetometer;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Gyroscope_Provider;
import com.aware.providers.Magnetometer_Provider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements View.OnTouchListener{
    private static AccelerometerObserver accelObs;
    public static final String TAG = "com.fi.workshopa.MainActivity";

    private static int countCommon;
    private static boolean isNorthPressed = false;
    private static boolean isSouthPressed = false;
    private static boolean isWestPressed = false;
    private static boolean isEastPressed = false;
    private static SharedPreferences prefs;
    private Runnable update;
    private Handler handler;
    private static Double[] accel;
    private static Double[] gyro;
    private static Double[] magnet;
    private static int label;
    private String flag;

    //1 - NORTH
    //2 - WEST
    //3 - SOUTH
    //4 - EAST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnNorth = (Button) findViewById(R.id.btnNorth);
        Button btnSouth = (Button) findViewById(R.id.btnSouth);
        Button btnWest = (Button) findViewById(R.id.btnWest);
        Button btnEast = (Button) findViewById(R.id.btnEast);

        btnNorth.setOnTouchListener(this);
        btnSouth.setOnTouchListener(this);
        btnWest.setOnTouchListener(this);
        btnEast.setOnTouchListener(this);

        /*Switch swh = (Switch) findViewById(R.id.switch1);
        swh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    isBackPressed = true;
                else
                    isBackPressed = false;
            }
        });*/
        final TextView txtBack = (TextView) findViewById(R.id.txtBackhand);

        if (flag == null){
            flag = "started";
        }

        update = new Runnable() {
            @Override
            public void run() {
                txtBack.setText("" + countCommon);
            }
        };

        handler = new Handler();

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_GYROSCOPE, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_MAGNETOMETER, 20000);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.e("+++++++", "lOOOOOO");
                Log.e("++++++", "" + isNorthPressed);
                if (isNorthPressed || isWestPressed || isSouthPressed || isEastPressed) {
                    Log.e("++++++++", "WRITING!!!");
                    if (accel != null && gyro != null && magnet != null){
                        ContentValues data = new ContentValues();
                        data.put(Provider.Plugin_Data.TIMESTAMP, System.currentTimeMillis());
                        data.put(Provider.Plugin_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                        data.put(Provider.Plugin_Data.ACCEL_AXIS_X, accel[0]);
                        data.put(Provider.Plugin_Data.ACCEL_AXIS_Y, accel[1]);
                        data.put(Provider.Plugin_Data.ACCEL_AXIS_Z, accel[2]);
                        data.put(Provider.Plugin_Data.GYRO_AXIS_X, gyro[0]);
                        data.put(Provider.Plugin_Data.GYRO_AXIS_Y, gyro[1]);
                        data.put(Provider.Plugin_Data.GYRO_AXIS_Z, gyro[2]);
                        data.put(Provider.Plugin_Data.MAGNET_AXIS_X, magnet[0]);
                        data.put(Provider.Plugin_Data.MAGNET_AXIS_Y, magnet[1]);
                        data.put(Provider.Plugin_Data.MAGNET_AXIS_Z, magnet[2]);
                        data.put(Provider.Plugin_Data.LABEL, label);
                        data.put(Provider.Plugin_Data.COUNT, countCommon);
                        getApplicationContext().getContentResolver().insert(Provider.Plugin_Data.CONTENT_URI, data);
                        //Log.e("++++++++", "WRITING!!!");
                    }
                }
            }
        }, 0, 20, TimeUnit.MICROSECONDS);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Accelerometer.ACTION_AWARE_ACCELEROMETER);
        filter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        filter.addAction(Magnetometer.ACTION_AWARE_MAGNETOMETER);
        registerReceiver(accelReceiver, filter);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public void onResume(){
        super.onResume();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(accelReceiver);
        //getContentResolver().unregisterContentObserver(accelObs);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, false);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("counter", countCommon);
        editor.apply();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){
            case R.id.btnNorth:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isNorthPressed = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        isNorthPressed = false;
                        countCommon++;
                        handler.postDelayed(update, 0);
                        return true;
                }
                break;
            case R.id.btnSouth:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isSouthPressed = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        isSouthPressed = false;
                        countCommon++;
                        handler.postDelayed(update, 0);
                        return true;
                }
                break;
            case R.id.btnWest:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isWestPressed = true;
                        return true;

                    case MotionEvent.ACTION_UP:
                        isWestPressed = false;
                        countCommon++;
                        handler.postDelayed(update, 0);
                        return true;
                }
                break;
            case R.id.btnEast:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isEastPressed = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isEastPressed = false;
                        countCommon++;
                        handler.postDelayed(update, 0);
                        return true;
                }
        }
        return false;
    }

    private void onInitialize(){

        /*accelObs = new AccelerometerObserver(new Handler());
        getContentResolver().registerContentObserver(
                Accelerometer_Provider.Accelerometer_Data.CONTENT_URI,
                true,
                accelObs);*/
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    private void onFinish(){
        //getContentResolver().unregisterContentObserver(accelObs);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    private static AccelReceiver accelReceiver = new AccelReceiver();

    public static class AccelReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)){
                ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
                Log.d("DEMO", raw_data.toString());
                prefs = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
                if (countCommon == 0)
                    countCommon = prefs.getInt("counter", 0);

                if (isNorthPressed){
                    accel = new Double[3];
                    accel[0] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                    accel[1] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                    accel[2] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2));
                    label = 1;
                }

                if (isWestPressed){
                    accel = new Double[3];
                    accel[0] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                    accel[1] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                    accel[2] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2));
                    label = 2;
                }

                if (isSouthPressed){
                    accel = new Double[3];
                    accel[0] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                    accel[1] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                    accel[2] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2));
                    label = 3;
                }

                if (isEastPressed){
                    accel = new Double[3];
                    accel[0] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                    accel[1] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                    accel[2] = (raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2));
                    label = 4;
                }
            }

            if (intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE)){
                ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Gyroscope.EXTRA_DATA);

                if (isNorthPressed){
                    gyro = new Double[3];
                    gyro[0] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                    gyro[1] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1));
                    gyro[2] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2));
                    label = 1;
                }

                if (isWestPressed){
                    gyro = new Double[3];
                    gyro[0] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                    gyro[1] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1));
                    gyro[2] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2));
                    label = 2;
                }

                if (isSouthPressed){
                    gyro = new Double[3];
                    gyro[0] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                    gyro[1] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1));
                    gyro[2] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2));
                    label = 3;
                }

                if (isEastPressed){
                    gyro = new Double[3];
                    gyro[0] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0));
                    gyro[1] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1));
                    gyro[2] = (raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2));
                    label = 4;
                }
            }

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)){
                ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Magnetometer.EXTRA_DATA);

                if (isNorthPressed){
                    magnet = new Double[3];
                    magnet[0] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_0));
                    magnet[1] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_1));
                    magnet[2] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_2));
                    label = 1;
                }

                if (isWestPressed){
                    magnet = new Double[3];
                    magnet[0] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_0));
                    magnet[1] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_1));
                    magnet[2] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_2));
                    label = 2;
                }

                if (isSouthPressed){
                    magnet = new Double[3];
                    magnet[0] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_0));
                    magnet[1] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_1));
                    magnet[2] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_2));
                    label = 3;
                }

                if (isEastPressed){
                    magnet = new Double[3];
                    magnet[0] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_0));
                    magnet[1] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_1));
                    magnet[2] = (raw_data.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_2));
                    label = 4;
                }
            }
        }
    }

    public class AccelerometerObserver extends ContentObserver {
        public AccelerometerObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (isEastPressed){
                Cursor raw_data = getContentResolver().query(
                        Accelerometer_Provider.Accelerometer_Data.CONTENT_URI,
                        null,
                        null,
                        null,
                        Accelerometer_Provider.Accelerometer_Data.TIMESTAMP + " DESC LIMIT 1");

                double mag_accel = 0;
                if( raw_data != null && raw_data.moveToFirst() ) {
                    do {

                        double x = raw_data.getDouble(raw_data.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_0));
                        double y = raw_data.getDouble(raw_data.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_1));
                        double z = raw_data.getDouble(raw_data.getColumnIndex(Accelerometer_Provider.Accelerometer_Data.VALUES_2));

                        mag_accel = Math.sqrt(x * x + y * y + z * z);
                        //Log.e(TAG, "result accel " + mag_accel);

                    }while( raw_data.moveToNext() );
                }
                if( raw_data != null && ! raw_data.isClosed() ) raw_data.close();
            }
        }

    }
}