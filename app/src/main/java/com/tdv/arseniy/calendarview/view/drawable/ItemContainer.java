package com.tdv.arseniy.calendarview.view.drawable;

import android.graphics.Canvas;
import android.graphics.RectF;

import com.tdv.arseniy.calendarview.view.provider.IDataWindow;
import com.tdv.arseniy.calendarview.view.provider.ShiftDirection;

import java.util.LinkedList;

/**
 * Created by arseniy on 28.08.16.
 */

public class ItemContainer implements IDrawable {

    private class Crate implements IDrawable {
        private Item item;
        private float offset;
        private float padding;
        private float doublePadding;

        private void calculate() {
            doublePadding = padding * 2f;
            if (!sizeAssigned) {
                setMaximalItemSize(item.measureWidth() + doublePadding,
                        item.measureHeight() + doublePadding);
            }
        }

        Crate(Item item, float padding, int index) {
            this.item = item;
            this.padding = padding;
            calculate();
            offset = crateH * index;
        }

        float getOffset() {
            return offset;
        }

        void move(float offset) {
            this.offset += offset;
        }

        void setOffset(float offset) {
            this.offset = offset;
        }

        void setItem(Item item) {
            this.item = item;
            calculate();
        }

        @Override
        public void draw(Canvas canvas) {
            item.draw(canvas, originX + padding, originY + offset + padding);
        }
    }

    private static final int INVISIBLE_ITEMS = 2;
    private static final int VISIBLE_ITEMS = 3;
    private LinkedList<Crate> internalItems = new LinkedList<>();
    private Crate current;
    private IDataWindow window;
    private int dataSize = INVISIBLE_ITEMS + VISIBLE_ITEMS;
    private int dataOffset = dataSize / 2;

    private float originX;
    private float originY;
    private float padding;
    private RectF renderTarget = new RectF();
    private float upperCenterBound;
    private float bottomCenterBound;

    private float crateW;
    private float crateH;
    private boolean sizeAssigned = false;

    public ItemContainer(float x, float y, int visibleItems){
        if (visibleItems % 2 == 0)
            throw new IllegalArgumentException("visibleItems has to be odd.");
        padding = 15f;
        setOrigin(x, y);
    }

    public ItemContainer(IDataWindow window, float x, float y, float padding, int visibleItems) {
        if (visibleItems % 2 == 0)
            throw new IllegalArgumentException("visibleItems has to be odd.");
        this.window = window;
        this.padding = padding;
        setOrigin(x, y);
        dataSize = visibleItems + INVISIBLE_ITEMS;
        dataOffset = dataSize / 2;
        initItems();
        calculateRenderTarget();
        calculateBounds();
    }

    public ItemContainer(IDataWindow window, float x, float y, float padding) {
        this(window, x, y, padding, VISIBLE_ITEMS);
    }

    public ItemContainer(IDataWindow window){
        this.window = window;
        this.padding = 15f;
        setOrigin(0f, 0f);
        initItems();
        calculateRenderTarget();
        calculateBounds();
    }

    private void initItems(){
        for (int i = 0; i < dataSize; ++i) {
            internalItems.addFirst(new Crate((Item) window.receiveData(i - dataOffset), padding, dataSize - i - 1));
        }
        current = internalItems.get(dataOffset);
    }

    private void calculateBounds(){
        upperCenterBound = current.offset - crateH;
        bottomCenterBound = current.offset + crateH;
    }

    private void calculateRenderTarget(){
        renderTarget.set(originX,
                originY + crateH * (INVISIBLE_ITEMS / 2) - crateH / 2f,
                originX + crateW,
                originY + crateH * (dataSize - INVISIBLE_ITEMS / 2) - crateH / 2);
    }

    public void setMaximalItemSize(float w, float h) {
        crateW = w;
        crateH = h;
        sizeAssigned = true;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        canvas.clipRect(renderTarget);
        for (IDrawable drawable : internalItems) {
            drawable.draw(canvas);
        }
        canvas.restore();
    }

    public void scroll(float dy) {
        for (Crate crate : internalItems) {
            crate.move(dy);
        }
        if (current.getOffset() > bottomCenterBound) {

            window.shift(ShiftDirection.FORWARD);
            Crate prefirst = internalItems.getFirst();
            internalItems.addFirst(internalItems.removeLast());
            Crate first = internalItems.getFirst();
            first.setItem((Item) window.receiveData(dataOffset));
            first.setOffset(prefirst.offset - crateH);
            current = internalItems.get(dataOffset);

        } else if (current.getOffset() < upperCenterBound) {

            window.shift(ShiftDirection.BACKWARD);
            Crate prelast = internalItems.getLast();
            internalItems.addLast(internalItems.removeFirst());
            Crate last = internalItems.getLast();
            last.setItem((Item) window.receiveData(-dataOffset));
            last.setOffset(prelast.offset + crateH);
            current = internalItems.get(dataOffset);
        }
    }

    public Item getCurrent() {
        return current.item;
    }

    public void setOrigin(float x, float y) {
        originX = x;
        originY = y;
    }

    public int getWidth() {
        return (int) renderTarget.width();
    }

    public int getHeight() {
        return (int) renderTarget.height();
    }

    public void invalidate() {
        internalItems.clear();
        initItems();
        calculateRenderTarget();
        calculateBounds();
    }
}
