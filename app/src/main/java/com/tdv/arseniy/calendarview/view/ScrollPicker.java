package com.tdv.arseniy.calendarview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
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
import com.tdv.arseniy.calendarview.view.util.AttributeBind;
import com.tdv.arseniy.calendarview.view.util.AttributeType;
import com.tdv.arseniy.calendarview.view.util.AttributeUtil;

import java.util.Calendar;

/**
 * Created by arseniy on 27.08.16.
 */

public class ScrollPicker extends View {

    private class Detector extends GestureDetector.SimpleOnGestureListener {

        Scroller scroller;
        IContainer container;

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
            if (year.hit(e.getX(), e.getY())) {
                scroller = yearScroller;
                container = year;
            } else {
                if (month.hit(e.getX(), e.getY())) {
                    scroller = monthScroller;
                    container = month;
                } else {
                    scroller = dayScroller;
                    container = day;
                }
            }
            scroller.forceFinished(true);
            return true;
        }
    }

    // ============== internal members ==============

    public static final int MIN_YEAR = 1900;
    public static final int MAX_YEAR = 2100;

    @AttributeBind(id = R.styleable.ScrollPicker_textSize, type = AttributeType.INTEGER)
    int textSize = 64;
    @AttributeBind(id = R.styleable.ScrollPicker_textColor, type = AttributeType.COLOR)
    int textColor = Color.BLACK;
    @AttributeBind(id = R.styleable.ScrollPicker_backgroundColor, type = AttributeType.COLOR)
    int backgroundColor = Color.TRANSPARENT;
    @AttributeBind(id = R.styleable.ScrollPicker_day, type = AttributeType.INTEGER)
    int mDay;
    @AttributeBind(id = R.styleable.ScrollPicker_month, type = AttributeType.INTEGER)
    int mMonth;
    @AttributeBind(id = R.styleable.ScrollPicker_year, type = AttributeType.INTEGER)
    int mYear;
    @AttributeBind(id = R.styleable.ScrollPicker_spacing, type = AttributeType.DIMENSION)
    int spacing = 10;
    @AttributeBind(id = R.styleable.ScrollPicker_labelSize, type = AttributeType.INTEGER)
    int labelSize = 42;

    private Scroller yearScroller;
    private Scroller monthScroller;
    private Scroller dayScroller;
    private IContainer year;
    private IContainer month;
    private IContainer day;
    private GestureDetector detector;
    private Rect renderTarget = new Rect();
    private String monthName;
    private String dayName;
    private String yearName;

    public ScrollPicker(Context context) {
        super(context);
        initCalendar();
        init();
    }

    public ScrollPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCalendar();
        AttributeUtil.resolve(this, attrs, R.styleable.ScrollPicker);
        mMonth++;
        init();
    }

    private void initCalendar() {
        Calendar current = Calendar.getInstance();
        mDay = current.get(Calendar.DAY_OF_MONTH) + 1;
        mYear = current.get(Calendar.YEAR);
        mMonth = current.get(Calendar.MONTH) + 1;
    }

    private void init() {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(textSize);

        DrawableItemFactory factory = new DrawableItemFactory(textPaint);
        Rect bounds4 = factory.getSampleBounds(new Rect(), "2222");
        Rect bounds2 = factory.getSampleBounds(new Rect(), "22");

        NumericWindow yearsWindow = new NumericWindow(MIN_YEAR, MAX_YEAR, factory);
        yearsWindow.setPivot(mYear);
        yearsWindow.setFormat("%04d");
        year = new ItemContainer(yearsWindow, bounds4.width(), bounds4.height());
        year.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                mYear = (int) to;
            }
        });
        yearName = getContext().getString(R.string.picker_year_name);
        yearScroller = new Scroller(getContext());

        NumericWindow monthWindow = new NumericWindow(1, 12, factory);
        monthWindow.setPivot(mMonth);
        monthWindow.setFormat("%02d");
        month = new ItemContainer(monthWindow, bounds2.width(), bounds2.height());
        monthName = getContext().getString(R.string.picker_month_name);
        monthScroller = new Scroller(getContext());

        NumericWindow dayWindow = new NumericWindow(1, 31, factory);
        dayWindow.setPivot(mDay);
        dayWindow.setFormat("%02d");
        day = new ItemContainer(dayWindow, bounds2.width(), bounds2.height());
        dayName = getContext().getString(R.string.picker_day_name);
        dayScroller = new Scroller(getContext());

        detector = new GestureDetector(getContext(), new Detector());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(renderTarget);

        for (IDrawable drawable : year) {
            drawable.draw(canvas);
        }
        for (IDrawable drawable : month) {
            drawable.draw(canvas);
        }
        for (IDrawable drawable : day) {
            drawable.draw(canvas);
        }

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getPaddingLeft() + getPaddingRight() +
                (int) year.measureWidth() + spacing +
                (int) month.measureWidth() + spacing +
                (int) day.measureWidth();

        int desiredHeight = (int) year.measureHeight() + getPaddingBottom() + getPaddingTop();

        renderTarget.set(getPaddingLeft(), getPaddingTop(), desiredWidth, desiredHeight);
        year.setOrigin(getPaddingLeft(), getPaddingTop());
        month.setOrigin(getPaddingLeft() + year.measureWidth() + spacing, getPaddingTop());
        day.setOrigin(getPaddingLeft() + year.measureWidth() +
                spacing + month.measureWidth() + spacing, getPaddingTop());
        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        final float lastYear = yearScroller.getCurrY();
        final float lastMonth = monthScroller.getCurrY();
        final float lastDay = dayScroller.getCurrY();

        boolean isContinued = false;
        if (yearScroller.computeScrollOffset()) {
            year.scroll(yearScroller.getCurrY() - lastYear);
            isContinued = true;
        }
        if (monthScroller.computeScrollOffset()) {
            month.scroll(monthScroller.getCurrY() - lastMonth);
            isContinued = true;
        }
        if (dayScroller.computeScrollOffset()) {
            day.scroll(dayScroller.getCurrY() - lastDay);
            isContinued = true;
        }
        if (isContinued) {
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
