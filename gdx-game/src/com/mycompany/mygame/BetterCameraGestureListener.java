package com.mycompany.mygame;

import com.badlogic.gdx.math.*;

public class BetterCameraGestureListener extends AbstractGestureListener
{
    float lastZoom=-1;
    float lastInitialDistance=-1;

    public BetterCameraGestureListener(Axis axis, Graph graph, Graph.PlotCalculatorListener plotCalculatorListener)
    {
        super(axis, graph, plotCalculatorListener);
    }

    @Override
    public boolean panImpl(float x, float y, float deltaX, float deltaY)
    {
        float zoom = GdxUtil.getCameraZoom();
        float graphDeltaX = deltaX * (axis.xGrid / screenGrid.x) * zoom;
        float graphDeltaY = -deltaY * (axis.yGrid / screenGrid.y) * zoom;

        float newXMin = axis.xMin - graphDeltaX;
        float newXMax = axis.xMax - graphDeltaX;

        float newYMin = axis.yMin - graphDeltaY;
        float newYMax = axis.yMax - graphDeltaY;

        updateDuringGesture(newXMin, 
                            newXMax, 
                            axis.xGrid, 
                            newYMin, 
                            newYMax, 
                            axis.yGrid);

        return true;                            
    }

    public boolean panStopImpl(float x, float y, int pointer, int button)
    {
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
    public boolean pinchImpl(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 currentPointer1, Vector2 currentPointer2)
    {
        return true;
    }

    @Override
    public void pinchStopImpl()
    {
    }

    // ---------------------------------------------------------------------
    // grid = M*10^E
    // where M in {1, 2, 5}
    //       E in Integer
    //
    // example to get larger grid
    // 1,      2,      5,      10,     20,     50,     100,    ...
    // 1*10^0, 2*10^0, 5*10^0, 1*10^1, 2*10^1, 5*10^1, 1*10^2, ...
    //
    // example to get smaller grid
    // 0.5,     0.2,     0.1,     0.05,    0.02,    0.01,    0.005, ...
    // 5*10^-1, 2*10^-1, 1*10^-1, 5*10^-2, 2*10^-2, 1*10^-2, 5*10^-3, ...
    //
    // calculation of M and E from the given grid
    // float grid = ...;
    // int M = 0;
    // int E = 0;
    // for (M in {1, 2, 5})
    //    float e=log(grid/M,10)
    //    if (Math.abs (e-Math.round(e)) < 0.00001)
    //      E=Math.round(e);
    //      break;
    //    end if
    // end for
    // ---------------------------------------------------------------------
    /*
    public static float getLargerGrid(float grid)
    {
        float result = 0;
        return result;
    }

    public static float getLargerGrid(float grid)
    {
        float result = 0;
        return result;
    }
    */
}
