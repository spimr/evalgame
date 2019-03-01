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

public class EvalGame implements ApplicationListener
{

    public static final int ALIGN_NO = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_TOP = 1;
    public static final int ALIGN_CENTER = 2;

    public static final int PLOT_ALGORITHM = GraphUtil.PLOT_ALGORITHM_ADAPTIVE_STEP;

    public static final boolean PLOT_RED_DOTS = true;
    public static final boolean PLOT_BLUE_DOTS = true;

    // stage and actors
    Stage stage;
    Skin skin;
    String skinFilename = "clean-crispy-skin/skin/clean-crispy-ui.json";
    // "my-skin/uiskin.json";
    // "default-skin/skin/uiskin.json";

    TextField expressionField;
    Drawable expressionFieldValidBackground;
    Drawable expressionFieldInvalidBackground;
    Label expressionStatusLabel;

    ImageTextButton exitButton;
    ImageTextButton resetButton;
    ImageTextButton plusButton;
    ImageTextButton minusButton;

    // batch and shape renderer for graph
    int screenWidth;
    int screenHeight;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    OrthographicCamera camera;
    BitmapFont font;
    GlyphLayout layout;

    Graph graph = new Graph();

    // --------------------------------------------------
    // create method
    // --------------------------------------------------
    @Override
    public void create()
    {
        Gdx.app.setLogLevel(Application.LOG_INFO);

        // stage
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal(skinFilename));
        TextField.TextFieldStyle textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font.getData().scale(0.5f);

