package com.tdv.arseniy.calendarview.view.container;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.widget.Scroller;

import com.tdv.arseniy.calendarview.view.drawable.IDrawable;
import com.tdv.arseniy.calendarview.view.drawable.IMeasurable;
import com.tdv.arseniy.calendarview.view.drawable.Item;
import com.tdv.arseniy.calendarview.view.provider.IDataWindow;
import com.tdv.arseniy.calendarview.view.provider.ShiftDirection;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by arseniy on 30.08.16.
 */

public class ItemContainer implements IContainer {

    private interface ItemBox extends IMeasurable, IDrawable {
        float getOffset();

        void setOffset(float offset);

        void move(float dx);

        Item getItem();

        void setItem(Item item);

        void refresh();
    }

    private class ItemBoxFactory {

        private class Box implements ItemBox {
            float offset;
            Item item;
            float localX;
            float localY;
            int fading;

            Box(float offset, Item item) {
                this.offset = offset;
                this.item = item;
                init();
            }

            void initX() {
                localX = originX + (boxWidth - item.measureWidth()) / 2f;
            }

            void initY() {
                localY = originY + offset + (boxHeight - item.measureHeight()) / 2f + item.measureHeight();
                final float normalized = (float) Math.PI * (offset + halfHeight) / height;
                fading = normalized > Math.PI || normalized < 0 ? 0 : (int) (Math.sin(normalized) * 255.0);

            }

            void init() {
                initX();
                initY();
            }

            @Override
            public float getOffset() {
                return offset;
            }

            @Override
            public void setOffset(float offset) {
                this.offset = offset;
                initY();
            }

            @Override
            public void move(float dx) {
                offset += dx;
                initY();
            }

            @Override
            public Item getItem() {
                return item;
            }

            @Override
            public void setItem(Item item) {
                this.item = item;
                init();
            }

            @Override
            public void refresh() {
                init();
            }

            @Override
            public float measureWidth() {
                return boxWidth;
            }

            @Override
            public float measureHeight() {
                return boxHeight;
            }

            @Override
            public void draw(Canvas canvas) {
                item.getPaint().setAlpha(fading);
                item.draw(canvas, localX, localY);
            }
        }

        private static final float SPACING_PERCENT = .8f;
        private static final float PADDING_PERCENT = .2f;

        private float maxItemWidth;
        private float maxItemHeight;
        private float boxHeight;
        private float boxWidth;
        private float halfHeight;
        private int originIndex;

        ItemBoxFactory(float maxItemWidth, float maxItemHeight) {
            this(maxItemWidth, maxItemHeight, 0);
        }

        ItemBoxFactory(float maxItemWidth, float maxItemHeight, int originIndex) {
            this.maxItemWidth = maxItemWidth;
            this.maxItemHeight = maxItemHeight;
            this.originIndex = originIndex;
            init();
        }

        private void init() {
            boxHeight = maxItemHeight * (1 + SPACING_PERCENT);
            boxWidth = maxItemWidth * (1 + PADDING_PERCENT);
            halfHeight = boxHeight / 2f;
        }

        void setMaxItemSize(float width, float height) {
            this.maxItemWidth = width;
            this.maxItemHeight = height;
            init();
        }

        float getBoxHeight() {
            return boxHeight;
        }

        float getBoxWidth() {
            return boxWidth;
        }

        void setOriginIndex(int originIndex) {
            this.originIndex = originIndex;
        }

        float getOffsetAtIndex(int index) {
            return boxHeight * (index - originIndex);
        }

        ItemBox build(IDataWindow window, int index) {
            return new Box(boxHeight * (index - originIndex), (Item) window.receiveData(-index));
        }
    }

    private class IteratorImpl implements java.util.Iterator<IDrawable> {

        private java.util.Iterator<ItemBox> internalIterator;

        IteratorImpl() {
            internalIterator = internalContainer.iterator();
        }

        @Override
        public boolean hasNext() {
            return internalIterator.hasNext();
        }

        @Override
        public IDrawable next() {
            return internalIterator.next();
        }
    }

    private static final int VISIBLE_ITEMS = 3;
    private static final int PREPARED_ITEMS = 2;
    private static final int MAX_SPEED_MULTIPLIER = 70;
    private IDataWindow window;
    private LinkedList<ItemBox> internalContainer = new LinkedList<>();
    private ItemBoxFactory itemBoxFactory;
    private ItemBox current;
    private int visibleItems = VISIBLE_ITEMS;
    private int pivot;
    private float height;
    private float originX = 0f;
    private float originY = 0f;
    private float maxSpeed;
    private float minSpeed;
    private OnItemChangedListener listener;

    public ItemContainer(float maxItemWidth, float maxItemHeight, int visibleItems) {
        itemBoxFactory = new ItemBoxFactory(maxItemWidth, maxItemHeight);
        this.visibleItems = visibleItems;
    }

