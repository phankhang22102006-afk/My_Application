package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity2 extends Activity
        implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    private TextView tvCount;
    private TextView tvBest;
    private TextView tvIntensity;
    private TextView tvStatus;

    private ProgressBar progressIntensity;

    private Button btnStart;
    private Button btnReset;

    private SharedPreferences preferences;

    private int shakeCount = 0;
    private int bestRecord = 0;

    private boolean isRunning = false;
    private boolean isRegistered = false;

    private final float[] gravity = new float[3];

    private static final float ALPHA = 0.85f;

    private static final float SHAKE_THRESHOLD = 2.5f;

    private static final float RESET_THRESHOLD = 1.3f;

    private static final long SHAKE_INTERVAL = 400;

    private boolean shakeArmed = true;

    private long lastShakeTime = 0;

    private float peakAcceleration = 0f;

    private static final String PREF_NAME = "ShakePreferences";
    private static final String KEY_BEST = "best_record";
    private IntensityChartView intensityChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        tvCount = findViewById(R.id.tvCount);
        tvBest = findViewById(R.id.tvBest);
        tvIntensity = findViewById(R.id.tvIntensity);
        tvStatus = findViewById(R.id.tvStatus);

        progressIntensity =
                findViewById(R.id.progressIntensity);
        intensityChart = findViewById(R.id.intensityChart);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);

        sensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
        );

        vibrator = (Vibrator)
                getSystemService(Context.VIBRATOR_SERVICE);
        preferences = getSharedPreferences(
                PREF_NAME,
                MODE_PRIVATE
        );

        bestRecord = preferences.getInt(KEY_BEST, 0);

        capNhatGiaoDien();

        if (accelerometer == null) {
            btnStart.setEnabled(false);

            tvStatus.setText(
                    "Thiết bị không hỗ trợ Accelerometer"
            );

            Toast.makeText(
                    this,
                    "Không tìm thấy cảm biến gia tốc",
                    Toast.LENGTH_LONG
            ).show();
        }

        btnStart.setOnClickListener(v -> {

            if (isRunning) {
                tamDung();
            } else {
                batDau();
            }
        });

        btnReset.setOnClickListener(v -> resetBoDem());
    }

    private void batDau() {

        if (accelerometer == null) {
            return;
        }

        isRunning = true;

        dangKyCamBien();

        btnStart.setText("Tạm dừng");

        tvStatus.setText("Đang theo dõi chuyển động");

        Toast.makeText(
                this,
                "Bắt đầu đo chuyển động",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void tamDung() {

        isRunning = false;

        huyDangKyCamBien();

        btnStart.setText("Bắt đầu");

        tvStatus.setText("Đã tạm dừng");
    }

    private void dangKyCamBien() {

        if (!isRegistered && accelerometer != null) {

            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
            );

            isRegistered = true;
        }
    }

    private void huyDangKyCamBien() {

        if (isRegistered) {

            sensorManager.unregisterListener(this);

            isRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isRunning) {
            dangKyCamBien();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        huyDangKyCamBien();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (!isRunning) {
            return;
        }

        if (event.sensor.getType()
                != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        gravity[0] = ALPHA * gravity[0]
                + (1 - ALPHA) * x;

        gravity[1] = ALPHA * gravity[1]
                + (1 - ALPHA) * y;

        gravity[2] = ALPHA * gravity[2]
                + (1 - ALPHA) * z;

        float linearX = x - gravity[0];
        float linearY = y - gravity[1];
        float linearZ = z - gravity[2];

        float acceleration = (float) Math.sqrt(
                linearX * linearX
                        + linearY * linearY
                        + linearZ * linearZ
        );

        capNhatCuongDo(acceleration);

        long currentTime = System.currentTimeMillis();
        peakAcceleration = Math.max(
                peakAcceleration,
                acceleration
        );

        if (acceleration < RESET_THRESHOLD) {
            shakeArmed = true;
            peakAcceleration = 0f;
        }

        if (acceleration >= SHAKE_THRESHOLD
                && shakeArmed
                && currentTime - lastShakeTime
                >= SHAKE_INTERVAL) {

            shakeArmed = false;

            lastShakeTime = currentTime;

            shakeCount++;
            if (shakeCount > bestRecord) {

                bestRecord = shakeCount;

                preferences.edit()
                        .putInt(KEY_BEST, bestRecord)
                        .apply();
            }

            hienThiMucDo(acceleration);

            phanHoiRung();

            capNhatGiaoDien();
        }
    }

    private void capNhatCuongDo(float acceleration) {

        int progress = Math.min(
                100,
                Math.round(acceleration * 5f)
        );

        progressIntensity.setProgress(progress);
        intensityChart.addValue(acceleration);

        if (acceleration < SHAKE_THRESHOLD) {

            tvIntensity.setText("Cường độ: Đang chờ");

        } else {

            tvIntensity.setText(String.format(
                    Locale.getDefault(),
                    "Cường độ: %.1f m/s²",
                    acceleration
            ));
        }
    }

    private void hienThiMucDo(float acceleration) {

        String mucDo;

        if (acceleration < 5f) {

            mucDo = "Nhẹ";

        } else if (acceleration < 9f) {

            mucDo = "Vừa";

        } else {

            mucDo = "Mạnh";
        }

        tvIntensity.setText(
                "Mức độ lắc: " + mucDo
        );
    }

    private void phanHoiRung() {

        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            35,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );

        } else {

            vibrator.vibrate(35);
        }
    }
    private void capNhatGiaoDien() {

        tvCount.setText(String.valueOf(shakeCount));

        tvBest.setText(
                "Kỷ lục: " + bestRecord
        );
    }
    private void resetBoDem() {

        shakeCount = 0;

        shakeArmed = true;

        peakAcceleration = 0f;

        lastShakeTime = 0;

        gravity[0] = 0f;
        gravity[1] = 0f;
        gravity[2] = 0f;

        progressIntensity.setProgress(0);
        intensityChart.clear();
        tvIntensity.setText("Cường độ: Chưa có");

        capNhatGiaoDien();

        tvStatus.setText(
                isRunning
                        ? "Đang theo dõi chuyển động"
                        : "Đã đặt lại bộ đếm"
        );

        Toast.makeText(
                this,
                "Đã đặt lại bộ đếm",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy
    ) {
    }
}
