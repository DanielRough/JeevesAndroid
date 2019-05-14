package com.jeevesandroid.sensing;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jeevesandroid.R;

public class HeartActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mHeartRate;
    private TextView txtHeart;
    private TextView txtAcc;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRate = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        txtHeart = findViewById(R.id.txtHeart);
        txtAcc = findViewById(R.id.txtAcc);
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("ACCURACY","Accuracy is " + accuracy);
        txtAcc.setText(String.format("%d",accuracy));
    }

    public void onSensorChanged(SensorEvent event) {
        StringBuilder message = new StringBuilder();
        for(int i = 0; i < event.values.length; i++){
            message.append(event.values[i]).append(",");
        }
        txtHeart.setText(message.toString());
    }
}
