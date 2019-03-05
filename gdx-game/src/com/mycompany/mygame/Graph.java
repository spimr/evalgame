package com.mycompany.mygame;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.*;
import java.util.*;
import java.util.Vector;

public class Graph
{
    public static final String TEST_EXPRESSION = "sin(1/x)";
    // "x";
    // "x^2";
    // "sin(1/x)"
    // "x*(sin(x))^3"

    String expression = TEST_EXPRESSION; 
    String expressionStatus;

    float xMin;
    float xMax;
    float xGrid;
    float yMin;
    float yMax;
    float yGrid;

    int screenWidth;
    int screenHeight;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;

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

    Plot plot;

    // --------------------------------------------------
    // Batch, ShapeRenderer and Camera
    // --------------------------------------------------
    public void setGdxObjects(SpriteBatch batch,
                              ShapeRenderer shapeRenderer,
                              OrthographicCamera camera)
    {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
    }

    public void setScreenSize(int screenWidth, int screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        camera.viewportWidth = screenWidth;
        camera.viewportHeight = screenHeight;
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();
    }

    protected void setCameraZoom(float zoom)
    {
        if (zoom < 0.01f)
        {
            zoom = 0.01f;
        }
        if (zoom > 10)
        {
            zoom = 1;
        }
        camera.zoom = zoom;
        camera.update();
    }

    boolean batchStarted = false;
    ShapeRenderer.ShapeType shapeType;
    float lineWidth;

    protected void setSettings(ShapeRenderer.ShapeType shapeType, float lineWidth)
    {
        if (shapeType == null)
        {
            shapeType = this.shapeType;
        }
        if (this.shapeType != shapeType || this.lineWidth != lineWidth)
        {
            if (batchStarted)
            {
                endBatch();
            }
            Gdx.gl.glLineWidth(lineWidth);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(shapeType);
            batch.begin();
            batchStarted = true;
            this.shapeType = shapeType;
            this.lineWidth = lineWidth;
        }
    }

    protected void startBatch()
    {
        shapeType = null;
        lineWidth = 0;
        batchStarted = false;
        setSettings(ShapeRenderer.ShapeType.Line, 1f);
    }

    protected void endBatch()
    {
        batch.end();
        shapeRenderer.end();
        batchStarted = false;
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

    public void calculateDomain()
    {
        if (expression.equals("sin(1/x)"))
        {
            xMin = -0.5f;
            xMax = 0.5f;
            xGrid = 0.1f;
            yMin = -1.5f;
            yMax = 1.5f;
            yGrid = 0.1f;
        }
        else if (expression.equals("x^2"))
        {
            xMin = -5f;
            xMax = 5f;
            xGrid = 1f;
            yMin = -15f;
            yMax = 15f;
            yGrid = 1f;
        }
        else if (expression.equals("x^4"))
        {
            xMin = -1f;
            xMax = 1f;
            xGrid = 0.1f;
            yMin = -0.1f;
            yMax = 0.3f;
            yGrid = 0.1f;
        }
        else
        {
            xMin = -5f;
            xMax = 5f;
            xGrid = 1f;
            yMin = -5f;
            yMax = 5f;
            yGrid = 1f;
        }
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        setCameraZoom(1f);
    }

    public void calculateGraph(int plotAlgorithm)
    {
        calculateAxis();
        calculatePlot(plotAlgorithm);
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
            yAxisTextPosition.add(getYAxisTextPosition(y,0));
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

    public void calculatePlot(int plotAlgorithm)
    {
        plot = GraphUtil.plotGraph(this, plotAlgorithm);

        // infer rendering values in the plot
        long gridPointsCount =0;
        float gridPointsToX = graphX2UpperSnapX(xMin + xGrid);
        for (Vector2 point : plot.graphPoints)
        {
            // prepare end point
            if (point != null)
            {
                Vector2 end=new Vector2();
                graph2Screen(end, point.x, point.y);
                plot.screenPoints.add(end);
                float snapX = graphX2UpperSnapX(point.x);
                if (snapX <= gridPointsToX)
                {
                    gridPointsCount++;
                }
                else 
                {
                    plot.pointsCountXAxisText.add(Long.toString(gridPointsCount));
                    plot.pointsCountXAxisPosition.add(getXAxisTextPosition(gridPointsToX - xGrid / 2, 1));
                    gridPointsCount = 1;
                    gridPointsToX = snapX;
                }

                Vector2 blueDot = new Vector2();
                graph2Screen(blueDot, point.x, 0);
                plot.blueDotScreenPosition.add(blueDot);
            }
            else
            {
                plot.screenPoints.add(null);
                plot.blueDotScreenPosition.add(null);
            }
        }
        plot.pointsCountXAxisText.add(Long.toString(gridPointsCount));
        plot.pointsCountXAxisPosition.add(getXAxisTextPosition(gridPointsToX - xGrid / 2, 1));
    }
}
