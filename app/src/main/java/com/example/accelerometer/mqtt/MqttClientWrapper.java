package com.example.accelerometer.mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.example.accelerometer.R;

public class MqttClientWrapper {
    private static final String CLIENT_ID = "YandexIoTCoreTestJavaClient";
    private static final String URL = "ssl://mqtt.cloud.yandex.net:8883";
    private static final String X509_CERTIFICATE_TYPE = "X.509";
    private static final String SSL_PROTOCOL_TYPE = "TLS";
    private static final String CERTIFYING_CENTER_CERTIFICATE_NAME = "caCert";
    private static final int KEEP_ALIVE_INTERVAL = 60;
    private static final int CONNECTION_TIMEOUT = 15;
    private final MqttEventListener mqttEventListener;
    private final MqttAndroidClient client;
    private final Context context;

    public MqttClientWrapper(MqttEventListener mqttEventListener,
                             Context context) {
        this.mqttEventListener = mqttEventListener;

        this.client = new MqttAndroidClient(context, URL, CLIENT_ID);
        this.client.setCallback(new MqttErrorCallback());

        this.context = context;
    }

    public void connect() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setConnectionTimeout(CONNECTION_TIMEOUT);
        mqttConnectOptions.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        mqttConnectOptions.setSocketFactory(getSocketFactory());

        SharedPreferences sharedPref = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String username = sharedPref.getString("Username", "");
        String password = sharedPref.getString("Password", "");
        Log.d("MqttClientWrapper", String.format("Username: %s; Password: %s", username, password));
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            IMqttToken mqttToken = client.connect(mqttConnectOptions);
            mqttToken.setActionCallback(mqttEventListener);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void publish(String topic, String message) {
        try {
            client.publish(
                    topic,
                    new MqttMessage(message.getBytes(StandardCharsets.UTF_8))
            );
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private SSLSocketFactory getSocketFactory() {
        try (InputStream inputStream = context.getResources().openRawResource(R.raw.root_ca)) {
            CertificateFactory cFactory = CertificateFactory.getInstance(X509_CERTIFICATE_TYPE);
            X509Certificate rootCa = (X509Certificate) cFactory.generateCertificate(inputStream);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            ks.setCertificateEntry(CERTIFYING_CENTER_CERTIFICATE_NAME, rootCa);
            tmf.init(ks);

            SSLContext ctx = SSLContext.getInstance(SSL_PROTOCOL_TYPE);
            ctx.init(null, tmf.getTrustManagers(), null);
            return ctx.getSocketFactory();
        } catch (IOException | CertificateException | NoSuchAlgorithmException |
                 KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private class MqttErrorCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            Log.e("MqttClientWrapper", "Connection has been lost", cause);
            mqttEventListener.stopHandling();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            // do nothing
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // do nothing
        }
    }
}
