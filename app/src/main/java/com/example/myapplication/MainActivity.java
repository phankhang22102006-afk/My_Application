package com.example.myapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Surface;
import java.util.Locale;

public class MainActivity extends Activity
        implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor camBienGiaToc;
    private Sensor camBienTuTruong;

    private CompassDialView compassDial;
    private TextView tvGoc;
    private TextView tvHuong;

    private float[] giaToc = new float[3];
    private float[] tuTruong = new float[3];

    private boolean coGiaToc = false;
    private boolean coTuTruong = false;

    private float[] maTranXoay = new float[9];
    private float[] gocHuong = new float[3];
    private float[] maTranXoayTam = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassDial = findViewById(R.id.compassDial);
        tvGoc = findViewById(R.id.tvGoc);
        tvHuong = findViewById(R.id.tvHuong);
        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        camBienGiaToc = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        camBienTuTruong = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        if (camBienGiaToc == null || camBienTuTruong == null) {
            Toast.makeText(this,
                    "Điện thoại không có đủ cảm biến la bàn",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (camBienGiaToc != null) {
            sensorManager.registerListener(
                    this,
                    camBienGiaToc,
                    SensorManager.SENSOR_DELAY_UI);
        }

        if (camBienTuTruong != null) {
            sensorManager.registerListener(
                    this,
                    camBienTuTruong,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()
                == Sensor.TYPE_ACCELEROMETER) {

            giaToc = event.values.clone();
            coGiaToc = true;

        } else if (event.sensor.getType()
                == Sensor.TYPE_MAGNETIC_FIELD) {

            tuTruong = event.values.clone();
            coTuTruong = true;
        }

        if (!coGiaToc || !coTuTruong) {
            return;
        }

        boolean thanhCong = SensorManager.getRotationMatrix(
                maTranXoay,
                null,
                giaToc,
                tuTruong);

        if (!thanhCong) {
            return;
        }

        int manHinh = getWindowManager()
                .getDefaultDisplay().getRotation();

        boolean remapThanhCong = false;

        switch (manHinh) {
            case Surface.ROTATION_90:
                remapThanhCong = SensorManager.remapCoordinateSystem(
                        maTranXoay,
                        SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_X,
                        maTranXoayTam);
                break;

            case Surface.ROTATION_180:
                remapThanhCong = SensorManager.remapCoordinateSystem(
                        maTranXoay,
                        SensorManager.AXIS_MINUS_X,
                        SensorManager.AXIS_MINUS_Y,
                        maTranXoayTam);
                break;

            case Surface.ROTATION_270:
                remapThanhCong = SensorManager.remapCoordinateSystem(
                        maTranXoay,
                        SensorManager.AXIS_MINUS_Y,
                        SensorManager.AXIS_X,
                        maTranXoayTam);
                break;

            default:
                System.arraycopy(
                        maTranXoay, 0,
                        maTranXoayTam, 0,
                        maTranXoay.length);
                remapThanhCong = true;
                break;
        }

        if (!remapThanhCong) {
            return;
        }

        SensorManager.getOrientation(maTranXoayTam, gocHuong);

        float goc = (float) Math.toDegrees(gocHuong[0]);
        goc = (goc % 360 + 360) % 360;

        int gocHienThi = Math.round(goc) % 360;

        compassDial.setHeading(goc);

        tvGoc.setText(String.format(
                java.util.Locale.getDefault(),
                "%d°",
                gocHienThi));

        tvHuong.setText(layTenHuong(gocHienThi));



    }

    private String layTenHuong(int goc) {

        if (goc >= 338 || goc < 23) {
            return "Hướng Bắc";
        } else if (goc < 68) {
            return "Hướng Đông Bắc";
        } else if (goc < 113) {
            return "Hướng Đông";
        } else if (goc < 158) {
            return "Hướng Đông Nam";
        } else if (goc < 203) {
            return "Hướng Nam";
        } else if (goc < 248) {
            return "Hướng Tây Nam";
        } else if (goc < 293) {
            return "Hướng Tây";
        } else {
            return "Hướng Tây Bắc";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Khong can xu ly them trong ban co ban
    }
}
