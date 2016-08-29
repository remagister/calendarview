package com.tdv.arseniy.calendarview.view.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by arseniy on 28.08.16.
 */

public interface Item extends IMeasurable {
    void setPaint(Paint paint);
    Paint getPaint();
    Object getData();
    void setData(Object object);
    void draw(Canvas canvas, float x, float y);
}
