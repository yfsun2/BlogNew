package com.syf.blognew.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ScaleImageView extends AppCompatImageView {

    private final Matrix mMatrix = new Matrix();
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    private float mBaseScale;
    private float mCurrentScale;
    private final float mMaxScale = 3.0f;

    private float mLastX, mLastY;
    private final int mTouchSlop;
    private boolean mIsDrag;

    public ScaleImageView(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::resetMatrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetMatrix();
    }

    private void resetMatrix() {
        Drawable d = getDrawable();
        if (d == null) return;

        float imgW = d.getIntrinsicWidth();
        float imgH = d.getIntrinsicHeight();
        float viewW = getWidth();
        float viewH = getHeight();

        mBaseScale = Math.min(viewW / imgW, viewH / imgH);
        mCurrentScale = mBaseScale;

        mMatrix.reset();
        mMatrix.setScale(mCurrentScale, mCurrentScale);

        float transX = (viewW - imgW * mCurrentScale) * 0.5f;
        float transY = (viewH - imgH * mCurrentScale) * 0.5f;
        mMatrix.postTranslate(transX, transY);

        setImageMatrix(mMatrix);
    }

    /**
     * 核心：限制图片边界，绝不拖出屏幕
     */
    private void fixMatrixBounds() {
        if (getDrawable() == null) return;

        RectF rect = new RectF(0, 0,
                getDrawable().getIntrinsicWidth(),
                getDrawable().getIntrinsicHeight());
        mMatrix.mapRect(rect); // 获取图片当前实际位置

        float viewW = getWidth();
        float viewH = getHeight();
        float dx = 0, dy = 0;

        // 水平边界限制
        if (rect.width() <= viewW) {
            dx = (viewW - rect.width()) * 0.5f - rect.left;
        } else if (rect.left > 0) {
            dx = -rect.left;
        } else if (rect.right < viewW) {
            dx = viewW - rect.right;
        }

        // 垂直边界限制
        if (rect.height() <= viewH) {
            dy = (viewH - rect.height()) * 0.5f - rect.top;
        } else if (rect.top > 0) {
            dy = -rect.top;
        } else if (rect.bottom < viewH) {
            dy = viewH - rect.bottom;
        }

        mMatrix.postTranslate(dx, dy);
        setImageMatrix(mMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewParent parent = getParent();
        if (parent != null) parent.requestDisallowInterceptTouchEvent(true);

        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        if (mScaleDetector.isInProgress()) {
            fixMatrixBounds(); // 缩放后自动贴边
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mIsDrag = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!mIsDrag) {
                    mIsDrag = Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop;
                }

                if (mIsDrag) {
                    mMatrix.postTranslate(dx, dy);
                    fixMatrixBounds(); // 拖动时强制锁定边界
                    mLastX = x;
                    mLastY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDrag = false;
                fixMatrixBounds(); // 松手自动回弹
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = mCurrentScale * scaleFactor;

            newScale = Math.max(mBaseScale, Math.min(newScale, mMaxScale));
            float realScale = newScale / mCurrentScale;
            mCurrentScale = newScale;

            mMatrix.postScale(realScale, realScale,
                    detector.getFocusX(), detector.getFocusY());
            fixMatrixBounds();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mCurrentScale > mBaseScale) {
                resetMatrix();
            } else {
                mCurrentScale = mMaxScale;
                mMatrix.postScale(mMaxScale / mBaseScale, mMaxScale / mBaseScale,
                        e.getX(), e.getY());
                fixMatrixBounds();
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