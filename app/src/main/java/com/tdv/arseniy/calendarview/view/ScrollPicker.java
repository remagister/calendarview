package com.tdv.arseniy.calendarview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.view.drawable.DrawableItemFactory;
import com.tdv.arseniy.calendarview.view.drawable.IDrawable;
import com.tdv.arseniy.calendarview.view.provider.IDataWindow;
import com.tdv.arseniy.calendarview.view.provider.NumericWindow;

/**
 * Created by arseniy on 27.08.16.
 */

public class ScrollPicker extends View {

    private class Detector extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            container.fling(velocityY, scroller);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            container.scroll(-distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            scroller.forceFinished(true);
            return true;
        }
    }

    public ScrollPicker(Context context) {
        super(context);
        init();
    }

    public ScrollPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private Scroller scroller;
    private IContainer container;
    private GestureDetector detector;
    private Rect renderTarget = new Rect();
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private void init(){
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(72);
        DrawableItemFactory factory = new DrawableItemFactory(textPaint);
        Rect bounds = factory.getSampleBounds(new Rect(), "32");
        NumericWindow window = new NumericWindow(1, 31, factory);
        window.setPivot(5);
        window.setFormat("%02d");
        container = new ItemContainer(window, bounds.width(),bounds.height());
        detector = new GestureDetector(getContext(), new Detector());
        scroller = new Scroller(getContext());
    }

    private static int DIM_GRAY = Color.argb(0x20, 0x40, 0x40, 0x40);
    LinearGradient grad = new LinearGradient(100, 100, 100, 500,
            new int[]{DIM_GRAY, Color.TRANSPARENT, Color.TRANSPARENT, DIM_GRAY},
            new float[]{0f, 0.3f, 0.7f, 1f}, Shader.TileMode.CLAMP);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(renderTarget);

        for (IDrawable drawable: container) {
            drawable.draw(canvas);
        }

        canvas.restore();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) container.measureWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = (int) container.measureHeight() + getPaddingBottom() + getPaddingTop();

        /*int widthMode = MeasureSpec.getMode(widthMeasureSpec);
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
                width = desiredWidth;
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
                height = desiredHeight;
                break;
            default: case MeasureSpec.UNSPECIFIED:
                height = desiredHeight;
                break;
        }*/
        renderTarget.set(0, 0, desiredWidth, desiredHeight);
        container.setOrigin(getPaddingLeft(), getPaddingTop());
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        final float lastY = scroller.getCurrY();
        boolean isContinued = scroller.computeScrollOffset();
        if(isContinued){
            container.scroll(scroller.getCurrY() - lastY);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = detector.onTouchEvent(event);
        ViewCompat.postInvalidateOnAnimation(this);
        return ret;
    }
}