    public ItemContainer(IDataWindow window, float maxItemWidth, float maxItemHeight) {
        this(maxItemWidth, maxItemHeight, VISIBLE_ITEMS);
        this.window = window;
        refresh();
    }

    @Override
    public void setDataWindow(IDataWindow window) {
        this.window = window;
        refresh();
    }

    private void centerItem() {
            Object oldData = current.getItem().getData();
            current = internalContainer.get(pivot);

            if (listener != null) {
                listener.onItemChanged(this, oldData, current.getItem().getData());
            }
    }

    @Override
    public void scroll(float dx) {
        if (dx == 0f) {
            return;
        }

        for (ItemBox box : internalContainer) {
            box.move(dx);
        }

        ItemBox pre, curr;
        boolean shiftNeeded = false;

        if (dx > 0f) {
            while (true) {
                curr = internalContainer.getFirst();

                if (curr.getOffset() > itemBoxFactory.getOffsetAtIndex(pivot) + itemBoxFactory.getBoxHeight()) {
                    window.shift(ShiftDirection.FORWARD);
                    pre = internalContainer.getLast();
                    internalContainer.removeFirst();
                    curr.setItem((Item) window.receiveData(pivot));
                    curr.setOffset(pre.getOffset() - itemBoxFactory.getBoxHeight());
                    internalContainer.addLast(curr);
                    shiftNeeded = true;

                } else {

                    if (shiftNeeded && dx < itemBoxFactory.getBoxHeight()) {
                        centerItem();
                    }

                    break;
                }
            }
        } else {
            while (true) {
                curr = internalContainer.getLast();

                if (curr.getOffset() < itemBoxFactory.getOffsetAtIndex(-pivot)) {
                    window.shift(ShiftDirection.BACKWARD);
                    pre = internalContainer.getFirst();
                    internalContainer.removeLast();
                    curr.setItem((Item) window.receiveData(-pivot));
                    curr.setOffset(pre.getOffset() + itemBoxFactory.getBoxHeight());
                    internalContainer.addFirst(curr);
                    shiftNeeded = true;
                } else {

                    if (shiftNeeded && dx > -itemBoxFactory.getBoxHeight()) {
                        centerItem();
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void fling(float dy, Scroller scroller) {
        float speedAbs = Math.abs(dy);
        if (speedAbs < minSpeed) {
            scroller.abortAnimation();
            return;
        }

        if (speedAbs > maxSpeed) {
            dy = maxSpeed * Math.signum(dy);
        }

        final int stopPoint = (int) itemBoxFactory.getOffsetAtIndex(0);
        scroller.fling(0, (int) current.getOffset(),    // x, y
                0, (int) dy,                            // vx, vy
                0, 0,                                   // minx maxx
                stopPoint - (int) speedAbs,
                (int) current.getOffset() * 2 + (int) speedAbs - stopPoint);
    }

    @Override
    public void setMaxItemSize(float width, float height) {
        itemBoxFactory.setMaxItemSize(width, height);
        refresh();
    }

    @Override
    public Item getCurrent() {
        return current.getItem();
    }

    @Override
    public float getCentralUpperBound() {
        return 0;
    }

    @Override
    public void refresh() {
        height = visibleItems * itemBoxFactory.boxHeight;
        minSpeed = itemBoxFactory.getBoxHeight();
        maxSpeed = minSpeed * MAX_SPEED_MULTIPLIER;
        internalContainer.clear();
        pivot = (visibleItems + PREPARED_ITEMS) / 2;
        itemBoxFactory.setOriginIndex(-pivot + PREPARED_ITEMS / 2);
        for (int index = -pivot; index <= pivot; ++index) {
            internalContainer.addFirst(itemBoxFactory.build(window, index));
        }
        current = internalContainer.get(pivot);
    }

    @Override
    public void setOrigin(float x, float y) {
        originX = x;
        originY = y;
        for (ItemBox box : internalContainer) {
            box.refresh();
        }
    }

    @Override
    public void setOnItemChangedListener(OnItemChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean hit(float x, float y) {
        return (x > originX) &&
                x < (originX + itemBoxFactory.boxWidth) &&
                (y > originY) &&
                y < (originY + height);
    }

    @Override
    public void setVisibleItems(int visibleItems) {
        if (visibleItems % 2 == 0) {
            throw new IllegalArgumentException("Illegal argument: visibleItems has to be odd.");
        }
        this.visibleItems = visibleItems;
        refresh();
    }

    @Override
    public float measureWidth() {
        return itemBoxFactory.getBoxWidth();
    }

    @Override
    public float measureHeight() {
        return height;
    }

    @Override
    public Iterator<IDrawable> iterator() {
        return new IteratorImpl();
    }

}
