package com.tdv.arseniy.calendarview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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
import com.tdv.arseniy.calendarview.view.util.AttributeBind;
import com.tdv.arseniy.calendarview.view.util.AttributeType;
import com.tdv.arseniy.calendarview.view.util.AttributeUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
            scroller.fling(0, 0, 0, 0, 0, 0, 0, 0);
            scroller.computeScrollOffset();
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
    @AttributeBind(id = R.styleable.ScrollPicker_labelColor, type = AttributeType.COLOR)
    int labelColor = Color.LTGRAY;
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
    int labelSize = 36;
    @AttributeBind(id = R.styleable.ScrollPicker_visibleItems, type = AttributeType.INTEGER)
    int visibleItems = 3;

    private Calendar myCalendar;
    private Paint labelPaint;
    private Scroller yearScroller;
    private Scroller monthScroller;
    private Scroller dayScroller;
    private IContainer year;
    private NumericWindow yearWindow;
    private IContainer month;
    private NumericWindow monthWindow;
    private IContainer day;
    private NumericWindow dayWindow;
    private GestureDetector detector;
    private String monthName;
    private String dayName;
    private String yearName;
    private Rect labelBounds = new Rect();
    private Point dayPosition = new Point();
    private Point monthPosition = new Point();
    private Point yearPosition = new Point();
    private OnDateChangedListener listener;

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
        mDay = current.get(Calendar.DAY_OF_MONTH);
        mYear = current.get(Calendar.YEAR);
        mMonth = current.get(Calendar.MONTH);
    }

    private void sendDate(Date old){
        if (listener != null) {
            listener.OnDateChanged(this, old, myCalendar.getTime());
        }
    }

    private void init() {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(textSize);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(labelColor);
        labelPaint.setTypeface(Typeface.DEFAULT);
        labelPaint.setTextSize(labelSize);

        DrawableItemFactory factory = new DrawableItemFactory(textPaint);
        Rect bounds4 = factory.getSampleBounds(new Rect(), "2222");
        Rect bounds2 = factory.getSampleBounds(new Rect(), "22");

        yearWindow = new NumericWindow(MIN_YEAR, MAX_YEAR, factory);
        yearWindow.setPivot(mYear);
        yearWindow.setFormat("%04d");
        year = new ItemContainer(bounds4.width(), bounds4.height(), visibleItems);
        year.setDataWindow(yearWindow);
        year.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                mYear = (int) to;
            }
        });
        yearName = getContext().getString(R.string.picker_year_name);
        yearScroller = new Scroller(getContext());

        monthWindow = new NumericWindow(1, 12, factory);
        monthWindow.setPivot(mMonth);
        monthWindow.setFormat("%02d");
        month = new ItemContainer(bounds2.width(), bounds2.height(), visibleItems);
        month.setDataWindow(monthWindow);
        monthName = getContext().getString(R.string.picker_month_name);
        monthScroller = new Scroller(getContext());

        myCalendar = new GregorianCalendar(mYear, mMonth, 1);

        dayWindow = new NumericWindow(1, myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH), factory);
        dayWindow.setPivot(mDay);
        dayWindow.setFormat("%02d");
        day = new ItemContainer(bounds2.width(), bounds2.height(), visibleItems);
        day.setDataWindow(dayWindow);
        dayName = getContext().getString(R.string.picker_day_name);
        dayScroller = new Scroller(getContext());

        year.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                mYear = (int) to;
                Date old = myCalendar.getTime();
                myCalendar.set(Calendar.YEAR, mYear);
                dayWindow.setMaximal(myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                day.refresh();
                sendDate(old);
            }
        });

        month.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                mMonth = (int) to;
                Date old = myCalendar.getTime();
                myCalendar.set(Calendar.MONTH, mMonth);
                dayWindow.setMaximal(myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                day.refresh();
                sendDate(old);
            }
        });

        day.setOnItemChangedListener(new OnItemChangedListener() {
            @Override
            public void onItemChanged(IContainer sender, Object from, Object to) {
                mDay = (int) to;
                Date old = myCalendar.getTime();
                sendDate(old);
            }
        });

        detector = new GestureDetector(getContext(), new Detector());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (IDrawable drawable : year) {
            drawable.draw(canvas);
        }
        canvas.drawText(yearName, yearPosition.x, yearPosition.y, labelPaint);

        for (IDrawable drawable : month) {
            drawable.draw(canvas);
        }
        canvas.drawText(monthName, monthPosition.x, monthPosition.y, labelPaint);

        for (IDrawable drawable : day) {
            drawable.draw(canvas);
        }
        canvas.drawText(dayName, dayPosition.x, dayPosition.y, labelPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int yearLabelWid = (int) labelPaint.measureText(yearName);
        final int monthLabelWid = (int) labelPaint.measureText(monthName);
        final int dayLabelWid = (int) labelPaint.measureText(dayName);

        int desiredWidth = getPaddingLeft() + getPaddingRight() +
                (int) year.measureWidth() + yearLabelWid +
                (int) month.measureWidth() + monthLabelWid +
                (int) day.measureWidth() + dayLabelWid +
                (spacing * 5);

        int desiredHeight = (int) year.measureHeight() + getPaddingBottom() + getPaddingTop();

        year.setOrigin(getPaddingLeft(), getPaddingTop());
        labelPaint.getTextBounds(yearName, 0, yearName.length(), labelBounds);
        yearPosition.set(getPaddingLeft() + (int) year.measureWidth() + spacing,
                desiredHeight / 2 + labelBounds.height() / 2);

        final int monthX = yearPosition.x + yearLabelWid + spacing;
        month.setOrigin(monthX, getPaddingTop());
        labelPaint.getTextBounds(monthName, 0, monthName.length(), labelBounds);
        monthPosition.set(monthX + (int) month.measureWidth() + spacing,
                desiredHeight / 2 + labelBounds.height() / 2);

        final int dayX = monthPosition.x + monthLabelWid + spacing;
        day.setOrigin(dayX, getPaddingTop());
        labelPaint.getTextBounds(dayName, 0, dayName.length(), labelBounds);
        dayPosition.set(dayX + (int) day.measureWidth() + spacing,
                desiredHeight / 2 + labelBounds.height() / 2);


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

    public void setDate(int year, int month, int day) {
        mYear = year;
        mMonth = month;
        mDay = day;
        Date old = myCalendar.getTime();
        myCalendar.set(year, month, day);
        yearWindow.setPivot(year);
        monthWindow.setPivot(month);
        dayWindow.setMaximal(myCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        dayWindow.setPivot(day);

        this.year.refresh();
        this.month.refresh();
        this.day.refresh();

        sendDate(old);

    }

    public Date getDate() {
        return myCalendar.getTime();
    }

    public void setOnDateChangedListener(OnDateChangedListener listener){
        this.listener = listener;
    }
}
