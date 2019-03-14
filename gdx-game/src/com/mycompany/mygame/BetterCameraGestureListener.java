package com.mycompany.mygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.glutils.*;

import java.util.*;
import com.badlogic.gdx.input.*;

public class BetterCameraGestureListener extends GestureDetector.GestureAdapter
{
    Axis axis;
    Graph graph;
    Graph.PlotCalculatorListener plotCalculatorListener;
    
    Vector2 screenGrid = null;
    float lastZoom=-1;
    float lastInitialDistance=-1;

    public BetterCameraGestureListener (Axis axis, Graph graph, Graph.PlotCalculatorListener plotCalculatorListener)
    {
        this.axis=axis;
        this.graph=graph;
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

        float graphDeltaX = deltaX * (axis.xGrid / screenGrid.x);
        float graphDeltaY = -deltaY * (axis.xGrid / screenGrid.x);

        boolean changeAxis = false;

        float newXMin = axis.xMin - graphDeltaX;
        float newXMax = axis.xMax - graphDeltaX;
        if (NumberUtil.isReasonable(newXMin) &&
            NumberUtil.isReasonable(newXMax))
        {
            changeAxis = true;
        }
        
        float newYMin = axis.yMin - graphDeltaY;
        float newYMax = axis.yMax - graphDeltaY;
        if (NumberUtil.isReasonable(newYMin) &&
            NumberUtil.isReasonable(newYMax))
        {
            changeAxis = true;
        }

        if (changeAxis)
        {
            if (axis.setWorldSize(newXMin, newXMax, axis.xGrid, newYMin, newYMax, axis.yGrid))
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
        }
        
        return true;
    }
    
    @Override
    public boolean panStop (float x, float y, int pointer, int button)
    {
        screenGrid = null;
        return true;
    }

    @Override
    public boolean zoom(float initialDistance, float distance)
    {
        float zoom=GdxUtil.getCameraZoom();
        if (lastInitialDistance != initialDistance)
        {
            lastInitialDistance = initialDistance;
            lastZoom = GdxUtil.getCameraZoom();
        }
        zoom = lastZoom / (distance / initialDistance);
        if (zoom < 0.01f)
        {
            zoom = 0.01f;
        }
        if (zoom > 100)
        {
            zoom = 100;
        }
        GdxUtil.setCameraZoom(zoom);
        axis.calculateAxis();
        return true;
    }
    
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)
    {
        return true;
    }

    @Override
    public void pinchStop()
    {
    }
    
}
