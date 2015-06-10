package com.swproject.fi.workshopa;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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


public class MainActivity extends Activity implements View.OnTouchListener{
    private static AccelerometerObserver accelObs;
    public static final String TAG = "com.swproject.fi.workshopa.MainActivity";
    private static Intent labelBackhandAccel;
    private static Intent labelForehandAccel;
    private static Intent labelBackhandGyro;
    private static Intent labelForehandGyro;
    private static Intent labelBackhandMagnet;
    private static Intent labelForehandMagnet;
    private static int countBackhand = 0;
    private static int countForehand = 0;
    private static SharedPreferences prefs;
    private Runnable update;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnBackhand = (Button) findViewById(R.id.btnBackhand);
        Button btnForehand = (Button) findViewById(R.id.btnForehand);
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

        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_ACCELEROMETER, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_GYROSCOPE, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_GYROSCOPE, 20000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_MAGNETOMETER, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_MAGNETOMETER, 20000);

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
                        sendBroadcast(labelBackhandAccel);
                        sendBroadcast(labelBackhandGyro);
                        sendBroadcast(labelBackhandMagnet);
                        return true;
                    case MotionEvent.ACTION_UP:
                        countBackhand++;
                        editor.putInt("backhand", countBackhand);
                        editor.apply();
                        //editor.commit();
                        handler.postDelayed(update, 0);
                        return true;
                }
            break;
            case R.id.btnForehand:
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        sendBroadcast(labelForehandAccel);
                        sendBroadcast(labelForehandGyro);
                        sendBroadcast(labelForehandMagnet);
                        return true;
                    case MotionEvent.ACTION_UP:
                        countForehand++;
                        editor.putInt("forehand", countForehand);
                        editor.apply();
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
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)){
                labelBackhandAccel = new Intent(Accelerometer.ACTION_AWARE_ACCELEROMETER_LABEL);
                countBackhand = prefs.getInt("backhand", 0);
                labelBackhandAccel.putExtra(Accelerometer.EXTRA_LABEL, "backhand " + countBackhand);

                labelForehandAccel = new Intent(Accelerometer.ACTION_AWARE_ACCELEROMETER_LABEL);
                countForehand = prefs.getInt("forehand", 0);
                labelForehandAccel.putExtra(Accelerometer.EXTRA_LABEL, "forehand " + countForehand);
            }

            if (intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE)){
                labelBackhandGyro = new Intent(Gyroscope.ACTION_AWARE_GYROSCOPE_LABEL);
                countBackhand = prefs.getInt("backhand", 0);
                labelBackhandGyro.putExtra(Gyroscope.EXTRA_LABEL, "backhand " + countBackhand);

                labelForehandGyro = new Intent(Gyroscope.ACTION_AWARE_GYROSCOPE_LABEL);
                countForehand = prefs.getInt("forehand", 0);
                labelForehandGyro.putExtra(Gyroscope.EXTRA_LABEL, "forehand " + countBackhand);
            }

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)){
                labelBackhandMagnet = new Intent(Magnetometer.ACTION_AWARE_MAGNETOMETER_LABEL);
                countBackhand = prefs.getInt("backhand", 0);
                labelBackhandMagnet.putExtra(Magnetometer.EXTRA_LABEL, "backhand " + countBackhand);

                labelForehandMagnet = new Intent(Magnetometer.ACTION_AWARE_MAGNETOMETER_LABEL);
                countForehand = prefs.getInt("forehand", 0);
                labelForehandMagnet.putExtra(Magnetometer.EXTRA_LABEL, "forehand " + countForehand);
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

                    Intent lable = new Intent(Accelerometer.ACTION_AWARE_ACCELEROMETER_LABEL);
                    lable.putExtra(Accelerometer.EXTRA_LABEL, "MY_DATA");
                    sendBroadcast(lable);

                    mag_accel = Math.sqrt(x * x + y * y + z * z);
                    //Log.e(TAG, "result accel " + mag_accel);

                }while( raw_data.moveToNext() );
            }
            if( raw_data != null && ! raw_data.isClosed() ) raw_data.close();
        }
    }
}
