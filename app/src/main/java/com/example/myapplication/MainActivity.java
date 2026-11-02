package com.example.myapplication;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity
        implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor camBienXoay;

    private CompassDialView compassDial;
    private TextView tvGoc;
    private TextView tvHuong;

    private final float[] rotationVector = new float[5];
    private final float[] maTranXoay = new float[9];
    private final float[] maTranXoayTam = new float[9];
    private final float[] gocHuong = new float[3];

    private boolean daCoDuLieu = false;

    private float gocLamMuot = Float.NaN;
    private int gocHienThiCu = -1;

    private static final float HE_SO_LAM_MUOT = 0.12f;

    private static final float NGUONG_DAO_DONG = 1.2f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassDial = findViewById(R.id.compassDial);
        tvGoc = findViewById(R.id.tvGoc);
        tvHuong = findViewById(R.id.tvHuong);

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        camBienXoay = sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR);

        if (camBienXoay == null) {
            Toast.makeText(
                    this,
                    "Điện thoại không hỗ trợ Rotation Vector",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (camBienXoay != null) {
            sensorManager.registerListener(
                    this,
                    camBienXoay,
                    SensorManager.SENSOR_DELAY_GAME
            );
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

        if (event.sensor.getType()
                != Sensor.TYPE_ROTATION_VECTOR) {
            return;
        }

        System.arraycopy(
                event.values,
                0,
                rotationVector,
                0,
                Math.min(event.values.length, rotationVector.length)
        );

        SensorManager.getRotationMatrixFromVector(
                maTranXoay,
                rotationVector
        );

        int manHinh = getWindowManager()
                .getDefaultDisplay().getRotation();

        int axisX;
        int axisY;

        switch (manHinh) {

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;

            default:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;
        }

        boolean thanhCong =
                SensorManager.remapCoordinateSystem(
                        maTranXoay,
                        axisX,
                        axisY,
                        maTranXoayTam
                );

        if (!thanhCong) {
            return;
        }

        SensorManager.getOrientation(
                maTranXoayTam,
                gocHuong
        );

        float goc = (float) Math.toDegrees(gocHuong[0]);

        goc = (goc % 360f + 360f) % 360f;

        // Khoi tao gia tri lan dau
        if (!daCoDuLieu || Float.isNaN(gocLamMuot)) {
            gocLamMuot = goc;
            daCoDuLieu = true;

        } else {

            float chenhLech =
                    ((goc - gocLamMuot + 540f) % 360f) - 180f;

            if (Math.abs(chenhLech) < NGUONG_DAO_DONG) {
                return;
            }

            gocLamMuot =
                    (gocLamMuot
                            + chenhLech * HE_SO_LAM_MUOT
                            + 360f) % 360f;
        }

        compassDial.setHeading(gocLamMuot);

        int gocHienThi =
                Math.round(gocLamMuot) % 360;

        if (gocHienThi != gocHienThiCu) {

            gocHienThiCu = gocHienThi;

            tvGoc.setText(String.format(
                    Locale.getDefault(),
                    "%d°",
                    gocHienThi
            ));

            tvHuong.setText(
                    layTenHuong(gocHienThi)
            );
        }
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
    public void onAccuracyChanged(
            Sensor sensor,
            int accuracy
    ) {
    }
}
