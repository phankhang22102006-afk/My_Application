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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) * 0.78f;

        canvas.save();
        canvas.translate(cx, cy);

        // Vong ngoai
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(0, 0, radius, paint);

        // Xoay mat la ban theo huong dien thoai
        canvas.save();
        canvas.rotate(-heading);

        for (int degree = 0; degree < 360; degree += 3) {
            canvas.save();
            canvas.rotate(degree);

            float start;
            float end;

            if (degree % 30 == 0) {
                start = radius - 23;
                end = radius;
                paint.setStrokeWidth(2.5f);
            } else if (degree % 15 == 0) {
                start = radius - 16;
                end = radius;
                paint.setStrokeWidth(1.8f);
            } else {
                start = radius - 9;
                end = radius;
                paint.setStrokeWidth(1f);
            }

            paint.setColor(Color.WHITE);
            canvas.drawLine(0, -start, 0, -end, paint);
            canvas.restore();
        }

        // Cac moc goc
        for (int degree = 0; degree < 360; degree += 30) {
            canvas.save();
            canvas.rotate(degree);

            drawText(canvas, String.valueOf(degree),
                    0, -radius - 12, 13,
                    Color.LTGRAY, Paint.Align.CENTER);

            canvas.restore();
        }

        // Bon huong
        drawText(canvas, "B", 0, -radius + 52,
                25, Color.RED, Paint.Align.CENTER);

        drawText(canvas, "N", 0, radius - 30,
                23, Color.WHITE, Paint.Align.CENTER);

        drawText(canvas, "T", -radius + 30, 8,
                23, Color.WHITE, Paint.Align.CENTER);

        drawText(canvas, "Đ", radius - 30, 8,
                23, Color.WHITE, Paint.Align.CENTER);

        canvas.restore();

        // Tam la ban
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(35, 35, 35));
        canvas.drawCircle(0, 0, radius * 0.30f, paint);

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1f);
        canvas.drawLine(-radius * 0.5f, 0,
                radius * 0.5f, 0, paint);
        canvas.drawLine(0, -radius * 0.5f,
                0, radius * 0.5f, paint);

        canvas.restore();

        // Mui ten do co dinh o phia tren
        float tipY = cy - radius - 5;

        Path arrow = new Path();
        arrow.moveTo(cx, tipY);
        arrow.lineTo(cx - 10, tipY + 24);
        arrow.lineTo(cx + 10, tipY + 24);
        arrow.close();

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrow, paint);
    }
}
