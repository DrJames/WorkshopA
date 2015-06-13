package com.swproject.fi.workshopa;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aware.Accelerometer;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.aware.Magnetometer;
import com.aware.providers.Accelerometer_Provider;
import com.aware.providers.Gyroscope_Provider;
import com.aware.providers.Magnetometer_Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppActivity extends Activity {
    private static boolean isPressed = false;
    private static List<Double[]> accelResult = new ArrayList<>();
    private static List<Double[]> gyroResult = new ArrayList<>();
    private static List<Double[]> magnerResult= new ArrayList<>();
    private static Double[] magnet;
    private static Double[] accel;
    private static Double[] gyro;
    private double result;
    public static String remoteResponse = "";
    public String localResponse = "a";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        //final MediaPlayer mp = MediaPlayer.create(this, R.raw.soho);
        //result = svm.svm_predict(new svm_model(), new svm_node[12]);
        Button btnThrow = (Button) findViewById(R.id.btnThrow);
        btnThrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isPressed = false;
                        //if (BluetoothService.STATE_CONNECTED)
                        BluetoothConnect.sendMessage(analyzeData(), getApplicationContext());
                        //Toast.makeText(getApplicationContext(), analyzeData(), Toast.LENGTH_SHORT).show();
                        resetData();
                        return true;
                }
                return false;
            }
        });

        Button btnReceive = (Button) findViewById(R.id.btnReceive);
        btnReceive.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        return true;
                    case MotionEvent.ACTION_UP:
                        isPressed = false;
                        localResponse = analyzeData();
                        Toast.makeText(getApplicationContext(), localResponse, Toast.LENGTH_SHORT).show();
                        resetData();
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

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.e("DBG","I timer data");
                if (remoteResponse.equals(localResponse)) {
                    Log.e("++++++++++", "TAAAAADAAAAAAAAA");
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();

                        remoteResponse = "";
                        Toast.makeText(getApplicationContext(), "CATCH!!!!!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                /*if (isPressed) {
                    Log.d("DBG","I read data");
                    if (accel != null && gyro != null && magnet != null) {
                        Log.d("DBG","I collect data");
                        //collectData();
                    }
                }*/

            }
        }, 0, 200, TimeUnit.MICROSECONDS);

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
        filter.addAction(GlobalAccel.ACTION_NEW_DATA);
        registerReceiver(sensorReceiver, filter);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        new GlobalAccel(getApplicationContext());
    }

    private void resetData(){
        accelResult = new ArrayList<>();
        gyroResult = new ArrayList<>();
        magnerResult = new ArrayList<>();
    }

    private void collectData(){
        accelResult.add(accel);
        gyroResult.add(gyro);
        magnerResult.add(magnet);
    }

    private String analyzeData() {
        int index = accelResult.size() - 1;
        Log.d("DBG", "index " + index);

        double magx_avg;
        double magy_avg;
        double magz_avg;
        double linx_avg;
        double liny_avg;
        double linz_avg;
        double gyrox_avg;
        double gyroy_avg;
        double gyroz_avg;

        double accel_x = 0;
        double accel_y = 0;
        double accel_z = 0;
        double gyro_x = 0;
        double gyro_y = 0;
        double gyro_z = 0;
        double magnet_x = 0;
        double magnet_y = 0;
        double magnet_z = 0;
        int count = 1;

        while (index >= 0) {

            accel_x += accelResult.get(index)[0];
            accel_y += accelResult.get(index)[1];
            accel_z += accelResult.get(index)[2];

//            magnet_x += magnerResult.get(index)[0];
//            magnet_y += magnerResult.get(index)[1];
//            magnet_z += magnerResult.get(index)[2];

//            gyro_x += gyroResult.get(index)[0];
//            gyro_y += gyroResult.get(index)[1];
//            gyro_z += gyroResult.get(index)[2];

            count++;
            index--;
        }
        linx_avg = accel_x / count;
        liny_avg = accel_y / count;
        linz_avg = accel_z / count;
        magx_avg = magnet_x / count;
        magy_avg = magnet_y / count;
        magz_avg = magnet_z / count;
        gyrox_avg = gyro_x / count;
        gyroy_avg = gyro_y / count;
        gyroz_avg = gyro_z / count;
        
        int i = accelResult.size() - 1;

        if (linx_avg  * linx_avg > liny_avg * liny_avg){
            if (linx_avg > 0)
                return "East";
            else
                return "West";
        }
        else {
            if (liny_avg > 0)
                return "North";
            else
                return "South";
        }

        //do{
        /*if (magnerResult.get(i)[0] < 39.25) {
            if (magy_avg < -26.59) {
                if (linz_avg < 2.53) {
                    if (linx_avg < -2.84) {
                        if (linx_avg < -10.42) {
                            return "West";
                        }
                        if (linx_avg >= -10.42) {
                            if (liny_avg < 5.9) {
                                if (accelResult.get(i)[0] < 17.06) {
                                    return "North";
                                }
                                if (accelResult.get(i)[0] >= 17.06) {
                                    return "North";
                                }
                            }
                            if (liny_avg >= 5.9) {
                                if (magnerResult.get(i)[0] < -4.46) {
                                    return "West";
                                }
                                if (magnerResult.get(i)[0] >= -4.46) {
                                    return "North";
                                }
                            }
                        }
                    }
                    if (linx_avg >= -2.84) {
                        if (linz_avg < 1.58) {
                            if (magnerResult.get(i)[0] < 17.45) {
                                if (mag_y < -50.45) {
                                    return "South";
                                }
                                if (mag_y >= -50.45) {
                                    return "West";
                                }
                            }
                            if (magnerResult.get(i)[0] >= 17.45) {
                                if (magx_avg < 16.28) {
                                    return "North";
                                }
                                if (magx_avg >= 16.28) {
                                    return "South";
                                }
                            }
                        }
                        if (linz_avg >= 1.58) {
                            if (gyro_y < -2.55) {
                                return "East";
                            }
                            if (gyro_y >= -2.55) {
                                return "North";
                            }
                        }
                    }
                }
                if (linz_avg >= 2.53) {
                    if (magx_avg < 12.65) {
                        if (gyroy_avg < 1.21) {
                            if (linz_avg < 5.44) {
                                if (mag_y < -51.57) {
                                    return "South";
                                }
                                if (mag_y >= -51.57) {
                                    return "East";
                                }
                            }
                            if (linz_avg >= 5.44) {
                                if (gyroy_avg < -2.29) {
                                    return "North";
                                }
                                if (gyroy_avg >= -2.29) {
                                    return "South";
                                }
                            }
                        }
                        if (gyroy_avg >= 1.21) {
                            if (accelResult.get(i)[0] < 5.98) {
                                return "West";
                            }
                            if (accelResult.get(i)[0] >= 5.98) {
                                return "North";
                            }
                        }
                    }
                    if (magx_avg >= 12.65) {
                        if (gyro_y < 2.93) {
                            return "West";
                        }
                        if (gyro_y >= 2.93) {
                            return "North";
                        }
                    }
                }
            }
            if (magy_avg >= -26.59) {
                if (magz_avg < -41.26) {
                    if (magz_avg < -51.57) {
                        return "North";
                    }
                    if (magz_avg >= -51.57) {
                        if (magy_avg < -25.45) {
                            if (mag_y < -18.77) {
                                return "East";
                            }
                            if (mag_y >= -18.77) {
                                return "North";
                            }
                        }
                        if (magy_avg >= -25.45) {
                            if (accelResult.get(i)[2] < 6.24) {
                                return "East";
                            }
                            if (accelResult.get(i)[2] >= 6.24) {
                                if (magnerResult.get(i)[0] < 0.36) {
                                    return "West";
                                }
                                if (magnerResult.get(i)[0] >= 0.36) {
                                    return "West";
                                }
                            }
                        }
                    }
                }
                if (magz_avg >= -41.26) {
                    if (linz_avg < 0.45) {
                        if (linx_avg < -4.37) {
                            if (accelResult.get(i)[1] < 8.78) {
                                if (liny_avg < -3.57) {
                                    return "North";
                                }
                                if (liny_avg >= -3.57) {
                                    return "North";
                                }
                            }
                            if (accelResult.get(i)[1] >= 8.78) {
                                if (liny_avg < -2.95) {
                                    return "North";
                                }
                                if (liny_avg >= -2.95) {
                                    return "South";
                                }
                            }
                        }
                        if (linx_avg >= -4.37) {
                            if (linx_avg < -3.44) {
                                return "East";
                            }
                            if (linx_avg >= -3.44) {
                                return "South";
                            }
                        }
                    }
                    if (linz_avg >= 0.45) {
                        if (gyroy_avg < -1.5) {
                            if (magnerResult.get(i)[0] < -31.2) {
                                return "South";
                            }
                            if (magnerResult.get(i)[0] >= -31.2) {
                                if (magz_avg < -0.37) {
                                    return "North";
                                }
                                if (magz_avg >= -0.37) {
                                    return "North";
                                }
                            }
                        }
                        if (gyroy_avg >= -1.5) {
                            if (magx_avg < 11.07) {
                                if (magx_avg < -17.8) {
                                    return "North";
                                }
                                if (magx_avg >= -17.8) {
                                    return "West";
                                }
                            }
                            if (magx_avg >= 11.07) {
                                if (magy_avg < -21.93) {
                                    return "East";
                                }
                                if (magy_avg >= -21.93) {
                                    return "West";
                                }
                            }
                        }
                    }
                }
            }
        }
        if (magnerResult.get(i)[0] >= 39.25) {
            if (magz_avg < 3.87) {
                if (liny_avg < -4.77) {
                    if (linz_avg < 6.27) {
                        if (magz_avg < -1.18) {
                            if (linz_avg < 4.91) {
                                if (magz_avg < -4.56) {
                                    return "East";
                                }
                                if (magz_avg >= -4.56) {
                                    return "South";
                                }
                            }
                            if (linz_avg >= 4.91) {
                                if (mag_y < 5.25) {
                                    return "East";
                                }
                                if (mag_y >= 5.25) {
                                    return "East";
                                }
                            }
                        }
                        if (magz_avg >= -1.18) {
                            return "East";
                        }
                    }
                    if (linz_avg >= 6.27) {
                        if (magnerResult.get(i)[0] < 6.35) {
                            if (gyro_y < -3.48) {
                                return "East";
                            }
                            if (gyro_y >= -3.48) {
                                return "North";
                            }
                        }
                        if (magnerResult.get(i)[0] >= 6.35) {
                            return "South";
                        }
                    }
                }
                if (liny_avg >= -4.77) {
                    if (liny_avg < -2.92) {
                        if (magz_avg < -4.77) {
                            return "East";
                        }
                        if (magz_avg >= -4.77) {
                            if (linx_avg < -3.45) {
                                return "North";
                            }
                            if (linx_avg >= -3.45) {
                                if (accelResult.get(i)[2] < 14.6) {
                                    return "West";
                                }
                                if (accelResult.get(i)[2] >= 14.6) {
                                    return "West";
                                }
                            }
                        }
                    }
                    if (liny_avg >= -2.92) {
                        if (magx_avg < 42.65) {
                            if (magz_avg < 2.22) {
                                if (magy_avg < -24.73) {
                                    return "North";
                                }
                                if (magy_avg >= -24.73) {
                                    return "South";
                                }
                            }
                            if (magz_avg >= 2.22) {
                                if (linz_avg < 8) {
                                    return "East";
                                }
                                if (linz_avg >= 8) {
                                    return "North";
                                }
                            }
                        }
                        if (magx_avg >= 42.65) {
                            return "South";
                        }
                    }
                }
            }
            if (magz_avg >= 3.87) {
                if (linz_avg < 0.26) {
                    if (magnerResult.get(i)[0] < 19.45) {
                        if (magy_avg < -17.82) {
                            return "South";
                        }
                        if (magy_avg >= -17.82) {
                            return "North";
                        }
                    }
                    if (magnerResult.get(i)[0] >= 19.45) {
                        if (magz_avg < 12.7) {
                            return "South";
                        }
                        if (magz_avg >= 12.7) {
                            return "North";
                        }
                    }
                }
                if (linz_avg >= 0.26) {
                    if (magy_avg < -8.72) {
                        if (magnerResult.get(i)[0] < 44.65) {
                            if (linz_avg < 3.48) {
                                if (magx_avg < 26.42) {
                                    return "East";
                                }
                                if (magx_avg >= 26.42) {
                                    return "East";
                                }
                            }
                            if (linz_avg >= 3.48) {
                                if (liny_avg < -2.52) {
                                    return "North";
                                }
                                if (liny_avg >= -2.52) {
                                    return "West";
                                }
                            }
                        }
                        if (magnerResult.get(i)[0] >= 44.65) {
                            if (magz_avg < 9.51) {
                                if (magy_avg < -31.59) {
                                    return "North";
                                }
                                if (magy_avg >= -31.59) {
                                    return "West";
                                }
                            }
                            if (magz_avg >= 9.51) {
                                if (accelResult.get(i)[0] < 15.5) {
                                    return "North";
                                }
                                if (accelResult.get(i)[0] >= 15.5) {
                                    return "West";
                                }
                            }
                        }
                    }
                    if (magy_avg >= -8.72) {
                        if (magnerResult.get(i)[0] < 21.21) {
                            if (gyroy_avg < -0.93) {
                                if (accelResult.get(i)[0] < 5.25) {
                                    return "West";
                                }
                                if (accelResult.get(i)[0] >= 5.25) {
                                    return "South";
                                }
                            }
                            if (gyroy_avg >= -0.93) {
                                return "South";
                            }
                        }
                        if (magnerResult.get(i)[0] >= 21.21) {
                            if (linz_avg < 2.88) {
                                if (liny_avg < -4.69) {
                                    return "East";
                                }
                                if (liny_avg >= -4.69) {
                                    return "West";
                                }
                            }
                            if (linz_avg >= 2.88) {
                                if (magx_avg < 29.42) {
                                    return "South";
                                }
                                if (magx_avg >= 29.42) {
                                    return "North";
                                }
                            }
                        }
                    }
                }
            }
        }
        return "Default";*/
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
            /*if (intent.getAction().equals(Accelerometer.ACTION_AWARE_ACCELEROMETER)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Accelerometer.EXTRA_DATA);
                    getAccelData(raw_data);
                    accelResult.add(accel);
                    //Log.e("------", accelResult.get(i)[0].toString());

                }
            }*/

            if (intent.getAction().equals(GlobalAccel.ACTION_NEW_DATA)){
                if (isPressed){
                    accel = new Double[3];
                    accel[0] = new Double( intent.getFloatExtra(GlobalAccel.X, 0));
                    accel[1] = new Double( intent.getFloatExtra(GlobalAccel.Y, 0));
                    accel[2] = new Double( intent.getFloatExtra(GlobalAccel.Z, 0));
                    if (isPressed) {
                        accelResult.add(accel);
                        Log.d("DBG", "I fuking add data");
                    }

                }
            }


            if (intent.getAction().equals(Gyroscope.ACTION_AWARE_GYROSCOPE)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Gyroscope.EXTRA_DATA);
                    getGyroData(raw_data);
                    gyroResult.add(gyro);
                    //Log.e("------", gyro[1].toString());
                }
            }

            if (intent.getAction().equals(Magnetometer.ACTION_AWARE_MAGNETOMETER)){
                if (isPressed){
                    ContentValues raw_data = (ContentValues) intent.getParcelableExtra(Magnetometer.EXTRA_DATA);
                    getMagnetData(raw_data);
                    magnerResult.add(magnet);
                    //Log.e("------", magnerResult.get(i)[0].toString());
                }
            }
        }
    }
}
