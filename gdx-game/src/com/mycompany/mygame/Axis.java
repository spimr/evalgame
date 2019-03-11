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
    OrthographicCamera camera;

    int screenWidth;
    int screenHeight;
    float xMin;
    float xMax;
    float xGrid;
    float yMin;
    float yMax;
    float yGrid;

    Vector<Vector2> yAxisLineStart = new Vector<>();
    Vector<Vector2> yAxisLineEnd = new Vector<>();
    Vector<Vector2> yAxisMarkStart = new Vector<>();
    Vector<Vector2> yAxisMarkEnd = new Vector<>();
    Vector<Vector2> xAxisLineStart = new Vector<>();
    Vector<Vector2> xAxisLineEnd = new Vector<>();
    Vector<Vector2> xAxisMarkStart = new Vector<>();
    Vector<Vector2> xAxisMarkEnd = new Vector<>();
    Vector<String> xAxisText = new Vector<>();
    Vector<Vector2> xAxisTextPosition = new Vector<>();
    Vector<String> yAxisText = new Vector<>();
    Vector<Vector2> yAxisTextPosition = new Vector<>();

    public Axis(OrthographicCamera camera)
    {
        this.camera = camera;
    }

    public void setScreenSize(int screenWidth, int screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setWorldSize(float xMin, float xMax, float xGrid, float yMin, float yMax, float yGrid)
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.xGrid = xGrid;
        this.yMin = yMin;
        this.yMax = yMax;
        this.yGrid = yGrid;
    }

    // --------------------------------------------------
    // Screen to Graph coordinates conversion
    // --------------------------------------------------
    protected void graph2Screen(Vector2 screenPoint, float graphX, float graphY)
    {
        float graphWidth = xMax - xMin;
        float graphHeight = yMax - yMin;
        float screenX = (graphX - xMin) * screenWidth / graphWidth;
        float screenY = (graphY - yMin) * screenHeight / graphHeight;
        screenPoint.set(screenX, screenY);
    }

    protected void screen2Graph(Vector2 graphPoint, float screenX, float screenY, boolean snapToGrid)
    {
        float graphWidth = xMax - xMin;
        float graphHeight = yMax - yMin;
        float graphX = screenX * graphWidth / screenWidth;
        float graphY = screenY * graphHeight / screenHeight;
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
        return xGrid * (int) Math.ceil(graphX / xGrid);
    }

    protected float graphY2UpperSnapY(float graphY)
    {
        return yGrid * (int) Math.ceil(graphY / yGrid);
    }

    protected Vector2 getGraphPixel()
    {
        Vector2 result = new Vector2();
        screen2Graph(result, 1, 1, false);
        return result;
    }

    public void calculateAxis()
    {
        yAxisLineStart.clear();
        yAxisLineEnd.clear();
        yAxisMarkStart.clear();
        yAxisMarkEnd.clear();
        xAxisLineStart.clear();
        xAxisLineEnd.clear();
        xAxisMarkStart.clear();
        xAxisMarkEnd.clear();
        xAxisText.clear();
        xAxisTextPosition.clear();
        yAxisText.clear();
        yAxisTextPosition.clear();

        for (float x = xMin + xGrid; x < xMax; x += xGrid)
        { 
            Vector2 start= new Vector2();
            Vector2 end = new Vector2();
            graph2Screen(start, x, yMin);
            graph2Screen(end, x, yMax);
            yAxisLineStart.add(start);
            yAxisLineEnd.add(end);

            start = new Vector2();
            end = new Vector2();
            graph2Screen(start, x, -5 * (yMax - yMin) / screenHeight * camera.zoom);
            graph2Screen(end, x, 5 * (yMax - yMin) / screenHeight * camera.zoom);
            yAxisMarkStart.add(start);
            yAxisMarkEnd.add(end);

            String label = NumberUtil.toGridString(x, xGrid);
            xAxisText.add(label);
            xAxisTextPosition.add(getXAxisTextPosition(x, 0));
        }

        for (float y = yMin + yGrid; y < yMax; y += yGrid)
        {
            Vector2 start= new Vector2();
            Vector2 end = new Vector2();

            graph2Screen(start, xMin, y);
            graph2Screen(end, xMax, y);
            xAxisLineStart.add(start);
            xAxisLineEnd.add(end);

            if (NumberUtil.isZero(y))
            {
                continue;
            }

            start = new Vector2();
            end = new Vector2();
            graph2Screen(start, -5 * (xMax - xMin) / screenWidth * camera.zoom, y);
            graph2Screen(end, 5 * (xMax - xMin) / screenWidth * camera.zoom, y);
            xAxisMarkStart.add(start);
            xAxisMarkEnd.add(end);

            String label = NumberUtil.toGridString(y, yGrid);
            yAxisText.add(label);
            yAxisTextPosition.add(getYAxisTextPosition(y, 0));
        }
    }

    private Vector2 getXAxisTextPosition(float graphX, int line)
    {
        Vector2 result = new Vector2();
        Vector3 vector = new Vector3();
        graph2Screen(result, graphX, 0);
        vector.set(result, 0);
        camera.project(vector);
        result.set(vector.x, vector.y - 10 - line * 20);
        return result;
    }

    protected Vector2 getYAxisTextPosition(float graphY, float line)
    {
        Vector2 result = new Vector2();
        Vector3 vector = new Vector3();
        graph2Screen(result, 0, graphY);
        vector.set(result, 0);
        camera.project(vector);
        result.set(vector.x - 10, vector.y - line * 20);
        return result;
    }

}
