package com.tdv.arseniy.calendarview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.view.drawable.AnimItemContainer;
import com.tdv.arseniy.calendarview.view.drawable.DrawableItemFactory;
import com.tdv.arseniy.calendarview.view.drawable.ItemContainer;
import com.tdv.arseniy.calendarview.view.provider.NumericWindow;

/**
 * Created by arseniy on 27.08.16.
 */

public class View2D extends View {

    private class Detector extends GestureDetector.SimpleOnGestureListener{

        private AnimItemContainer container;

        Detector(AnimItemContainer container) {
            this.container = container;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            container.starFlingWith(-velocityY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            container.scrollWith(-distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            container.stop();
            return true;
        }
    }

    public View2D(Context context) {
        super(context);
        init();
    }

    public View2D(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private AnimItemContainer container;
    private GestureDetector detector;

    private void init(){
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(52);
        DrawableItemFactory factory = new DrawableItemFactory(textPaint);
        NumericWindow window = new NumericWindow(1, 21, factory);
        window.setPivot(5);
        window.setFormat("%02d");
        container = new AnimItemContainer(window, new Scroller(getContext()));
        detector = new GestureDetector(getContext(),
                new Detector(container));
    }

    private static int DIM_GRAY = Color.argb(0x20, 0x40, 0x40, 0x40);
    LinearGradient grad = new LinearGradient(100, 100, 100, 500,
            new int[]{DIM_GRAY, Color.TRANSPARENT, Color.TRANSPARENT, DIM_GRAY},
            new float[]{0f, 0.3f, 0.7f, 1f}, Shader.TileMode.CLAMP);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        container.draw(canvas);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = container.getWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = container.getHeight() + getPaddingBottom() + getPaddingTop();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(desiredWidth, widthSize);
                break;
            default: case MeasureSpec.UNSPECIFIED:
                width = desiredWidth;
                break;
        }
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(desiredHeight, heightSize);
                break;
            default: case MeasureSpec.UNSPECIFIED:
                height = desiredHeight;
                break;
        }
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = detector.onTouchEvent(event);
        invalidate();
        return ret;
    }
}
