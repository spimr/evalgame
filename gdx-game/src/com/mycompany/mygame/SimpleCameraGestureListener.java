package com.mycompany.mygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.glutils.*;

import java.util.*;
import com.badlogic.gdx.input.*;

public class SimpleCameraGestureListener extends GestureDetector.GestureAdapter
{
    Axis axis;
    
    float lastZoom=-1;
    float lastInitialDistance=-1;
    
    public SimpleCameraGestureListener (Axis axis)
    {
        this.axis=axis;
    }
    
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY)
    {
        float zoom=GdxUtil.getCameraZoom();
        GdxUtil.setCameraTranslation(-deltaX * zoom, deltaY * zoom);
        axis.calculateAxis();
        return false;
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
        return false;
    }
}
