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

    public static final int PLOT_ALGORITHM = GraphUtil.PLOT_ALGORITHM_ADAPTIVE_STEP;
    public static final boolean PLOT_RED_DOTS = true;
    public static final boolean PLOT_BLUE_DOTS = true;

    OrthographicCamera camera;

    String expressionStatus;

    String expression = TEST_EXPRESSION;    
    private int screenWidth;
    private int screenHeight;
    private float xMin;
    private float xMax;
    private float xGrid;
    private float yMin;
    private float yMax;
    private float yGrid;

    Plot plot;
    PlotCalculator plotCalculator;

    public Graph(int screenWidth, int screenHeight)
    {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void setGdxObjects(OrthographicCamera camera)
    {
        this.camera = camera;
    }
    
    public float getXMin()
    {
        return xMin;
    }

    public float getXMax()
    {
        return xMax;
    }

    public float getXGrid()
    {
        return xGrid;
    }

    public float getYMin()
    {
        return yMin;
    }

    public float getYMax()
    {
        return yMax;
    }

    public float getYGrid()
    {
        return yGrid;
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
        camera.zoom = 1f;
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

    public void calculatePlot(float xMin, float xMax, float xGrid, float yMin, float yMax, float yGrid, PlotCalculatorListener listener)
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.xGrid = xGrid;
        this.yMin = yMin;
        this.yMax = yMax;
        this.yGrid = yGrid;
        if (plotCalculator != null)
        {
            plotCalculator.setAborted();
        }
        plotCalculator = new PlotCalculator(listener);
        new Thread(plotCalculator).start();
    }

    public static class PlotCalculatorListener
    {
        void finished()
        {
        }

        void aborted()
        {
        }
    }

    public class PlotCalculator implements Runnable
    {
        PlotCalculatorListener listener;
        protected String progressText;
        protected boolean aborted;

        public PlotCalculator(PlotCalculatorListener listener)
        {
            this.listener = listener;
        }

        public void run()
        {
            calculate();
        }

        public void setAborted()
        {
            this.aborted = true;
        }

        public boolean isAborted()
        {
            return aborted;
        }

        public void setProgressText(String progressText)
        {
            this.progressText = progressText;
        }

        public String getProgressText()
        {
            return null;
        }

        public void calculate()
        {
            Plot plot = GraphUtil.plotGraph(Graph.this, PLOT_ALGORITHM);

            plot.log.add("screenWidth=" + screenWidth);
            plot.log.add("screenHeight=" + screenHeight);

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
            Graph.this.plot=plot;
        }
    }

    public boolean isCalculating()
    {
        return plotCalculator != null;
    }
}
