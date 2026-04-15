package com.syf.blognew.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class ScaleImageView extends androidx.appcompat.widget.AppCompatImageView {

    private Matrix matrix = new Matrix();
    private PointF last = new PointF();
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private float scale = 1f;
    private float minScale = 1f;
    private float maxScale = 3f;

    private float baseScale = 1f;

    public ScaleImageView(Context context) {
        super(context);
        init();
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        resetMatrix();
    }

    // 核心：按原图比例自适应，不拉伸全屏
    private void resetMatrix() {
        Drawable d = getDrawable();
        if (d == null) return;

        float w = d.getIntrinsicWidth();
        float h = d.getIntrinsicHeight();
        if (w <= 0 || h <= 0) return;

        float viewW = getWidth();
        float viewH = getHeight();
        if (viewW <= 0 || viewH <= 0) return;

        float scaleW = viewW / w;
        float scaleH = viewH / h;

        // 自适应：按小的比例，保证完整显示原图
        baseScale = Math.min(scaleW, scaleH);
        scale = baseScale;

        matrix.reset();
        matrix.setScale(scale, scale);

        // 居中
        float transX = (viewW - w * scale) / 2f;
        float transY = (viewH - h * scale) / 2f;
        matrix.postTranslate(transX, transY);

        setImageMatrix(matrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetMatrix();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        PointF curr = new PointF(event.getX(), event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                last.set(curr);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = curr.x - last.x;
                float dy = curr.y - last.y;
                matrix.postTranslate(dx, dy);
                setImageMatrix(matrix);
                last.set(curr);
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = scale * scaleFactor;

            if (newScale >= baseScale && newScale <= maxScale) {
                scale = newScale;
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(matrix);
            }
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (scale > baseScale) {
                scale = baseScale;
                resetMatrix();
            } else {
                scale = maxScale;
                matrix.postScale(scale / baseScale, scale / baseScale, e.getX(), e.getY());
                setImageMatrix(matrix);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            performClick();
            return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}