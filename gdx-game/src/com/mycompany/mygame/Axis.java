package com.mycompany.mygame;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.*;
import java.util.*;
import java.util.Vector;

public class Axis
{
    float xMin;
    float xMax;
    float xGrid;
    float yMin;
    float yMax;
    float yGrid;

    ArrayList<String> calculationLog = new ArrayList<>();

    Vector2List yAxisLineStart = new Vector2List();
    Vector2List yAxisLineEnd = new Vector2List();
    Vector2List yAxisMarkStart = new Vector2List();
    Vector2List yAxisMarkEnd = new Vector2List();
    int yAxisMainIndex;

    Vector2List xAxisLineStart = new Vector2List();
    Vector2List xAxisLineEnd = new Vector2List();
    Vector2List xAxisMarkStart = new Vector2List();
    Vector2List xAxisMarkEnd = new Vector2List();
    int xAxisMainIndex;

    ArrayList<String> xAxisText = new ArrayList<>();
    Vector2List xAxisTextPosition = new Vector2List();
    int xAxisTextAlign;
    ArrayList<String> yAxisText = new ArrayList<>();
    Vector2List yAxisTextPosition = new Vector2List();
    int yAxisTextAlign;

    public Axis()
    {
        xMin = -5f;
        xMax = 5f;
        xGrid = 0.1f;
        yMin = -5;
        yMax = 5f;
        yGrid = 1;
    }

    public void log()
    {
        GdxUtil.log("Axis");
        GdxUtil.log("x=<" + NumberUtil.toString(xMin, 3) + "," + NumberUtil.toString(xMax, 3) + "> grid " + NumberUtil.toString(xGrid, 3));
        GdxUtil.log("y=<" + NumberUtil.toString(yMin, 3) + "," + NumberUtil.toString(yMax, 3) + "> grid " + NumberUtil.toString(yGrid, 3));
        for (String logText:calculationLog)
        {
            GdxUtil.log(logText);
        }
        GdxUtil.log("");
    }

    public boolean setWorldSize(float xMin, 
                                float xMax, 
                                float xGrid, 
                                float yMin, 
                                float yMax, 
                                float yGrid)
    {
        boolean result=false;
        if (!NumberUtil.equals(this.xMin, xMin) && isReasonable(xMin, xMax, xGrid))
        {
            result = true;
            this.xMin = xMin;
        }
        if (!NumberUtil.equals(this.xMax, xMax) && isReasonable(xMin, xMax, xGrid))
        {
            result = true;
            this.xMax = xMax;
        }
        if (!NumberUtil.equals(this.xGrid, xGrid) && isReasonable(xMin, xMax, xGrid))
        {
            result = true;
            this.xGrid = xGrid;
        }
        if (!NumberUtil.equals(this.yMin, yMin) && isReasonable(yMin, yMax, yGrid))
        {
            result = true;
            this.yMin = yMin;
        }
        if (!NumberUtil.equals(this.yMax, yMax) && isReasonable(yMin, yMax, yGrid))
        {
            result = true;
            this.yMax = yMax;
        }
        if (!NumberUtil.equals(this.yGrid, yGrid) && isReasonable(yMin, yMax, yGrid))
        {
            result = true;
            this.yGrid = yGrid;
        }
        return result;
    }

    private boolean isReasonable(float min, float max, float grid)
    {
        return NumberUtil.isReasonable(min) && //
            NumberUtil.isReasonable(max) && //
            NumberUtil.isReasonable(grid);
    }

    // --------------------------------------------------
    // Screen to Graph coordinates conversion
    // --------------------------------------------------
    protected void graph2Screen(Vector2 screenPoint, float graphX, float graphY)
    {
        float graphWidth = xMax - xMin;
        float graphHeight = yMax - yMin;
        float screenX = (graphX - xMin) * GdxUtil.getScreenWidth() / graphWidth;
        float screenY = (graphY - yMin) * GdxUtil.getScreenHeight() / graphHeight;
        screenPoint.set(screenX, screenY);
    }

