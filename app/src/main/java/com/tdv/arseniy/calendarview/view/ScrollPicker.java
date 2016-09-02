package com.tdv.arseniy.calendarview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.R;
import com.tdv.arseniy.calendarview.view.container.IContainer;
import com.tdv.arseniy.calendarview.view.container.ItemContainer;
import com.tdv.arseniy.calendarview.view.container.OnItemChangedListener;
import com.tdv.arseniy.calendarview.view.drawable.DrawableItemFactory;
import com.tdv.arseniy.calendarview.view.drawable.IDrawable;
import com.tdv.arseniy.calendarview.view.provider.NumericWindow;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by arseniy on 27.08.16.
 */

public class ScrollPicker extends View {

    private class Attributes {
        float textSize;
        int textColor;
        int backgroundColor;
        int day;
        int month;
        int year;

        public Attributes(){
            textSize = 64;
            textColor = Color.BLACK;
            backgroundColor = Color.TRANSPARENT;
            Calendar current = Calendar.getInstance();
            day = current.get(Calendar.DAY_OF_MONTH) + 1;
            year = current.get(Calendar.YEAR);
            month = current.get(Calendar.MONTH) + 1;
        }

        public Attributes(TypedArray array) {
            textSize = array.getInt(R.styleable.ScrollPicker_textSize, 64);
            textColor = array.getColor(R.styleable.ScrollPicker_textColor, Color.BLACK);
            backgroundColor = array.getColor(R.styleable.ScrollPicker_backgroundColor, Color.TRANSPARENT);
            Calendar current = Calendar.getInstance();
            day = array.getInt(R.styleable.ScrollPicker_day, current.get(Calendar.DAY_OF_MONTH) + 1);
            year = array.getInt(R.styleable.ScrollPicker_year, current.get(Calendar.YEAR));
            month = array.getInt(R.styleable.ScrollPicker_month, current.get(Calendar.MONTH)) + 1;
        }

        float getTextSize() {
            return textSize;
        }

        int getTextColor() {
            return textColor;
        }

        int getBackgroundColor() {
            return backgroundColor;
        }

        int getDay() {
            return day;
        }

        int getMonth() {
            return month;
        }

        int getYear() {
            return year;
        }

        void setDay(int day) {
            this.day = day;
        }

        void setMonth(int month) {
            this.month = month;
        }

        void setYear(int year) {
            this.year = year;
        }
    }

    private class Detector extends GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            year.fling(velocityY, scroller);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            year.scroll(-distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            scroller.forceFinished(true);
            return true;
        }
    }

    // ============== internal members ==============

    private Scroller scroller;
    private IContainer year;
    private IContainer month;
    private IContainer day;
    private GestureDetector detector;
    private Rect renderTarget = new Rect();
    private Attributes attributes;

    public ScrollPicker(Context context) {
        super(context);
        init();
    }

    public ScrollPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = null;
        try {
            array = getContext().obtainStyledAttributes(attrs, R.styleable.ScrollPicker, 0, 0);
            attributes = new Attributes(array);
        }
        finally {
            if(array != null) {
                array.recycle();
            }
        }

        init();
    }

    private void init(){
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(attributes.getTextColor());
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(attributes.getTextSize());
        DrawableItemFactory factory = new DrawableItemFactory(textPaint);
        Rect bounds = factory.getSampleBounds(new Rect(), "2222");
        NumericWindow window = new NumericWindow(1900, 2100, factory);
        window.setPivot(attributes.getYear());
        window.setFormat("%04d");
        year = new ItemContainer(window, bounds.width(),bounds.height());
        year.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                attributes.setYear((int) to);
            }
        });
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

        for (IDrawable drawable: year) {
            drawable.draw(canvas);
        }

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) year.measureWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = (int) year.measureHeight() + getPaddingBottom() + getPaddingTop();

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
        year.setOrigin(getPaddingLeft(), getPaddingTop());
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        final float lastY = scroller.getCurrY();
        boolean isContinued = scroller.computeScrollOffset();
        if(isContinued){
            year.scroll(scroller.getCurrY() - lastY);
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
