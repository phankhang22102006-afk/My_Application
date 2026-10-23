package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class CompassDialView extends View {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float heading = 0;

    public CompassDialView(Context context) {
        super(context);
    }

    public CompassDialView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHeading(float degrees) {
        heading = degrees;
        invalidate();
    }

    private void drawText(Canvas c, String text,
                          float x, float y, float size,
                          int color, Paint.Align align) {
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(align);
        paint.setStyle(Paint.Style.FILL);
        c.drawText(text, x, y, paint);
    }


    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        float radius = Math.min(
                getWidth() * 0.46f,
                getHeight() * 0.43f
        );

        canvas.save();
        canvas.translate(cx, cy);

        // Vong ngoai
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, radius, paint);

        // Xoay mat la ban
        canvas.save();
        canvas.rotate(-heading);

        // Vach chia do
        for (int degree = 0; degree < 360; degree += 3) {
            canvas.save();
            canvas.rotate(degree);

            float start;
            float width;

            if (degree % 30 == 0) {
                start = radius - 30;
                width = 3.5f;
            } else if (degree % 15 == 0) {
                start = radius - 22;
                width = 2.5f;
            } else {
                start = radius - 13;
                width = 1.5f;
            }

            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(width);
            canvas.drawLine(0, -start, 0, -radius, paint);

            canvas.restore();
        }

        // So goc lon hon
        for (int degree = 0; degree < 360; degree += 30) {
            canvas.save();
            canvas.rotate(degree);

            drawText(canvas, String.valueOf(degree),
                    0, -radius - 24, 32,
                    Color.LTGRAY, Paint.Align.CENTER);

            canvas.restore();
        }

        // Bon huong lon va ro

        // Bon huong that lon
        drawText(canvas, "B", 0, -radius + 85,
                sp(30), Color.RED, Paint.Align.CENTER);

        drawText(canvas, "N", 0, radius - 50,
                sp(30), Color.WHITE, Paint.Align.CENTER);

        drawText(canvas, "T", -radius + 55, 15,
                sp(30), Color.WHITE, Paint.Align.CENTER);

        drawText(canvas, "Đ", radius - 55, 15,
                sp(30), Color.WHITE, Paint.Align.CENTER);

        canvas.restore();

        // Tam la ban
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(35, 35, 35));
        canvas.drawCircle(0, 0, radius * 0.25f, paint);

        // Duong tam
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1.5f);

        canvas.drawLine(-radius * 0.5f, 0,
                radius * 0.5f, 0, paint);

        canvas.drawLine(0, -radius * 0.5f,
                0, radius * 0.5f, paint);

        canvas.restore();

        // Mui ten do co dinh
        float tipY = cy - radius - 3;

        Path arrow = new Path();
        arrow.moveTo(cx, tipY);
        arrow.lineTo(cx - 14, tipY + 30);
        arrow.lineTo(cx + 14, tipY + 30);
        arrow.close();

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrow, paint);
    }
}
