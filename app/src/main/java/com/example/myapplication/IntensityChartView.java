package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class IntensityChartView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Float> values = new ArrayList<>();

    private static final int MAX_POINTS = 50;

    public IntensityChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setColor(Color.rgb(52, 120, 246));
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        gridPaint.setColor(Color.rgb(225, 231, 241));
        gridPaint.setStrokeWidth(1.5f);

        fillPaint.setColor(Color.argb(35, 52, 120, 246));
        fillPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.rgb(120, 130, 150));
        textPaint.setTextSize(25f);
    }

    public void addValue(float value) {
        values.add(Math.min(value, 20f));

        if (values.size() > MAX_POINTS) {
            values.remove(0);
        }

        postInvalidate();
    }

    public void clear() {
        values.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = 12f;
        float right = getWidth() - 12f;
        float top = 15f;
        float bottom = getHeight() - 25f;

        float chartWidth = right - left;
        float chartHeight = bottom - top;

        // Đường lưới
        for (int i = 0; i <= 4; i++) {
            float y = top + chartHeight * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        canvas.drawText("20", left, top - 2, textPaint);
        canvas.drawText("0", left, bottom - 3, textPaint);

        if (values.size() < 2) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    "Đang chờ dữ liệu chuyển động...",
                    getWidth() / 2f,
                    getHeight() / 2f,
                    textPaint
            );
            textPaint.setTextAlign(Paint.Align.LEFT);
            return;
        }

        Path linePath = new Path();
        Path fillPath = new Path();

        float step = chartWidth / (MAX_POINTS - 1);

        for (int i = 0; i < values.size(); i++) {
            float x = left + (MAX_POINTS - values.size() + i) * step;

            float normalized = Math.min(values.get(i) / 20f, 1f);
            float y = bottom - normalized * chartHeight;

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        float lastX = left + (MAX_POINTS - 1) * step;
        fillPath.lineTo(lastX, bottom);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);
    }
}