    protected void screen2Graph(Vector2 graphPoint, float screenX, float screenY, boolean snapToGrid)
    {
        float graphWidth = xMax - xMin;
        float graphHeight = yMax - yMin;
        float graphX = screenX * graphWidth / GdxUtil.getScreenWidth();
        float graphY = screenY * graphHeight / GdxUtil.getScreenHeight();
        if (snapToGrid)
        {
            graphX = graphX2SnapX(graphX);
            graphY = graphY2SnapY(graphY);
        }
        graphPoint.set(graphX, graphY);
    }

    protected float graphX2SnapX(float graphX)
    {
        return xGrid * Math.round(graphX / xGrid);
    }

    protected float graphY2SnapY(float graphY)
    {
        return yGrid * Math.round(graphY / yGrid);
    }

    protected float graphX2UpperSnapX(float graphX)
    {
        return xGrid * (int) Math.ceil((graphX - NumberUtil.SMALLEST_FLOAT) / xGrid);
    }

    protected float graphY2UpperSnapY(float graphY)
    {
        return yGrid * (int) Math.ceil((graphY - NumberUtil.SMALLEST_FLOAT) / yGrid);
    }

    protected float graphX2LowerSnapX(float graphX)
    {
        return xGrid * (int) Math.floor((graphX + NumberUtil.SMALLEST_FLOAT) / xGrid);
    }

    protected float graphY2LowerSnapY(float graphY)
    {
        return yGrid * (int) Math.floor((graphY + NumberUtil.SMALLEST_FLOAT) / yGrid);
    }

    protected Vector2 getGraphPixel()
    {
        Vector2 result = new Vector2();
        screen2Graph(result, 1, 1, false);
        return result;
    }

