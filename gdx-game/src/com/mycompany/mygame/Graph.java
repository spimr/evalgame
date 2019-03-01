package com.mycompany.mygame;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;

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

    public void calculatePlot(int plotAlgorith)
    {
        plot = GraphUtil.plotGraph(this, plotAlgorith);
    }
}
