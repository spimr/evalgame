package com.mycompany.mygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.input.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.viewport.*;
import com.badlogic.gdx.input.GestureDetector.*;
import java.util.*;
import java.util.Vector;

public abstract class AbstractGestureListener extends GestureDetector.GestureAdapter
{
    public static final boolean IMMEDIATE_PLOT_CALCULATION = false;

    Axis axis;
    Graph graph;
    Graph.PlotCalculatorListener plotCalculatorListener;

    Vector<String> logTexts = new Vector<>(); 

    boolean recalculatePlotAfterGesture = false;

    Vector2 screenGrid = null;

    public AbstractGestureListener(Axis axis, Graph graph, Graph.PlotCalculatorListener plotCalculatorListener)
    {
        this.axis = axis;
        this.graph = graph;
        this.plotCalculatorListener = plotCalculatorListener;
    }

    public void initScreenGrid()
    {
        if (screenGrid == null)
        {
            screenGrid = new Vector2();
            axis.graph2Screen(screenGrid, axis.xMin + axis.xGrid, axis.yMin + axis.yGrid);
        }
    }

    public void disposeScreenGrid()
    {
        screenGrid = null;
    }

    public void updateDuringGesture(float xMin, 
                                    float xMax, 
                                    float xGrid, 
                                    float yMin, 
                                    float yMax, 
                                    float yGrid)
    {
        if (axis.setWorldSize(xMin,
                              xMax,
                              xGrid,
                              yMin,
                              yMax,
                              yGrid))
        {
            axis.calculateAxis();
            graph.setPlotObsolete(true);
            if (IMMEDIATE_PLOT_CALCULATION)
            {
                graph.calculatePlot(axis.xMin, 
                                    axis.xMax, 
                                    axis.xGrid, 
                                    axis.yMin, 
                                    axis.yMax, 
                                    axis.yGrid, 
                                    plotCalculatorListener);
            }
            else
            {
                recalculatePlotAfterGesture = true;
            }
        }
    }

    public void updateAfterGesture()
    {
        if (recalculatePlotAfterGesture)
        {
            recalculatePlotAfterGesture = false;
            graph.calculatePlot(axis.xMin, 
                                axis.xMax, 
                                axis.xGrid, 
                                axis.yMin, 
                                axis.yMax, 
                                axis.yGrid, 
                                plotCalculatorListener);
        }
    }

    public void log()
    {
        GdxUtil.log(logTexts);
    }

    public void log(String logText)
    {
        logTexts.add(logText);
    }

    public abstract boolean panImpl(float x, float y, float deltaX, float deltaY);

    @Override
    public final boolean pan(float x, float y, float deltaX, float deltaY)
    {
        boolean result;

        initScreenGrid();

        logTexts.clear();
        log("Gesture Pan");
        log("x=" + x);
        log("y=" + y);
        log("deltaX=" + deltaX);
        log("deltaY=" + deltaY);

        result = panImpl(x, y, deltaX, deltaY);

        log("");

        return result;
    }

    public abstract boolean panStopImpl(float x, float y, int pointer, int button)

    @Override
    public final boolean panStop(float x, float y, int pointer, int button)
    {
        updateAfterGesture();
        disposeScreenGrid();
        panStopImpl(x, y, pointer, button);
        logTexts.clear();
        return true;
    }

    public abstract boolean pinchImpl(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)
    {
        boolean result;

        initScreenGrid();

        logTexts.clear();
        log("Gesture Pinch");
        log("initialPointer1=" + initialPointer1);
        log("initialPointer2=" + initialPointer2);
        log("currentPointer1=" + currentPointer1);
        log("currentPointer2=" + currentPointer2);

        result = pinchImpl(initialPointer1, initialPointer2, currentPointer1, currentPointer2);
        
        log("");
        
        return result;
    }

    public abstract void pinchStopImpl();

    @Override
    public final void pinchStop()
    {
        updateAfterGesture();
        disposeScreenGrid();
        pinchStopImpl();
        logTexts.clear();
    }

}
