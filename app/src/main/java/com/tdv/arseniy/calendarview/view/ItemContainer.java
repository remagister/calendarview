package com.tdv.arseniy.calendarview.view;

import android.graphics.Canvas;
import android.util.Log;

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

    private interface ItemBox extends IMeasurable, IDrawable{
        float getOffset();
        void setOffset(float offset);
        void move(float dx);
        Item getItem();
        void setItem(Item item);
    }

    private class ItemBoxFactory{

        private class Box implements ItemBox{
            float offset;
            Item item;

            Box(float offset, Item item) {
                this.offset = offset;
                this.item = item;
            }

            @Override
            public float getOffset() {
                return offset;
            }

            @Override
            public void setOffset(float offset) {
                this.offset = offset;
            }

            @Override
            public void move(float dx) {
                offset += dx;
            }

            @Override
            public Item getItem() {
                return item;
            }

            @Override
            public void setItem(Item item) {
                this.item = item;
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
                item.draw(canvas, originX + padding, originY + offset + padding);
            }
        }

        private float maxItemWidth;
        private float maxItemHeight;
        private float boxHeight;
        private float boxWidth;
        private float padding;
        private int originIndex;

        ItemBoxFactory(float maxItemWidth, float maxItemHeight) {
            this(maxItemWidth, maxItemHeight, 20f);
        }

        public ItemBoxFactory(float maxItemWidth, float maxItemHeight, int originIndex) {
            this(maxItemWidth, maxItemHeight, 20f, originIndex);
        }

        ItemBoxFactory(float maxItemWidth, float maxItemHeight, float padding) {
            this(maxItemWidth, maxItemHeight, padding, 0);
        }

        ItemBoxFactory(float maxItemWidth, float maxItemHeight, float padding, int originIndex) {
            this.maxItemWidth = maxItemWidth;
            this.maxItemHeight = maxItemHeight;
            this.padding = padding;
            this.originIndex = originIndex;
            init();
        }

        private void init(){
            boxHeight = maxItemHeight + 2f*padding;
            boxWidth = maxItemWidth + 2f*padding;
        }

        void setMaxItemSize(float width, float height){
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

        float getOffsetAtIndex(int index){
            return boxHeight * (index - originIndex);
        }

        ItemBox build(IDataWindow window, int index){
            return new Box(boxHeight * (index - originIndex), (Item) window.receiveData(index));
        }
    }

    private class IteratorImpl implements java.util.Iterator<IDrawable>{

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
    private IDataWindow window;
    private LinkedList<ItemBox> internalContainer = new LinkedList<>();
    private ItemBoxFactory itemBoxFactory;
    private ItemBox current;
    private int visibleItems = VISIBLE_ITEMS;
    private int maxLoopTimes = visibleItems + PREPARED_ITEMS;
    private int pivot;
    private float height;
    private float originX = 0f;
    private float originY = 0f;
    private float centerOffset;

    public ItemContainer(float maxItemWidth, float maxItemHeight) {
        itemBoxFactory = new ItemBoxFactory(maxItemWidth, maxItemHeight);
    }

    public ItemContainer(IDataWindow window, float maxItemWidth, float maxItemHeight) {
        this(maxItemWidth, maxItemHeight);
        this.window = window;
        refresh();
    }

    @Override
    public void setDataWindow(IDataWindow window) {
        this.window = window;
        refresh();
    }

    private void loop(int times, ShiftDirection direction){
        if(times > maxLoopTimes) {
            times = maxLoopTimes;
        }
        window.shift(direction, times);
        ItemBox pre, curr;
        switch (direction){
            case BACKWARD:{
                while(times-- != 0){
                    pre = internalContainer.getLast();
                    curr = internalContainer.removeFirst();
                    curr.setItem((Item) window.receiveData(-pivot+times-1));
                    curr.setOffset(pre.getOffset() + itemBoxFactory.getBoxHeight());
                    internalContainer.addLast(curr);
                }
                break;
            }
            case FORWARD:{
                while(times-- != 0){
                    pre = internalContainer.getFirst();
                    curr = internalContainer.removeLast();
                    curr.setItem((Item) window.receiveData(pivot-times+1));
                    curr.setOffset(pre.getOffset() - itemBoxFactory.getBoxHeight());
                    internalContainer.addFirst(curr);
                }
                break;
            }
        }
        current = internalContainer.get(pivot);
    }

    @Override
    public void scroll(float dx) {
        for (ItemBox box: internalContainer) {
            box.move(dx);
        }
        if(current.getOffset() < itemBoxFactory.getOffsetAtIndex(-1)){
            loop(1, ShiftDirection.BACKWARD);
        }
        else if (current.getOffset() > itemBoxFactory.getOffsetAtIndex(1)){
            loop(1, ShiftDirection.FORWARD);
        }
        /*int overlap = (int)((centerOffset - current.getOffset()) / itemBoxFactory.getBoxHeight());
        // if > 0 then center moved up (backward scroll)
        if(overlap > 1){
            loop(overlap, ShiftDirection.BACKWARD);
        }
        if(overlap < -1){
            loop(-overlap, ShiftDirection.FORWARD);
        }
        Log.d("SCROLL", "overlap: " + overlap);*/
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
        height = visibleItems * itemBoxFactory.getBoxHeight();
        internalContainer.clear();
        pivot = (visibleItems + PREPARED_ITEMS) / 2;
        itemBoxFactory.setOriginIndex(- pivot + PREPARED_ITEMS/2);
        maxLoopTimes = visibleItems + PREPARED_ITEMS;
        for(int index = -pivot; index <= pivot; ++index){
            internalContainer.addFirst(itemBoxFactory.build(window, index));
        }
        current = internalContainer.get(pivot);
        centerOffset = itemBoxFactory.getOffsetAtIndex(0) + itemBoxFactory.getBoxHeight() / 2f;
    }

    @Override
    public void setOrigin(float x, float y) {
        originX = x;
        originY = y;
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
