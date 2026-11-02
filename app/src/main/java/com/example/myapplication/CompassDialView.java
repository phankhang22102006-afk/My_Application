package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class CompassDialView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private float heading = 0f;

    // Góc hướng đã lưu
    private float savedHeading = Float.NaN;

    public CompassDialView(Context context) {
        super(context);
    }

    public CompassDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHeading(float degrees) {
        heading = (degrees % 360f + 360f) % 360f;
        invalidate();
    }

    // Đặt hướng đã lưu để vẽ dấu xanh
    public void setSavedHeading(float degrees) {
        savedHeading = (degrees % 360f + 360f) % 360f;
        invalidate();
    }

    // Xóa dấu hướng đã lưu
    public void clearSavedHeading() {
        savedHeading = Float.NaN;
        invalidate();
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float y,
            float size,
            int color
    ) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(text, x, y, paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        float radius = Math.min(
                getWidth(),
                getHeight()
        ) * 0.43f;

        canvas.save();
        canvas.translate(cx, cy);

        // Vòng tròn bên ngoài
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2.5f);
        canvas.drawCircle(0, 0, radius, paint);

        // Các vạch chia độ xoay theo hướng điện thoại
        canvas.save();
        canvas.rotate(-heading);

        for (int degree = 0; degree < 360; degree += 3) {
            float start;
            float stroke;

            if (degree % 30 == 0) {
                start = radius * 0.88f;
                stroke = 3.5f;
            } else if (degree % 10 == 0) {
                start = radius * 0.92f;
                stroke = 2f;
            } else {
                start = radius * 0.965f;
                stroke = 1.3f;
            }

            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(stroke);

            canvas.save();
            canvas.rotate(degree);

            canvas.drawLine(
                    0, -start,
                    0, -radius,
                    paint
            );

            canvas.restore();
        }

        canvas.restore();

        // Dấu xanh đánh dấu hướng đã lưu
        if (!Float.isNaN(savedHeading)) {

            canvas.save();

            // Đặt dấu theo hướng đã lưu so với hướng hiện tại
            canvas.rotate(savedHeading - heading);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.rgb(0, 200, 255));
            paint.setStrokeWidth(6f);
            paint.setStrokeCap(Paint.Cap.ROUND);

            // Vạch đánh dấu trên vòng la bàn
            canvas.drawLine(
                    0,
                    -radius * 0.78f,
                    0,
                    -radius * 0.99f,
                    paint
            );

            // Chấm tròn ở đầu trong của dấu
            paint.setStyle(Paint.Style.FILL);

            canvas.drawCircle(
                    0,
                    -radius * 0.78f,
                    7f,
                    paint
            );

            canvas.restore();
        }

        // Số độ luôn giữ đúng chiều đọc
        float soRadius = radius * 0.79f;
        float soSize = radius * 0.085f;

        for (int degree = 0; degree < 360; degree += 30) {
            double rad = Math.toRadians(degree - heading);

            float x = (float) Math.sin(rad) * soRadius;
            float y = -(float) Math.cos(rad) * soRadius;

            drawText(
                    canvas,
                    String.valueOf(degree),
                    x,
                    y + soSize * 0.35f,
                    soSize,
                    Color.LTGRAY
            );
        }

        // Bốn hướng lớn
        float huongRadius = radius * 0.61f;
        float chuSize = radius * 0.19f;

        veChuHuong(canvas, "B", 0, huongRadius,
                chuSize, Color.RED);

        veChuHuong(canvas, "Đ", 90, huongRadius,
                chuSize, Color.WHITE);

        veChuHuong(canvas, "N", 180, huongRadius,
                chuSize, Color.WHITE);

        veChuHuong(canvas, "T", 270, huongRadius,
                chuSize, Color.WHITE);

        // Tâm la bàn
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(38, 38, 38));
        canvas.drawCircle(0, 0, radius * 0.16f, paint);

        // Đường tâm
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(95, 95, 95));
        paint.setStrokeWidth(1.5f);

        canvas.drawLine(
                -radius * 0.45f, 0,
                radius * 0.45f, 0,
                paint
        );

        canvas.drawLine(
                0, -radius * 0.45f,
                0, radius * 0.45f,
                paint
        );

        canvas.restore();

        // Mũi tên đỏ cố định phía trên
        float tipY = cy - radius - 2f;

        Path arrow = new Path();
        arrow.moveTo(cx, tipY);
        arrow.lineTo(cx - 18f, tipY + 34f);
        arrow.lineTo(cx + 18f, tipY + 34f);
        arrow.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        canvas.drawPath(arrow, paint);
    }

    private void veChuHuong(
            Canvas canvas,
            String text,
            int degree,
            float r,
            float size,
            int color
    ) {
        double rad = Math.toRadians(degree - heading);

        float x = (float) Math.sin(rad) * r;
        float y = -(float) Math.cos(rad) * r;

        drawText(
                canvas,
                text,
                x,
                y + size * 0.35f,
                size,
                color
        );
    }
}