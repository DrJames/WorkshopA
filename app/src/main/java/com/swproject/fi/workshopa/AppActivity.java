package com.swproject.fi.workshopa;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.aware.Magnetometer;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Gyroscope_Provider;
import com.aware.providers.Magnetometer_Provider;


public class AppActivity extends Activity {
    private static Double[] magnet;
    private static Double[] accel;
    private static Double[] gyro;
    private static boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        Button btnThrow = (Button) findViewById(R.id.btnThrow);
        btnThrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isPressed = false;
                        return true;
                }
                return false;
            }
        });
        Button btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent connect = new Intent(getApplicationContext(), BluetoothConnect.class);
                startActivity(connect);
            }
        });

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothConnect.sendMessage("LOLOLO", getApplicationContext());
            }
        });

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 2);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_GYROSCOPE, 2);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_MAGNETOMETER, 2);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Accelerometer.ACTION_AWARE_ACCELEROMETER);
        filter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        filter.addAction(Magnetometer.ACTION_AWARE_MAGNETOMETER);
        registerReceiver(sensorReceiver, filter);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);
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
        unregisterReceiver(sensorReceiver);
    }

    DataReceiver sensorReceiver = new DataReceiver();

    private static class DataReceiver extends BroadcastReceiver{
        private void getAccelData(ContentValues values){
            accel = new Double[3];
            accel[0] = values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_0);
            accel[1] = values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_1);
            accel[2] = values.getAsDouble(Accelerometer_Provider.Accelerometer_Data.VALUES_2);
            //return accel;
        }

        private void getGyroData(ContentValues values){
            gyro = new Double[3];
            gyro[0] = values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_0);
            gyro[1] = values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_1);
            gyro[2] = values.getAsDouble(Gyroscope_Provider.Gyroscope_Data.VALUES_2);
            //return gyro;
        }

        private void getMagnetData(ContentValues values){
            magnet = new Double[3];
            magnet[0] = values.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_0);
            magnet[1] = values.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_1);
            magnet[2] = values.getAsDouble(Magnetometer_Provider.Magnetometer_Data.VALUES_2);
            //return magnet;
        }
        @Override
        public void onReceive(Context context, Intent intent){
            if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
                    getAccelData(raw_data);
                    Log.e("------", accel[0].toString());
                }
            }

            if (intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Gyroscope.EXTRA_DATA);
                    getGyroData(raw_data);
                    Log.e("------", gyro[1].toString());
                }
            }

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Magnetometer.EXTRA_DATA);
                    getMagnetData(raw_data);
                    Log.e("------", magnet[2].toString());
                }
            }
        }
    }
}
