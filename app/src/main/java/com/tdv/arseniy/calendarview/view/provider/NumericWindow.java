package com.tdv.arseniy.calendarview.view.provider;

import com.tdv.arseniy.calendarview.view.drawable.DrawableItemFactory;

import java.util.Locale;

/**
 * Created by arseniy on 28.08.16.
 */

public class NumericWindow implements IDataWindow {

    private int minimal;
    private int maximal;
    private int pivot;
    private DrawableItemFactory factory;
    private String format = "%d";

    public NumericWindow(int minimal, int maximal, DrawableItemFactory factory) {
        this.minimal = minimal;
        this.maximal = maximal;
        this.factory = factory;
        pivot = minimal;
    }

    public void setMaximal(int maximal) {
        this.maximal = maximal;
        if (pivot > maximal) {
            pivot = maximal;
        }
    }

    public void setPivot(int index) {
        pivot = index;
        if (pivot > maximal) {
            pivot = maximal;
        } else if (pivot < minimal) {
            pivot = minimal;
        }
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public Object receiveData(int i) {
        int ret = pivot + i;
        if (ret < minimal) {
            ret += maximal - minimal + 1;
        } else if (ret > maximal) {
            ret -= maximal - minimal + 1;
        }
        return factory.create(ret, format);
    }

    @Override
    public void shift(ShiftDirection direction) {
        switch (direction) {
            case BACKWARD: {
                if (--pivot < minimal) {
                    pivot = maximal;
                }
                break;
            }
            case FORWARD: {
                if (++pivot > maximal) {
                    pivot = minimal;
                }
                break;
            }
        }
    }

    @Override
    public void shift(ShiftDirection direction, int by) {
        switch (direction) {
            case BACKWARD: {
                pivot -= by;
                if (pivot < minimal) {
                    pivot = maximal - (minimal - pivot) + 1;
                }
                break;
            }
            case FORWARD: {
                pivot += by;
                if (pivot > maximal) {
                    pivot = minimal + (pivot - maximal) - 1;
                }
                break;
            }
        }
    }
}
