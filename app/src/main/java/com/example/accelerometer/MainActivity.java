package com.example.accelerometer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accelerometer.mqtt.MqttClientWrapper;
import com.example.accelerometer.mqtt.MqttEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final Long PUBLISH_DELAY_MS = 5000L;

    private final MqttEventListener mqttEventListener = new MqttEventListener(
            MainActivity.this,
            this::publish,
            PUBLISH_DELAY_MS
    );
    private MqttClientWrapper client;
    private TextView xVal, yVal, zVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerAccelerometer();
        this.client = new MqttClientWrapper(
                mqttEventListener,
                this.getApplicationContext()
        );

        // Text View
        xVal = findViewById(R.id.xValue);
        yVal = findViewById(R.id.yVlaue);
        zVal = findViewById(R.id.zValue);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xVal.setText("X: " + event.values[0]);
        yVal.setText("Y: " + event.values[1]);
        zVal.setText("Z: " + event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    public void startPublish(View v) {
        client.connect();
    }

    public void publish() {
        SharedPreferences sharedPref = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String mqttTopic = sharedPref.getString("Topic", "");
        String message = xVal.getText().toString() + "," + yVal.getText().toString() + "," + zVal.getText().toString();
        client.publish(mqttTopic, message);
        Log.d("MainActivity", String.format("Published message: %s", message));
    }

    public void registerAccelerometer() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register Sensor Listener
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopPublish(View v) {
        client.disconnect();
    }

    public void handleClick(View v) {
        startActivity(new Intent(MainActivity.this, MqttSettings.class));
    }

    public void handleAboutUs(View v) {
        startActivity(new Intent(MainActivity.this, AboutUs.class));
    }
}
