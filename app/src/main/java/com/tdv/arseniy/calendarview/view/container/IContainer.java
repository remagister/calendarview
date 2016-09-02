package com.tdv.arseniy.calendarview.view.container;

import android.graphics.Canvas;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.view.drawable.IDrawable;
import com.tdv.arseniy.calendarview.view.drawable.IMeasurable;
import com.tdv.arseniy.calendarview.view.drawable.Item;
import com.tdv.arseniy.calendarview.view.provider.IDataWindow;
import com.tdv.arseniy.calendarview.view.provider.ShiftDirection;

/**
 * Created by arseniy on 30.08.16.
 */

public interface IContainer extends IMeasurable, Iterable<IDrawable>{

    void setDataWindow(IDataWindow window);
    void scroll(float dx);
    void fling(float dy, Scroller scroller);
    void setMaxItemSize(float width, float height);
    Item getCurrent();
    float getCentralUpperBound();
    void refresh();
    void setOrigin(float x, float y);
    void setOnItemChangedListener(OnItemChangedListener listener);
}
