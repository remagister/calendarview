package com.tdv.arseniy.calendarview.view.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Created by arseniy on 28.08.16.
 */

public class DrawableItemFactory {

    private static class ItemImpl implements Item {
        private String data = "";
        private Rect bounds = new Rect();
        private Paint textPaint;

        private ItemImpl(String data) {
            this.data = data;
        }

        private void measureBounds(){
            textPaint.getTextBounds(data, 0, data.length(), bounds);
        }

        @Override
        public void draw(Canvas canvas, float x, float y) {
            canvas.drawText(data, x, y, textPaint);
        }

        @Override
        public float measureWidth() {
            return bounds.width();
        }

        @Override
        public float measureHeight() {
            return bounds.height();
        }

        @Override
        public void setPaint(Paint paint) {
            textPaint = paint;
            measureBounds();
        }

        @Override
        public Paint getPaint() {
            return textPaint;
        }

        @Override
        public Object getData() {
            return data;
        }

        @Override
        public void setData(Object object) {
            data = (String) object;
            measureBounds();
        }
    }

    private Paint textPaint;
    public DrawableItemFactory(Paint fontPaint) {
        textPaint = fontPaint;
    }

    public Item create(String data){
        Item ret = new ItemImpl(data);
        ret.setPaint(textPaint);
        return ret;
    }

    public void getSampleBounds(Rect rect, String sample){
        textPaint.getTextBounds(sample, 0, sample.length(), rect);
    }
}
