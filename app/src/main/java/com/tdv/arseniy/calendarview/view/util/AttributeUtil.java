package com.tdv.arseniy.calendarview.view.util;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;

/**
 * Created by arseniy on 04.09.16.
 */

public final class AttributeUtil {

    public static void resolve(View view, AttributeSet attrs, int[] styleable) {
        Class<?> vc = view.getClass();
        TypedArray array = null;
        try {
            array = view.getContext().obtainStyledAttributes(attrs, styleable, 0, 0);
            Field[] fields = vc.getDeclaredFields();
            for (Field field : fields) {
                AttributeBind binding = field.getAnnotation(AttributeBind.class);
                if (binding != null) {
                    field.setAccessible(true);
                    switch (binding.type()) {
                        case INTEGER:
                            field.setInt(view, array.getInt(binding.id(), field.getInt(view)));
                            break;
                        case STRING:
                            if (!field.getType().equals(String.class)) {
                                throw new ClassCastException("field " + field.getName() + " cannot be converted to String type.");
                            }
                            field.set(view, array.getString(binding.id()));
                            break;
                        case COLOR:
                            field.setInt(view, array.getColor(binding.id(), field.getInt(view)));
                            break;
                        case DIMENSION:
                            if (field.getType().equals(Float.TYPE)) {
                                field.setFloat(view, array.getDimension(binding.id(), field.getFloat(view)));
                            } else {
                                field.setInt(view, array.getDimensionPixelSize(binding.id(), field.getInt(view)));
                            }
                    }
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new ClassCastException("One of fields cannot be accessed.\n" +
                    e.getMessage());

        } finally {
            if (array != null) {
                array.recycle();
            }
        }

    }
}
