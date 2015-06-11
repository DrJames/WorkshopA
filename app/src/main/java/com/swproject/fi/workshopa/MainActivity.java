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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnTouchListener{
    private static AccelerometerObserver accelObs;
    public static final String TAG = "com.fi.workshopa.MainActivity";

    private static int countBackhand = 0;
    private static int countForehand = 0;
    private static boolean isBackPressed = false;
    private static boolean isForePressed = false;
    private static long timestampStart;
    private static long timestampEnd;
    private SharedPreferences prefs;
    private Runnable update;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnBackhand = (Button) findViewById(R.id.btnBackhand);
        Button btnForehand = (Button) findViewById(R.id.btnForehand);

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
        final TextView txtFore = (TextView) findViewById(R.id.txtForehand);

        update = new Runnable() {
            @Override
            public void run() {
                txtBack.setText("" + countBackhand);
                txtFore.setText("" + countForehand);
            }
        };

        handler = new Handler();

        btnBackhand.setOnTouchListener(this);
        btnForehand.setOnTouchListener(this);

        prefs = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        //countForehand = prefs.getInt("forehand", 0);
        //countBackhand = prefs.getInt("backhand", 0);


        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_GYROSCOPE, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_MAGNETOMETER, 20000);

        /*accelObs = new AccelerometerObserver(new Handler());
        getContentResolver().registerContentObserver(
                Accelerometer_Provider.Accelerometer_Data.CONTENT_URI,
                true,
                accelObs);*/

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
        getContentResolver().unregisterContentObserver(accelObs);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, false);
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        SharedPreferences.Editor editor = prefs.edit();
        switch (view.getId()){
            case R.id.btnBackhand:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isBackPressed = true;
                        Log.e(TAG, "ACTION_DOWN");
                        timestampStart = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_UP:
                        isBackPressed = false;
                        timestampEnd = System.currentTimeMillis();
                        Log.e(TAG, "ACTION_UP");
                        editor.putInt("backhand", countBackhand);
                        editor.apply();
                        countBackhand++;
                        //editor.commit();
                        handler.postDelayed(update, 0);
                        return true;
                }
            break;
            case R.id.btnForehand:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isForePressed = true;

                        return true;

                    case MotionEvent.ACTION_UP:
                        isForePressed = false;
                        editor.putInt("forehand", countForehand);
                        editor.apply();
                        countForehand++;
                        //editor.commit();
                        handler.postDelayed(update, 0);
                        return true;
                }
            break;
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
        private List<Double> accel;
        private List<Double> gyro;
        private List<Double> magnet;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)){
                ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
                Log.d("DEMO", raw_data.toString());

                accel = new ArrayList<>(2);

                double x = raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0);
                double y = raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1);
                double z = raw_data.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2);

                if (isBackPressed){
                    ContentValues data = new ContentValues();
                    data.put(Provider.Plugin_Data.TIMESTAMP, System.currentTimeMillis());
                    data.put(Provider.Plugin_Data.DEVICE_ID, Aware.getSetting(context, Aware_Preferences.DEVICE_ID));
                    data.put(Provider.Plugin_Data.ACCEL_AXIS_X, x);
                    data.put(Provider.Plugin_Data.ACCEL_AXIS_Y, y);
                    data.put(Provider.Plugin_Data.ACCEL_AXIS_Z, z);
                    data.put(Provider.Plugin_Data.COUNT, countBackhand);
                    context.getContentResolver().insert(Provider.Plugin_Data.CONTENT_URI, data);
                }
            }

            if (intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE)){
                ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);

                double x = raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0);
                double y = raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1);
                double z = raw_data.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2);

                if (isBackPressed){
                    ContentValues data = new ContentValues();
                    //data.put();
                }
                if (isForePressed){
                    Intent labelForehandGyro = new Intent(Gyroscope.ACTION_AWARE_GYROSCOPE_LABEL);
                    labelForehandGyro.putExtra(Gyroscope.EXTRA_LABEL, "forehand " + countBackhand);
                    context.sendBroadcast(labelForehandGyro);
                }
            }

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)){
                if (isBackPressed){
                    Intent labelBackhandMagnet = new Intent(Magnetometer.ACTION_AWARE_MAGNETOMETER_LABEL);
                    labelBackhandMagnet.putExtra(Magnetometer.EXTRA_LABEL, "backhand " + countBackhand);
                    context.sendBroadcast(labelBackhandMagnet);
                }
                if (isForePressed){
                    Intent labelForehandMagnet = new Intent(Magnetometer.ACTION_AWARE_MAGNETOMETER_LABEL);
                    labelForehandMagnet.putExtra(Magnetometer.EXTRA_LABEL, "forehand " + countForehand);
                    context.sendBroadcast(labelForehandMagnet);
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
            if (isBackPressed){
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
