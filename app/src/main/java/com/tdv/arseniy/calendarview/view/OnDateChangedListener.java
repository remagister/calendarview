package com.tdv.arseniy.calendarview.view;

import android.view.View;

import java.util.Date;

/**
 * Created by arseniy on 05.09.16.
 */

public interface OnDateChangedListener {
    public void OnDateChanged(View sender, Date from, Date to);
}
