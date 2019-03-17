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

public class ExploreGraphGestureListener extends AbstractGestureListener
{
    private static final int ZOOM_EXPONENT=2;

    float panSumDeltaX;
    float panSumDeltaY;
    float pinchAppliedDeltaX;
    float pinchAppliedDeltaY;

    public ExploreGraphGestureListener(Axis axis, Graph graph, Graph.PlotCalculatorListener plotCalculatorListener)
    {
        super(axis, graph, plotCalculatorListener);
    }

    @Override
    public boolean panImpl(float x, float y, float deltaX, float deltaY)
    {
        panSumDeltaX = panSumDeltaX + deltaX;
        float graphDeltaX = (int) (panSumDeltaX / screenGrid.x);
        panSumDeltaX = panSumDeltaX - graphDeltaX * screenGrid.x;

        panSumDeltaY = panSumDeltaY + deltaY;
        float graphDeltaY = (int) (panSumDeltaY / screenGrid.y);
        panSumDeltaY = panSumDeltaY - graphDeltaY * screenGrid.y;

        float newXMin = axis.xMin - graphDeltaX * axis.xGrid;
        float newXMax = axis.xMax - graphDeltaX * axis.xGrid;
        
        float newYMin = axis.yMin + graphDeltaY * axis.yGrid;
        float newYMax = axis.yMax + graphDeltaY * axis.yGrid;

        updateDuringGesture(newXMin,
                            newXMax,
                            axis.xGrid,
                            newYMin,
                            newYMax,
                            axis.yGrid);

        return true;
    }

    @Override
    public boolean panStopImpl(float x, float y, int pointer, int button)
    {
        panSumDeltaX = 0;
        panSumDeltaY = 0;
        return true;
    }

    @Override
    public boolean pinchImpl(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)
    {
        // Vector2 graphInitialPointerCenter = new Vector2();
        // float initialPointerCenterX = (initialPointer1.x + initialPointer2.x) / 2;
        // float initialPointerCenterY = (initialPointer1.y + initialPointer2.y) / 2;
        // graph.screen2Graph(graphInitialPointerCenter, initialPointerCenterX, initialPointerCenterY, true);

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
        
        float newXGrid = (float) (axis.xGrid * Math.pow(ZOOM_EXPONENT, exponentX));
        float newXMin = (float) (axis.xMin * Math.pow(ZOOM_EXPONENT, exponentX));
        float newXMax = (float) (axis.xMax * Math.pow(ZOOM_EXPONENT, exponentX));
        
        float newYGrid =  (float) (axis.yGrid * Math.pow(ZOOM_EXPONENT, exponentY));
        float newYMin = (float) (axis.yMin * Math.pow(ZOOM_EXPONENT, exponentY));
        float newYMax = (float) (axis.yMax * Math.pow(ZOOM_EXPONENT, exponentY));

        updateDuringGesture(newXMin,
                            newXMax,
                            newXGrid,
                            newYMin,
                            newYMax,
                            newYGrid);

        return true;
    }

    @Override
    public void pinchStopImpl()
    {
        pinchAppliedDeltaX = 0;
        pinchAppliedDeltaY = 0;
    }

}
