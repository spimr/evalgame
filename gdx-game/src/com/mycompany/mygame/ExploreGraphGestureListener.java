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

public class ExploreGraphGestureListener extends GestureDetector.GestureAdapter
{
    Axis axis;
    Graph graph;
    Graph.PlotCalculatorListener plotCalculatorListener;

    int zoomExponent;
    Vector2 screenGrid = null;
    float panSumDeltaX;
    float panSumDeltaY;
    float pinchAppliedDeltaX;
    float pinchAppliedDeltaY;

    public ExploreGraphGestureListener(Axis axis, Graph graph, Graph.PlotCalculatorListener plotCalculatorListener)
    {
        this.axis = axis;
        this.graph = graph;
        this.plotCalculatorListener=plotCalculatorListener;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY)
    {
        if (screenGrid == null)
        {
            screenGrid = new Vector2();
            axis.graph2Screen(screenGrid, axis.xMin + axis.xGrid, axis.yMin + axis.yGrid);
        }

        panSumDeltaX = panSumDeltaX + deltaX;
        float graphDeltaX = (int) (panSumDeltaX / screenGrid.x);
        panSumDeltaX = panSumDeltaX - graphDeltaX * screenGrid.x;

        panSumDeltaY = panSumDeltaY + deltaY;
        float graphDeltaY = (int) (panSumDeltaY / screenGrid.y);
        panSumDeltaY = panSumDeltaY - graphDeltaY * screenGrid.y;

        boolean changed = false;

        float newXMin = axis.xMin - graphDeltaX * axis.xGrid;
        float newXMax = axis.xMax - graphDeltaX * axis.xGrid;
        if (NumberUtil.isReasonable(newXMin) &&
            NumberUtil.isReasonable(newXMax))
        {
            axis.xMin = newXMin;
            axis.xMax = newXMax;
            changed = true;
        }

        float newYMin = axis.yMin + graphDeltaY * axis.yGrid;
        float newYMax = axis.yMax + graphDeltaY * axis.yGrid;
        if (NumberUtil.isReasonable(newYMin) &&
            NumberUtil.isReasonable(newYMax))
        {
            axis.yMin = newYMin;
            axis.yMax = newYMax;
            changed = true;
        }

        if (changed)
        {
            axis.calculateAxis();
            graph.calculatePlot(newXMin,
                                newXMax,
                                axis.xGrid,
                                newYMin,
                                newYMax,
                                axis.yGrid,
                                plotCalculatorListener);
        }

        return true;
    }

    @Override
    public boolean panStop (float x, float y, int pointer, int button)
    {
        screenGrid = null;
        panSumDeltaX = 0;
        panSumDeltaY = 0;
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)
    {
        if (screenGrid == null)
        {
            screenGrid = new Vector2();
            axis.graph2Screen(screenGrid, axis.xMin + axis.xGrid, axis.yMin + axis.yGrid);
        }

        Vector2 graphInitialPointerCenter = new Vector2();
        float initialPointerCenterX = (initialPointer1.x + initialPointer2.x) / 2;
        float initialPointerCenterY = (initialPointer1.y + initialPointer2.y) / 2;
        graph.screen2Graph(graphInitialPointerCenter, initialPointerCenterX, initialPointerCenterY, true);

        float initialDeltaX = Math.abs(initialPointer2.x - initialPointer1.x);
        float currentDeltaX = Math.abs(currentPointer2.x - currentPointer1.x);
        float pinchDeltaX = currentDeltaX - initialDeltaX;
        float graphDeltaX = (int) ((pinchDeltaX - pinchAppliedDeltaX) / screenGrid.x);
        pinchAppliedDeltaX = pinchAppliedDeltaX + graphDeltaX * screenGrid.x;
        int exponentX = -(int) graphDeltaX;

        float initialDeltaY = Math.abs(initialPointer2.y - initialPointer1.y);
        float currentDeltaY = Math.abs(currentPointer2.y - currentPointer1.y);
        float pinchDeltaY = currentDeltaY - initialDeltaY;
        float graphDeltaY = (int) ((pinchDeltaY - pinchAppliedDeltaY) / screenGrid.y);
        pinchAppliedDeltaY = pinchAppliedDeltaY + graphDeltaY * screenGrid.y;
        int exponentY = -(int) graphDeltaY;
        boolean changed = false;
        if (exponentX != 0)
        {
            float newXGrid = (float) (axis.xGrid * Math.pow(zoomExponent, exponentX));
            float newXMin = (float) (axis.xMin * Math.pow(zoomExponent, exponentX));
            float newXMax = (float) (axis.xMax * Math.pow(zoomExponent, exponentX));

            if (NumberUtil.isReasonable(newXGrid) &&
                NumberUtil.isReasonable(newXMin) &&
                NumberUtil.isReasonable(newXMax))
            {
                axis.xGrid = newXGrid;
                axis.xMin = newXMin;
                axis.xMax = newXMax;
                changed = true;
            }
        }
        if (exponentY != 0)
        {
            float newYGrid =  (float) (axis.yGrid * Math.pow(zoomExponent, exponentY));
            float newYMin = (float) (axis.yMin * Math.pow(zoomExponent, exponentY));
            float newYMax = (float) (axis.yMax * Math.pow(zoomExponent, exponentY));

            if (NumberUtil.isReasonable(newYGrid) &&
                NumberUtil.isReasonable(newYMin) &&
                NumberUtil.isReasonable(newYMax))
            {
                axis.yGrid = newYGrid;
                axis.yMin = newYMin;
                axis.yMax = newYMax;
                changed = true;
            }
        }

        if (changed)
        {
            axis.calculateAxis();
            graph.calculatePlot(axis.xMin, 
                                axis.xMax, 
                                axis.xGrid, 
                                axis.yMin, 
                                axis.yMax, 
                                axis.yGrid, 
                                plotCalculatorListener);
        }
        return true;
    }

    @Override
    public void pinchStop()
    {
        screenGrid = null;
        pinchAppliedDeltaX = 0;
        pinchAppliedDeltaY = 0;
    }
    
}
