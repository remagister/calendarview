package com.tdv.arseniy.calendarview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;

import com.aigestudio.wheelpicker.widgets.WheelDatePicker;
import com.tdv.arseniy.calendarview.view.View2D;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View2D view = new View2D(this);
        //datePicker.setAtmospheric(true);
       // datePicker.setCurved(true);
        setContentView(view);
    }
}