    public void calculateAxis()
    {
        // clear previous calculation
        calculationLog.clear();

        yAxisLineStart.clear();
        yAxisLineEnd.clear();
        yAxisMarkStart.clear();
        yAxisMarkEnd.clear();
        yAxisMainIndex = -1;

        xAxisLineStart.clear();
        xAxisLineEnd.clear();
        xAxisMarkStart.clear();
        xAxisMarkEnd.clear();
        xAxisMainIndex = -1;

        xAxisText.clear();
        xAxisTextPosition.clear();
        yAxisText.clear();
        yAxisTextPosition.clear();

        // start and end for axis
        float xStart = graphX2UpperSnapX(xMin);
        float xEnd = graphX2LowerSnapX(xMax) + xGrid / 2;

        float yStart = graphY2UpperSnapY(yMin);
        float yEnd = graphY2LowerSnapY(yMax) + yGrid / 2;

        // mark coordinates
        boolean doXAxisMarks = yStart <= 0 && 0 <= yEnd;
        boolean doYAxisMarks = xStart <= 0 && 0 <= xEnd;
        float xForXAxisMark = (xMax - xMin) / GdxUtil.getScreenWidth() * GdxUtil.getCameraZoom();
        float yForYAxisMark = (yMax - yMin) / GdxUtil.getScreenHeight() * GdxUtil.getCameraZoom();

        // label coordinates and align
        float yForXAxisText =0;
        xAxisTextAlign = GdxUtil.HALIGN_CENTER | GdxUtil.VALIGN_BOTTOM;
        if (yForXAxisText < yMin + yGrid / 2)
        {
            yForXAxisText = yMin;
            xAxisTextAlign = GdxUtil.HALIGN_CENTER | GdxUtil.VALIGN_TOP;
        }
        if (yMax < yForXAxisText)
        {
            yForXAxisText = yMax;
        }

        float xForYAxisText =0;
        yAxisTextAlign = GdxUtil.HALIGN_LEFT | GdxUtil.VALIGN_CENTER;
        if (xForYAxisText < xMin + xGrid / 2)
        {
            xForYAxisText = xMin;
            yAxisTextAlign = GdxUtil.HALIGN_RIGHT | GdxUtil.VALIGN_CENTER;
        }
        if (xMax < xForYAxisText)
        {
            xForYAxisText = xMax;
        }

        // y axis
        for (float x = xStart; x <= xEnd; x += xGrid)
        { 
            if (NumberUtil.equals(x, 0f))
            {
                yAxisMainIndex = yAxisLineStart.size();
            }
            Vector2 start = yAxisLineStart.newElement();
            Vector2 end = yAxisLineEnd.newElement();
            graph2Screen(start, x, yMin);
            graph2Screen(end, x, yMax);
            yAxisLineStart.add(start);
            yAxisLineEnd.add(end);

            if (doXAxisMarks)
            {
                start = yAxisMarkStart.newElement();
                end = yAxisMarkStart.newElement();
                graph2Screen(start, x, -5 * yForYAxisMark);
                graph2Screen(end, x, 5 * yForYAxisMark);
                yAxisMarkStart.add(start);
                yAxisMarkEnd.add(end);
            }

            String label = NumberUtil.toGridString(x, xGrid);
            xAxisText.add(label);
            xAxisTextPosition.add(getXAxisTextPosition(xAxisTextPosition.newElement(), x, yForXAxisText, 0));
        }

        // x axis
        for (float y = yStart; y <= yEnd; y += yGrid)
        {
            if (NumberUtil.equals(y, 0f))
            {
                xAxisMainIndex = xAxisLineStart.size();
            }

            Vector2 start= xAxisLineStart.newElement();
            Vector2 end = xAxisLineEnd.newElement();
            graph2Screen(start, xMin, y);
            graph2Screen(end, xMax, y);
            xAxisLineStart.add(start);
            xAxisLineEnd.add(end);

            if (NumberUtil.equals(y, 0f) && NumberUtil.equals(xForYAxisText, 0f))
            {
                continue;
            }

            if (doYAxisMarks)
            {
                start = xAxisMarkStart.newElement();
                end = xAxisMarkEnd.newElement();
                graph2Screen(start, -5 * xForXAxisMark, y);
                graph2Screen(end, 5 * xForXAxisMark, y);
                xAxisMarkStart.add(start);
                xAxisMarkEnd.add(end);
            }
            String label = NumberUtil.toGridString(y, yGrid);
            yAxisText.add(label);
            yAxisTextPosition.add(getYAxisTextPosition(yAxisTextPosition.newElement(), xForYAxisText, y, 0));
        }

        // realign the Left aligned text to right aligned so that
        // decimal comma matches vertically
        if ((yAxisTextAlign & GdxUtil.HALIGN_RIGHT) == GdxUtil.HALIGN_RIGHT)
        {
            float textSizeMaxWidth = 0f;
            for (int i=0; i < yAxisText.size(); i++)
            {
                Vector2 textSize = GdxUtil.getRenderedTextSize(yAxisText.get(i));
                textSizeMaxWidth = Math.max(textSize.x, textSizeMaxWidth);                   
            }
            yAxisTextAlign = yAxisTextAlign + GdxUtil.HALIGN_LEFT - GdxUtil.HALIGN_RIGHT;
            textSizeMaxWidth += 15f;
            for (int i=0; i < yAxisText.size(); i++)
            {
                yAxisTextPosition.get(i).x += textSizeMaxWidth;
            }
        }
    }

    private Vector2 getXAxisTextPosition(Vector2 result, float graphX, float graphY, int line)
    {
        graph2Screen(result, graphX, graphY);
        GdxUtil.project(result);
        result.set(result.x, result.y + 5 - line * 20);
        return result;
    }

    protected Vector2 getYAxisTextPosition(Vector2 result, float graphX, float graphY, float line)
    {
        graph2Screen(result, graphX, graphY);
        GdxUtil.project(result);
        result.set(result.x - 7, result.y - line * 20);
        return result;
    }

}
