package com.mycompany.mygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.glutils.*;

import java.util.*;
import com.badlogic.gdx.input.*;

public class CameraDrivenGestureListener extends GestureDetector.GestureAdapter
{
    OrthographicCamera camera;
    
    float lastZoom=-1;
    float lastInitialDistance=-1;
    
    public CameraDrivenGestureListener (OrthographicCamera camera)
    {
        this.camera=camera;
    }
    
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY)
    {
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        camera.update();
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance)
    {
        if (lastInitialDistance != initialDistance)
        {
            lastInitialDistance = initialDistance;
            lastZoom = camera.zoom;
        }
        float zoom=camera.zoom;
        zoom = lastZoom / (distance / initialDistance);
        if (zoom < 0.01f)
        {
            zoom = 0.01f;
        }
        if (zoom > 100)
        {
            zoom = 100;
        }
        camera.zoom = zoom;
        camera.update();
        return false;
    }
}
