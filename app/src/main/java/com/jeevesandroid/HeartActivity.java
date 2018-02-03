package com.jeevesandroid;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import static android.hardware.SensorManager.*;

public class HeartActivity extends AppCompatActivity implements SensorEventListener {

    private  SensorManager mSensorManager;
    private  Sensor mHeartRate;
    private TextView txtHeart;
    private TextView txtAcc;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        txtHeart = (TextView)findViewById(R.id.txtHeart);
        txtAcc = (TextView)findViewById(R.id.txtAcc);
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mHeartRate, SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("ACCURACY","Accuracy is " + accuracy);
        txtAcc.setText(Integer.toString(accuracy));
    }

    public void onSensorChanged(SensorEvent event) {
      //  String msg = "" + (int)event.values[0];
        String message = "";
        for(int i = 0; i < event.values.length; i++){
            message += event.values[i] + ",";
        }
       // Log.d("HEART RATE","Heart rate is " + msg);

        txtHeart.setText(message);
    }
}
