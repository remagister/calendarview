package com.tdv.arseniy.calendarview.view.provider;

/**
 * Created by arseniy on 28.08.16.
 */

public interface IDataWindow {
    
    Object receiveData(int i);
    void shift(ShiftDirection direction);
    void shift(ShiftDirection direction, int by);
}
