package com.mycompany.mygame;

import java.text.*;
import java.util.ArrayList;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.math.*;

public class NumberUtil
{
    public static float SMALLEST_FLOAT = 0.000001f;
    public static float BIGGEST_FLOAT = 1000000f;

    public static boolean equals(float x, float y)
    {
        return Math.abs(x - y) < SMALLEST_FLOAT; 
    }

    public static boolean isZero(float x)
    {
        return equals(x, 0f);
    }

    public static boolean isReasonable(float x)
    {

        return Float.isFinite(x) && 
            Math.abs(x) > SMALLEST_FLOAT &&
            Math.abs(x) < BIGGEST_FLOAT;            
    }                      

    public static boolean isFinite(float x)
    {
        return Float.isFinite(x);
    }

    public static String toString(float number)
    {
        return Float.toString(number);
    }

    public static String toString(float number, int decimalDigits)
    {
        String result;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(decimalDigits);
        nf.setMinimumFractionDigits(decimalDigits);
        nf.setGroupingUsed(false);
        result = nf.format(number);
        return result;
    }

    public static String toString(Vector2 point, int decimalDigits)
    {
        StringBuilder result = new StringBuilder();
        result.append("[");
        result.append(toString(point.x, decimalDigits));
        result.append(", ");
        result.append(toString(point.y, decimalDigits));
        result.append("]");
        return result.toString();
    }

    public static String toGridString(float number, float grid)
    {
        String result;
        if (grid > 1)
        {
            result = toString(number, 0);
        }
        else
        {
            int decimalDigits = -(int)Math.round(Math.log10(grid));
            result = toString(number, decimalDigits);
        }
        return result;
    }
}
