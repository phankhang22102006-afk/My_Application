package com.example.myapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity2 extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView tvStatus;
    private TextView tvCount;
    private TextView tvInstruction;

    private int shakeCount = 0;
    private long lastShakeTime = 0L;
    private long lastUpdateTime = 0L;

    private static final float SHAKE_THRESHOLD = 12.5f;
    private static final long SHAKE_COOLDOWN_MS = 700L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        tvStatus = findViewById(R.id.tvStatus);
        tvCount = findViewById(R.id.tvCount);
        tvInstruction = findViewById(R.id.tvInstruction);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            tvStatus.setText("Thiết bị không hỗ trợ cảm biến gia tốc");
            tvInstruction.setText("Ứng dụng này cần cảm biến Accelerometer để phát hiện lắc.");
        } else {
            tvStatus.setText("Sẵn sàng");
            tvInstruction.setText("Hãy cầm điện thoại và lắc nhẹ để thử!");
        }

        tvCount.setText(String.valueOf(shakeCount));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0] / SensorManager.GRAVITY_EARTH;
        float y = event.values[1] / SensorManager.GRAVITY_EARTH;
        float z = event.values[2] / SensorManager.GRAVITY_EARTH;

        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
        long now = SystemClock.elapsedRealtime();

        // Ignore tiny movements and avoid counting the same shake several times.
        if (acceleration > SHAKE_THRESHOLD / SensorManager.GRAVITY_EARTH
                && now - lastShakeTime > SHAKE_COOLDOWN_MS) {
            lastShakeTime = now;
            shakeCount++;

            tvCount.setText(String.valueOf(shakeCount));
            tvStatus.setText("Đã phát hiện lắc!");
            tvInstruction.setText(String.format(
                    Locale.getDefault(),
                    "Cường độ chuyển động: %.1f m/s²\nTiếp tục lắc để tăng bộ đếm.",
                    acceleration * SensorManager.GRAVITY_EARTH));
        } else if (now - lastUpdateTime > 1500L
                && now - lastShakeTime > 1500L) {
            lastUpdateTime = now;
            tvStatus.setText("Đang chờ lắc...");
            tvInstruction.setText("Cầm điện thoại chắc chắn rồi lắc nhẹ.");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No extra handling is needed for this basic demo.
    }
}
