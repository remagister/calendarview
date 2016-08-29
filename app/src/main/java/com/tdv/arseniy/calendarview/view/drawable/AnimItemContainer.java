package com.tdv.arseniy.calendarview.view.drawable;

import android.graphics.Canvas;
import android.util.Log;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.view.provider.IDataWindow;

/**
 * Created by arseniy on 29.08.16.
 */

public class AnimItemContainer implements IDrawable{

    private static final float MAX_FORCE = 5000f;

    private ItemContainer container;
    private Scroller scroller;
    private float lastY;
    private float direction;

    public AnimItemContainer(IDataWindow window, Scroller scroller){
        this(new ItemContainer(window), scroller);
    }

    public AnimItemContainer(ItemContainer container, Scroller scroller) {
        this.container = container;
        this.scroller = scroller;
        scroller.setFriction(100f);
    }

    public void starFlingWith(float force){
        float absForce = Math.abs(force);
        direction = Math.signum(force);
        if(absForce > MAX_FORCE){
            absForce = MAX_FORCE;
        }
        scroller.fling(0, 0, 0, (int)absForce, 0, 0, 0, (int)MAX_FORCE * 5);
        lastY = scroller.getCurrY();
        Log.d("FLING", String.format("started with: force=%f , lastY=%f", force, lastY));
    }

    public void scrollWith(float offset){
        container.scroll(offset);
    }

    public void stop(){
        if(!scroller.isFinished()){
            scroller.forceFinished(true);
        }
    }

    public int getWidth(){
        return container.getWidth();
    }

    public int getHeight(){
        return container.getHeight();
    }

    @Override
    public void draw(Canvas canvas) {
        if(scroller.computeScrollOffset()){
            final float currY = scroller.getCurrY();
            container.scroll((lastY - currY) * direction);
            lastY = currY;
        }
        container.draw(canvas);
    }

}
