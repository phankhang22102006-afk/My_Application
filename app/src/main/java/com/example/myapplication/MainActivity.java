package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor camBienXoay;
    private Sensor camBienTuTruong;

    private CompassDialView compassDial;
    private TextView tvGoc;
    private TextView tvHuong;
    private TextView tvAccuracy;
    private TextView tvSaved;

    private Button btnSaveHeading;
    private Button btnCalibration;
    private Button btnCancelHeading;

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
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvSaved = findViewById(R.id.tvSaved);

        btnSaveHeading = findViewById(R.id.btnSaveHeading);
        btnCalibration = findViewById(R.id.btnCalibration);
        btnCancelHeading = findViewById(R.id.btnCancelHeading);

        btnCancelHeading.setOnClickListener(v -> xoaHuongDaLuu());

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        camBienXoay = sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR);

        camBienTuTruong = sensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        if (camBienXoay == null) {
            Toast.makeText(
                    this,
                    "Điện thoại không hỗ trợ Rotation Vector",
                    Toast.LENGTH_LONG
            ).show();
        }

        if (camBienTuTruong == null) {
            tvAccuracy.setText("Độ chính xác: Không hỗ trợ");
        }

        hienThiHuongDaLuu();

        btnSaveHeading.setOnClickListener(v -> luuHuong());

        btnCalibration.setOnClickListener(v -> hienThiHuongDan());
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

        if (camBienTuTruong != null) {
            sensorManager.registerListener(
                    this,
                    camBienTuTruong,
                    SensorManager.SENSOR_DELAY_UI
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

        // Cập nhật độ chính xác cảm biến từ trường
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            capNhatDoChinhXac(event.accuracy);
            return;
        }

        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
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

        boolean thanhCong = SensorManager.remapCoordinateSystem(
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

        // Khởi tạo giá trị lần đầu
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

        int gocHienThi = Math.round(gocLamMuot) % 360;

        if (gocHienThi != gocHienThiCu) {

            gocHienThiCu = gocHienThi;

            tvGoc.setText(String.format(
                    Locale.getDefault(),
                    "%d°",
                    gocHienThi
            ));

            tvHuong.setText(layTenHuong(gocHienThi));
        }
    }

    // Hiển thị độ chính xác cảm biến từ trường
    private void capNhatDoChinhXac(int accuracy) {

        String trangThai;
        int mau;

        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                trangThai = "Độ chính xác: Cao";
                mau = 0xFF34C759; // Xanh lá
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                trangThai = "Độ chính xác: Trung bình";
                mau = 0xFFFFCC00; // Vàng
                break;

            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                trangThai = "Độ chính xác: Thấp - nên hiệu chuẩn";
                mau = 0xFFFF453A; // Đỏ
                break;

            case SensorManager.SENSOR_STATUS_UNRELIABLE:
            default:
                trangThai = "Độ chính xác: Không ổn định";
                mau = 0xFFFF453A; // Đỏ
                break;
        }

        tvAccuracy.setText(trangThai);
        tvAccuracy.setTextColor(mau);
    }

    // Lưu góc phương vị hiện tại
    private void luuHuong() {

        if (!daCoDuLieu || Float.isNaN(gocLamMuot)) {
            Toast.makeText(
                    this,
                    "Chưa có dữ liệu la bàn",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        int gocLuu = Math.round(gocLamMuot) % 360;

        getSharedPreferences("CompassData", MODE_PRIVATE)
                .edit()
                .putInt("saved_heading", gocLuu)
                .putString("saved_direction", layTenHuong(gocLuu))
                .apply();

        // Vẽ dấu xanh tại hướng vừa lưu
        compassDial.setSavedHeading(gocLuu);

        hienThiHuongDaLuu();

        Toast.makeText(
                this,
                "Đã lưu hướng hiện tại",
                Toast.LENGTH_SHORT
        ).show();
    }

    // Hiển thị hướng đã lưu
    private void hienThiHuongDaLuu() {

        int gocLuu = getSharedPreferences(
                "CompassData",
                MODE_PRIVATE
        ).getInt("saved_heading", -1);

        String huongLuu = getSharedPreferences(
                "CompassData",
                MODE_PRIVATE
        ).getString("saved_direction", "");

        if (gocLuu == -1) {

            tvSaved.setText("Hướng đã lưu: Chưa có");

            // Không có hướng lưu thì xóa dấu xanh
            compassDial.clearSavedHeading();

        } else {

            tvSaved.setText(
                    "Hướng đã lưu: " + gocLuu + "° - " + huongLuu
            );

            // Khôi phục dấu xanh khi mở lại ứng dụng
            compassDial.setSavedHeading(gocLuu);
        }
    }
    private void xoaHuongDaLuu() {
        getSharedPreferences("CompassData", MODE_PRIVATE)
                .edit()
                .remove("saved_heading")
                .remove("saved_direction")
                .apply();

        compassDial.clearSavedHeading();
        tvSaved.setText("Chưa lưu hướng");

        Toast.makeText(this, "Đã hủy hướng đã lưu", Toast.LENGTH_SHORT).show();
    }

    // Hướng dẫn hiệu chuẩn
    private void hienThiHuongDan() {

        new AlertDialog.Builder(this)
                .setTitle("Hướng dẫn hiệu chuẩn la bàn")
                .setMessage(
                        "Để cải thiện độ chính xác, bạn hãy:\n\n" +
                                "1. Đưa điện thoại ra xa các vật có từ tính.\n\n" +
                                "2. Di chuyển điện thoại theo hình số 8 vài lần.\n\n" +
                                "3. Xoay điện thoại nhẹ nhàng theo nhiều hướng.\n\n" +
                                "4. Đặt điện thoại nằm ngang và kiểm tra lại."
                )
                .setPositiveButton("Đã hiểu", null)
                .show();
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
        // Độ chính xác được cập nhật trong onSensorChanged()
    }
}