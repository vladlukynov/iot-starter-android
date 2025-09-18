package com.example.accelerometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MqttSettings extends AppCompatActivity {
    private EditText topic, username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_settings);

        topic = findViewById(R.id.txtTopic);
        username = findViewById(R.id.txtUsername);
        password = findViewById(R.id.pwPassword);

        SharedPreferences sharedPref = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String mqttTopic = sharedPref.getString("Topic", "");
        String mqttUsername = sharedPref.getString("Username", "");
        String mqttPassword = sharedPref.getString("Password", "");

        topic.setText(mqttTopic);
        username.setText(mqttUsername);
        password.setText(mqttPassword);
    }

    public void connectMqtt(View v) {
        SharedPreferences sharedPref = this.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Topic", topic.getText().toString());
        editor.putString("Username", username.getText().toString());
        editor.putString("Password", password.getText().toString());
        editor.apply();

        Toast.makeText(MqttSettings.this, "Saved successfully", Toast.LENGTH_LONG).show();
    }
}
