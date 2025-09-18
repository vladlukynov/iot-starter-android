package com.example.accelerometer.mqtt;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MqttEventListener implements IMqttActionListener {
    private final Context context;
    private final Runnable publishCallback;
    private final RunnableTask runnableTask = new RunnableTask();
    private final Handler handler = new Handler();
    private final Long publishDelayMs;

    public MqttEventListener(Context context,
                             Runnable publishCallback,
                             Long publishDelayMs) {
        this.context = context;
        this.publishCallback = publishCallback;
        this.publishDelayMs = publishDelayMs;
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        // We are connected
        Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();
        publishCallback.run();

        handler.postDelayed(runnableTask, publishDelayMs);
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        // Something went wrong e.g. connection timeout or firewall problems
        Log.e("MqttConnection", "Connection Failed", exception);
        Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    public void stopHandling() {
        handler.removeCallbacks(runnableTask);
    }

    private class RunnableTask implements Runnable {
        @Override
        public void run() {
            publishCallback.run();
            // this will repeat this task again at specified time interval
            handler.postDelayed(this, publishDelayMs);
        }
    }
}
