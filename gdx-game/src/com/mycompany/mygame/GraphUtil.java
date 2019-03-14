package com.mycompany.mygame;

import com.badlogic.gdx.math.*;
import java.util.*;
import org.apache.http.impl.client.*;
import android.sax.*;

public class GraphUtil
{
    public static int PLOT_ALGORITHM_STATIC_STEP = 1;
    public static int PLOT_ALGORITHM_ADAPTIVE_STEP = 2;

    public static Plot plotGraph(Graph graph,
                                 int algorithm)
    {
        Plot result;
        long startTime = System.currentTimeMillis();
        if (algorithm == PLOT_ALGORITHM_STATIC_STEP)
        {
            result = plotAlgorithmStaticStep(graph);
        }
        else if (algorithm == PLOT_ALGORITHM_ADAPTIVE_STEP)
        {
            result = plotAlgorithmAdaptiveStep(graph);   
        }
        else
        {
            result = new Plot();
            result.log.add("Unknown Algorithm");
        }
        long endTime = System.currentTimeMillis();
        result.log.add("took=" + ((endTime-startTime)) + "ms.");
        result.log.add("");
        return result;
    }

    public static Plot plotAlgorithmStaticStep(Graph graph)
    {
        Plot result = new Plot();
        result.log.add("Static Plot Algorithm");
        result.log.add("f(x)=" + graph.expression);
        result.log.add("started=" + GdxUtil.getTime());
        Vector2 graphPixel = graph.getGraphPixel();
        float step = graphPixel.x * 5; // evaluate every 5th pixel
        result.log.add("step=" + step);

        float xMin = graph.getXMin();
        float xMax = graph.getXMax();
        for (float x = xMin; x < xMax; x += step)
        {
            Vector2 point = null;
            float y = EvalUtil.eval(graph.expression, x);
            if (NumberUtil.isFinite(y))
            {
                point = new Vector2(x, y);
            }
            result.graphPoints.add(point);
            if (point == null)
            {
                result.log.add((point == null ? "SKIP" : "POINT") + "f(" + NumberUtil.toString(x) + ")=" + NumberUtil.toString(y));
            }
        }
        return result;
    }

    public static Plot plotAlgorithmAdaptiveStep(Graph graph)
    {
        Plot result = new Plot();
        result.log.add("Adaptive Plot Algorithm");
        result.log.add("f(x)=" + graph.expression);
        result.log.add("started=" + GdxUtil.getTime());
        Vector2 graphPixel = graph.getGraphPixel();
        float xStep = graphPixel.x * 1; 
        float xTolerance = graphPixel.x;
        float yTolerance = graphPixel.y;
        int maxDepth = 5;

        float xMin = graph.getXMin();
        float xMax = graph.getXMax();
        plotAlgorithmAdaptiveStep(graph, xMin, xMax, xStep, xTolerance, yTolerance, maxDepth, result);
        return result;
    }

    private static void plotAlgorithmAdaptiveStep(Graph graph,
                                                  float xFrom, float xTo,
                                                  float xStep, 
                                                  float xTolerance,
                                                  float yTolerance, 
                                                  int maxDepth, 
                                                  Plot plot)
    {
        float x = xFrom;
        while (x < xTo)
        {
            float x1 =x;
            float x2 = x + xStep;
            float y1 = EvalUtil.eval(graph.expression, x1);
            float y2 = EvalUtil.eval(graph.expression, x2);
            float xm = (x1 + x2) / 2;
            float ym = EvalUtil.eval(graph.expression, xm);
            float yp = Math.min(y1, y2) + Math.abs(y2 - y1) / 2;
            if (Math.abs(ym - yp) <= yTolerance || maxDepth == 0)
            {
                Vector2 point1 = new Vector2(x1, y1);
                plot.graphPoints.add(point1);
            }
            else
            {
                //log.add("RECURSE (" + maxDepth + ") AT " + x);
                plotAlgorithmAdaptiveStep(graph, x1, x2, xStep / 2, xTolerance, yTolerance, maxDepth - 1, plot);
            }
            x = x2;
        }
    }
}