        // actors
        Label expressionLabel = new Label("f(x)=", skin);
        expressionField = new TextField(graph.expression, skin);
        expressionField.addListener(new ChangeListener(){
                public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent p1, com.badlogic.gdx.scenes.scene2d.Actor p2)
                {
                    initExpression(expressionField.getText());
                }
            });
        expressionFieldValidBackground = null;
        expressionFieldInvalidBackground = null;
        
        expressionStatusLabel = new Label("", skin);
        expressionStatusLabel.setWrap(true);

        exitButton = GdxUtil.getButton("Exit", "ExitButton-32x32.png", skin);
        exitButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    exitButtonAction();
                    return true;
                }
            });

        resetButton = GdxUtil.getButton("Reset", "ResetButton-32x32.png", skin);
        resetButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    resetButtonAction();
                    return true;
                }
            });

        plusButton = GdxUtil.getButton("Plus", "PlusButton-32x32.png", skin);
        plusButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    plusButtonAction();
                    return true;
                }
            });

        minusButton = GdxUtil.getButton("Minus", "MinusButton-32x32.png", skin);
        minusButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    minusButtonAction();
                    return true;
                }
            });

        Table expressionTable = new Table();
        expressionTable.add(expressionLabel).right();
        expressionTable.add(expressionField).expandX().fillX();
        expressionTable.row();
        expressionTable.add(new Label("", skin));
        expressionTable.add(expressionStatusLabel).expandX().fillX();

        Table buttonTable = GdxUtil.getButtonTable(new Button[] {exitButton, resetButton, plusButton, minusButton}, true);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.add(new Label("", skin)).expandX().fillX().space(GdxUtil.TABLE_SPACING);
        mainTable.add(expressionTable).expandX().fillX().top().padTop(3).space(GdxUtil.TABLE_SPACING);
        mainTable.add(buttonTable).top().space(GdxUtil.TABLE_SPACING);
        mainTable.right().top().pad(GdxUtil.TABLE_PADDING);

        stage.addActor(mainTable);

        // graph related (low level gdx)
        //screenWidth = Gdx.graphics.sc
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        font = skin.get("graph-font", BitmapFont.class);
        layout = new GlyphLayout();

        // imput processor
        // GestureDetector graphInputProcessor = new GestureDetector(new ExploreGraphListener());
        GestureDetector graphInputProcessor = new GestureDetector(new CameraDrivenGestureListener(camera));
        InputMultiplexer inputMultiplexer = new InputMultiplexer(stage, graphInputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // init
        graph.setGdxObjects(batch, shapeRenderer, camera);
        initExpression(graph.expression);
    }

    public void exitButtonAction()
    {
        Gdx.app.exit();
    }

    public void resetButtonAction()
    {
        graph.calculateDomain();
        graph.calculatePlot(PLOT_ALGORITHM);
    }

    protected void minusButtonAction()
    {
        graph.setCameraZoom(camera.zoom + 0.1f);
    }

    protected void plusButtonAction()
    {
        graph.setCameraZoom(camera.zoom - 0.1f);
    }

    public void initExpression(String newExpression)
    {
        String newExpressionTrimmed = newExpression == null ? "" : newExpression.trim();
        boolean validExpression = true;
        if (newExpressionTrimmed.length() == 0)
        {
            graph.expressionStatus = "Enter expression (e.g. 3*x^2)";
        }
        else
        {
            try
            {
                EvalUtil.check(newExpressionTrimmed);
                graph.expression = newExpressionTrimmed;
                graph.expressionStatus = "";
            }
            catch (EvalUtil.SyntaxException ex)
            {
                graph.expressionStatus = ex.getMessage();
                validExpression = false;
            }
        }
        expressionStatusLabel.setText(graph.expressionStatus);
        expressionStatusLabel.setWidth(expressionField.getWidth());
        expressionStatusLabel.setHeight(expressionStatusLabel.getPrefHeight());
        expressionStatusLabel.getStyle().font.setColor(validExpression ? Color.BLACK : Color.RED);
        expressionStatusLabel.invalidate();

        if (validExpression)
        {
            graph.calculateDomain();
            graph.calculatePlot(PLOT_ALGORITHM);
        }
        // expressionField.getStyle().background = getExpressionFieldBackground(validExpression);
    }

    public Drawable getExpressionFieldBackground(boolean validExpression)
    {
        Drawable result = validExpression ? expressionFieldValidBackground : expressionFieldInvalidBackground;
        if (result == null)
        {
            Color color = validExpression ? Color.WHITE : Color.RED;
            float width = expressionField.getWidth();
            float height = expressionField.getHeight();
            result = GdxUtil.getFilledRectangle(width, height, color);
            if (validExpression)
            {
                expressionFieldValidBackground = result;
            }
            else
            {
                expressionFieldInvalidBackground = result;
            }
        }   
        return result;
    }


    // --------------------------------------------------
    // render method
    // --------------------------------------------------
    Vector3 projectVector = new Vector3();
    Vector2 start = new Vector2();
    Vector2 end = new Vector2();
    ArrayList<String> logTexts = new ArrayList<>();

    @Override
    public void render()
    {
        // init
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        logTexts.clear();

        // log
        logTexts.add("screenWidth=" + screenWidth);
        logTexts.add("screenHeight=" + screenHeight);
        logTexts.add("x=<" + NumberUtil.toString(graph.xMin, 3) + "," + NumberUtil.toString(graph.xMax, 3) + "> grid " + NumberUtil.toString(graph.xGrid, 3));
        logTexts.add("y=<" + NumberUtil.toString(graph.yMin, 3) + "," + NumberUtil.toString(graph.yMax, 3) + "> grid " + NumberUtil.toString(graph.yGrid, 3));
        logTexts.add("fps=" + Math.round(1 / Gdx.graphics.getDeltaTime()));

        // begin
        startBatch();

        // x-labels
        renderXAxis();

        // y-labels
        for (float y = graph.yMin + graph.yGrid; y < graph.yMax; y += graph.yGrid)
        {
            graph.graph2Screen(start, graph.xMin, y);
            graph.graph2Screen(end, graph.xMax, y);
            renderLine(start, end, 1, NumberUtil.isZero(y) ? Color.BLACK : Color.LIGHT_GRAY);

            if (NumberUtil.isZero(y))
            {
                continue;
            }

            graph.graph2Screen(start, -5 * (graph.xMax - graph.xMin) / screenWidth * camera.zoom, y);
            graph.graph2Screen(end, 5 * (graph.xMax - graph.xMin) / screenWidth * camera.zoom, y);
            renderLine(start, end, 3, Color.BLACK);

            String label = NumberUtil.toGridString(y, graph.yGrid);
            graph.graph2Screen(start, 0, y);
            projectVector.set(start, 0);
            camera.project(projectVector);
            renderText(label, projectVector.x - 10, projectVector.y, ALIGN_LEFT, ALIGN_CENTER);
        }

        // graph
        renderGraph();

        // buttons and texts
        setSettings(ShapeRenderer.ShapeType.Line, 1f);
        renderLogTexts();

        // stage
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // end
        endBatch();
    }

    // --------------------------------------------------
    // Batch, ShapeRenderer and Camera
    // --------------------------------------------------
    boolean batchStarted = false;
    ShapeRenderer.ShapeType shapeType;
    float lineWidth;

    protected void setSettings(ShapeRenderer.ShapeType shapeType, float lineWidth)
    {
        if (this.shapeType != shapeType || this.lineWidth != lineWidth)
        {
            if (batchStarted)
            {
                endBatch();
            }
            Gdx.gl.glLineWidth(lineWidth);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(shapeType);
            batch.begin();
            batchStarted = true;
            this.shapeType = shapeType;
            this.lineWidth = lineWidth;
        }
    }

    protected void startBatch()
    {
        shapeType = null;
        lineWidth = 0;
        batchStarted = false;
        setSettings(ShapeRenderer.ShapeType.Line, 1f);
    }

    protected void endBatch()
    {
        batch.end();
        shapeRenderer.end();
        batchStarted = false;
    }

    // --------------------------------------------------
    // Axis
    // --------------------------------------------------
    protected void renderXAxis()
    {
        for (float x = graph.xMin + graph.xGrid; x < graph.xMax; x += graph.xGrid)
        {
            graph.graph2Screen(start, x, graph.yMin);
            graph.graph2Screen(end, x, graph.yMax);
            renderLine(start, end, 1, NumberUtil.isZero(x) ? Color.BLACK : Color.LIGHT_GRAY);

            graph.graph2Screen(start, x, -5 * (graph.yMax - graph.yMin) / screenHeight * camera.zoom);
            graph.graph2Screen(end, x, 5 * (graph.yMax - graph.yMin) / screenHeight * camera.zoom);
            renderLine(start, end, 3, Color.BLACK);

            String label = NumberUtil.toGridString(x, graph.xGrid);
            renderXAxisText(label, x, 0);
        }
    }

    Vector2 renderXAxisText_vector2 = new Vector2();
    Vector3 renderXAxisText_vector3 = new Vector3();
    protected void renderXAxisText(String text, float graphX, float line)
    {
        graph.graph2Screen(renderXAxisText_vector2, graphX, 0);
        renderXAxisText_vector3.set(renderXAxisText_vector2, 0);
        camera.project(renderXAxisText_vector3);
        renderText(text, renderXAxisText_vector3.x, renderXAxisText_vector3.y - 10 - line * 20, ALIGN_CENTER, ALIGN_NO);
    }

    // --------------------------------------------------
    // Graph
    // --------------------------------------------------
    Vector2 blueDot = new Vector2();

    public void renderGraph()
    {
        logTexts.addAll(graph.plot.log);

        boolean startInitialized = false;
        boolean endInitialized = false;
        long gridPointsCount =0;
        float gridPointsToX = graph.graphX2UpperSnapX(graph.xMin + graph.xGrid);
        for (Vector2 point : graph.plot.points)
        {
            if (point != null && point.x > -0.3 && point.x < -0.2)
            {
                logTexts.add(NumberUtil.toString(point, 4));
            }
            // prepare end point
            if (point != null)
            {
                graph.graph2Screen(end, point.x, point.y);
                float snapX = graph.graphX2UpperSnapX(point.x);
                if (snapX <= gridPointsToX)
                {
                    gridPointsCount++;
                }
                else 
                {
                    renderXAxisText(Long.toString(gridPointsCount), gridPointsToX - graph.xGrid / 2, 1);
                    gridPointsCount = 1;
                    gridPointsToX = snapX;
                }
            }
            endInitialized = point != null;

            // render line from start point to end point
            if (startInitialized && endInitialized)
            {
                renderLine(start, end, 1, Color.BLACK);
            }

            // render red blob around end point
            if (PLOT_RED_DOTS && endInitialized)
            {
                GdxUtil.renderFilledRectangle(shapeRenderer, camera, end.x, end.y, Color.RED, 4);
            }

            // render blue blob around end point x axis projectio 
            if (PLOT_BLUE_DOTS && endInitialized)
            {
                graph.graph2Screen(blueDot, point.x, 0);
                GdxUtil.renderFilledRectangle(shapeRenderer, camera, blueDot.x, blueDot.y, Color.BLUE, 4);
            }

            // prepare start point
            if (end != null)
            {
                start.set(end);
            }
            startInitialized = endInitialized;
        }
        renderXAxisText(Long.toString(gridPointsCount), gridPointsToX - graph.xGrid / 2, 1);
    }

    // --------------------------------------------------
    // render text
    // --------------------------------------------------
    int renderText_TextYOffset;
    Vector2 renderText_RenderedTextSize = new Vector2();
    Vector2 renderText_RenderedTextBoxSize = new Vector2();
    Vector3 renderText_ProjectVector = new Vector3();

    protected void renderLogTexts()
    {
        renderText_TextYOffset = 10;
        for (String logText : logTexts)
        {
            renderLogText(logText);
        }
        renderLogText("zoom=" + camera.zoom);
    }
    protected void renderLogText(String text)
    {
        renderText(text, 10, screenHeight - renderText_TextYOffset, ALIGN_NO, ALIGN_NO);
        renderText_TextYOffset += renderText_RenderedTextSize.y + 5;
    }

    protected void renderText(String text, float x, float y, int halign, int valign)
    {
        layout.setText(font, text);
        renderText_RenderedTextSize.set(layout.width, layout.height);
        renderText_RenderedTextBoxSize.set(renderText_RenderedTextSize.x + 4, renderText_RenderedTextSize.y + 4);
        if (halign == ALIGN_LEFT)
        {
            x = x - renderText_RenderedTextBoxSize.x;
        }
        else if (halign == ALIGN_CENTER)
        {
            x = x - renderText_RenderedTextBoxSize.x / 2;
        }
        if (valign == ALIGN_TOP)
        {
            y = y + renderText_RenderedTextBoxSize.y;
        }
        else if (valign == ALIGN_CENTER)
        {
            y = y + renderText_RenderedTextBoxSize.y / 2;
        }
        renderText_ProjectVector.set(x, screenHeight - y, 0);
        camera.unproject(renderText_ProjectVector);
        float rectX = renderText_ProjectVector.x;
        float rectY = renderText_ProjectVector.y;
        float rectWidth = camera.zoom * renderText_RenderedTextBoxSize.x;
        float rectHeight = camera.zoom * renderText_RenderedTextBoxSize.y;
        setSettings(ShapeRenderer.ShapeType.Filled, 1f);   
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(rectX, rectY - rectHeight, rectWidth, rectHeight);
        setSettings(ShapeRenderer.ShapeType.Line, 1f);
        font.draw(batch, layout, x + 2, y - 3);
    }

    // --------------------------------------------------
    // render line
    // --------------------------------------------------
    protected void renderLine(Vector2 start, Vector2 end, float lineWidth, Color color)
    {
        shapeRenderer.setColor(color);
        setSettings(shapeType, lineWidth);
        shapeRenderer.line(start, end);
    }

    // --------------------------------------------------
    // application listener related
    // --------------------------------------------------
    @Override
    public void resize(int width, int height)
    {
        // resize is called 
        // - directly after create
        // - when screen is resized (orientation change, bottom control panel shown)
        // in both cases we have to pass new screen dimensions to various gdx objects
        screenWidth = width;
        screenHeight = height;
        stage.getViewport().update(screenWidth, screenHeight, true);
        
        graph.setScreenSize (screenWidth, screenHeight);
        graph.calculatePlot(PLOT_ALGORITHM);
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resume()
    {
    }

    // --------------------------------------------------
    // render text
    // --------------------------------------------------
    protected class ExploreGraphListener extends GestureDetector.GestureAdapter
    {
        int zoomExponent;
        Vector2 screenGrid = null;
        float panSumDeltaX;
        float panSumDeltaY;
        float pinchAppliedDeltaX;
        float pinchAppliedDeltaY;

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY)
        {
            if (screenGrid == null)
            {
                screenGrid = new Vector2();
                graph.graph2Screen(screenGrid, graph.xMin + graph.xGrid, graph.yMin + graph.yGrid);
            }

            panSumDeltaX = panSumDeltaX + deltaX;
            float graphDeltaX = (int) (panSumDeltaX / screenGrid.x);
            panSumDeltaX = panSumDeltaX - graphDeltaX * screenGrid.x;

            panSumDeltaY = panSumDeltaY + deltaY;
            float graphDeltaY = (int) (panSumDeltaY / screenGrid.y);
            panSumDeltaY = panSumDeltaY - graphDeltaY * screenGrid.y;

            boolean changed = false;

            float newXMin = graph.xMin - graphDeltaX * graph.xGrid;
            float newXMax = graph.xMax - graphDeltaX * graph.xGrid;
            if (NumberUtil.isReasonable(newXMin) &&
                NumberUtil.isReasonable(newXMax))
            {
                graph.xMin = newXMin;
                graph.xMax = newXMax;
                changed = true;
            }

            float newYMin = graph.yMin + graphDeltaY * graph.yGrid;
            float newYMax = graph.yMax + graphDeltaY * graph.yGrid;
            if (NumberUtil.isReasonable(newYMin) &&
                NumberUtil.isReasonable(newYMax))
            {
                graph.yMin = newYMin;
                graph.yMax = newYMax;
                changed = true;
            }

            if (changed)
            {
                graph.calculatePlot(PLOT_ALGORITHM);
            }

            return true;
        }

        @Override
        public boolean panStop(float p1, float p2, int p3, int p4)
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
                graph.graph2Screen(screenGrid, graph.xMin + graph.xGrid, graph.yMin + graph.yGrid);
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
                float newXGrid = (float) (graph.xGrid * Math.pow(zoomExponent, exponentX));
                float newXMin = (float) (graph.xMin * Math.pow(zoomExponent, exponentX));
                float newXMax = (float) (graph.xMax * Math.pow(zoomExponent, exponentX));

                if (NumberUtil.isReasonable(newXGrid) &&
                    NumberUtil.isReasonable(newXMin) &&
                    NumberUtil.isReasonable(newXMax))
                {
                    graph.xGrid = newXGrid;
                    graph.xMin = newXMin;
                    graph.xMax = newXMax;
                    changed = true;
                }
            }
            if (exponentY != 0)
            {
                float newYGrid =  (float) (graph.yGrid * Math.pow(zoomExponent, exponentY));
                float newYMin = (float) (graph.yMin * Math.pow(zoomExponent, exponentY));
                float newYMax = (float) (graph.yMax * Math.pow(zoomExponent, exponentY));

                if (NumberUtil.isReasonable(newYGrid) &&
                    NumberUtil.isReasonable(newYMin) &&
                    NumberUtil.isReasonable(newYMax))
                {
                    graph.yGrid = newYGrid;
                    graph.yMin = newYMin;
                    graph.yMax = newYMax;
                    changed = true;
                }
            }

            if (changed)
            {
                graph.calculatePlot(PLOT_ALGORITHM);
            }
            return true;
        }

        public void pinchStop()
        {
            screenGrid = null;
            pinchAppliedDeltaX = 0;
            pinchAppliedDeltaY = 0;
        }
    }
}
