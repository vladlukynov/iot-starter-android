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

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final Long PUBLISH_DELAY_MS = 5000L;

    private final MqttEventListener mqttEventListener = new MqttEventListener(
            MainActivity.this,
            this::publish,
            PUBLISH_DELAY_MS
    );
    private MqttClientWrapper client;
    private TextView luxValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerLightSensor();
        this.client = new MqttClientWrapper(
                mqttEventListener,
                this.getApplicationContext()
        );

        // Text View
        luxValue = findViewById(R.id.luxValue);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        luxValue.setText("Освещенность (LX): " + event.values[0]);
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
        String message = "{\"result\":\"" + luxValue.getText().toString() + "\"}";
        client.publish(mqttTopic, message);
        Log.d("MainActivity", String.format("Published message: %s", message));
    }

    public void registerLightSensor() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Light or Proximity Sensor
        Sensor lightSensor = Objects.requireNonNullElse(
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));

        // Register Sensor Listener
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
