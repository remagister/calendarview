package com.tdv.arseniy.calendarview.view.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Locale;

/**
 * Created by arseniy on 28.08.16.
 */

public class DrawableItemFactory {

    private static class ItemImpl implements Item {
        private String string = "";
        private Object rawData;
        private Rect bounds = new Rect();
        private Paint textPaint;
        private String format;

        private ItemImpl(Object data, String format) {
            rawData = data;
            this.format = format;
            this.string = String.format(Locale.ENGLISH, format, rawData);
        }

        private void measureBounds(){
            textPaint.getTextBounds(string, 0, string.length(), bounds);
        }

        @Override
        public void draw(Canvas canvas, float x, float y) {
            canvas.drawText(string, x, y, textPaint);
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
            return rawData;
        }

        @Override
        public void setData(Object object) {
            rawData = object;
            this.string = String.format(Locale.ENGLISH, format, rawData);
            measureBounds();
        }
    }

    private Paint textPaint;

    public DrawableItemFactory(Paint fontPaint) {
        textPaint = fontPaint;
    }

    public Item create(Object data, String format){
        Item ret = new ItemImpl(data, format);
        ret.setPaint(textPaint);
        return ret;
    }

    public Item create(String data){
        Item ret = new ItemImpl(data, "%s");
        ret.setPaint(textPaint);
        return ret;
    }

    public Rect getSampleBounds(Rect outRect, String sample){
        textPaint.getTextBounds(sample, 0, sample.length(), outRect);
        return outRect;
    }
}
