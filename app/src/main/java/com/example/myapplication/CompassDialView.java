package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Typeface;

public class CompassDialView extends View {

    private final Paint paint =
            new Paint(Paint.ANTI_ALIAS_FLAG);

    private float heading = 0f;

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

        // Vong tron ben ngoai
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2.5f);
        canvas.drawCircle(0, 0, radius, paint);

        // Cac vach chia do xoay theo huong dien thoai
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

        // So do lon, giu chu dung de doc de hon
        float soRadius = radius * 0.79f;
        float soSize = radius * 0.085f;

        for (int degree = 0; degree < 360; degree += 30) {
            double rad = Math.toRadians(degree - heading);

            float x = (float) Math.sin(rad) * soRadius;
            float y = -(float) Math.cos(rad) * soRadius;

            drawText(
                    canvas,
                    String.valueOf(degree),
                    x, y + soSize * 0.35f,
                    soSize,
                    Color.LTGRAY
            );
        }

        // Bon huong lon, ro
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

        // Tam la ban
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(38, 38, 38));
        canvas.drawCircle(0, 0, radius * 0.16f, paint);

        // Duong tam
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.rgb(95, 95, 95));
        paint.setStrokeWidth(1.5f);

        canvas.drawLine(
                -radius * 0.45f, 0,
                radius * 0.45f, 0, paint
        );

        canvas.drawLine(
                0, -radius * 0.45f,
                0, radius * 0.45f, paint
        );

        canvas.restore();

        // Mui ten do co dinh phia tren
        // Mui ten do lon va ro hon
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
