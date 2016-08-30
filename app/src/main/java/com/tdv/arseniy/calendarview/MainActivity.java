package com.tdv.arseniy.calendarview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.tdv.arseniy.calendarview.view.ScrollPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollPicker view = new ScrollPicker(this);
        //datePicker.setAtmospheric(true);
       // datePicker.setCurved(true);
        setContentView(view);
    }
}
