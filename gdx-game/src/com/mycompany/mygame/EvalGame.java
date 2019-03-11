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

public class EvalGame implements ApplicationListener
{
    private static final boolean USE_LINES_FOR_FILLED_RENDERING = false;
    
    public static final int ALIGN_NO = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_TOP = 1;
    public static final int ALIGN_CENTER = 2;

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
    int screenHeight;
    SpriteBatch batch;
    ShapeRenderer renderer;
    OrthographicCamera camera;
    BitmapFont font;
    GlyphLayout layout;

    Axis axis;
    Graph graph;
    GraphUpdater graphUpdater=new GraphUpdater();

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
        expressionField = new TextField("", skin);
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
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        font = skin.get("graph-font", BitmapFont.class);
        layout = new GlyphLayout();

        // init
        axis = new Axis(camera);
        graph = new Graph(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        graph.setGdxObjects(camera);
        initExpression(graph.expression);

        // input processor
        // GestureDetector graphInputProcessor = new GestureDetector(new ExploreGraphListener());
        GestureDetector graphInputProcessor = new GestureDetector(new CameraDrivenGestureListener(axis, graph));
        InputMultiplexer inputMultiplexer = new InputMultiplexer(stage, graphInputProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void exitButtonAction()
    {
        Gdx.app.exit();
    }

    public void resetButtonAction()
    {
        graph.calculateDomain();
        axis.setWorldSize(graph.getXMin(), graph.getXMax(), graph.getXGrid(), graph.getYMin(), graph.getYMax(), graph.getYGrid());
        axis.calculateAxis();
        graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
    }

    protected void minusButtonAction()
    {
        setCameraZoom(camera.zoom + 0.1f);
    }

    protected void plusButtonAction()
    {
        setCameraZoom(camera.zoom - 0.1f);
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
            axis.setWorldSize(graph.getXMin(), graph.getXMax(), graph.getXGrid(), graph.getYMin(), graph.getYMax(), graph.getYGrid());
            axis.calculateAxis();
            graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
        }
    }

    // --------------------------------------------------
    // Batch, ShapeRenderer and Camera
    // --------------------------------------------------
    boolean batchStarted = false;
    int batchStartCount =0;

    boolean rendererStarted = false;
    int rendererStartCount=0;
    ShapeRenderer.ShapeType shapeType;
    float lineWidth;

    protected void useBatch()
    {
        if (!batchStarted)
        {
            endBatchAndRenderer();
        }
        batch.begin();
        batchStarted = true;
        batchStartCount++;
    }

    protected void useRenderer(ShapeRenderer.ShapeType shapeType, float lineWidth)
    {
        if (shapeType == null)
        {
            shapeType = this.shapeType;
        }
        if (this.shapeType != shapeType || this.lineWidth != lineWidth || !rendererStarted)
        {
            endBatchAndRenderer();
            Gdx.gl.glLineWidth(lineWidth);
            renderer.setProjectionMatrix(camera.combined);
            renderer.begin(shapeType);
            rendererStarted = true;
            rendererStartCount++;
            this.shapeType = shapeType;
            this.lineWidth = lineWidth;
        }
    }

    protected void initBatchAndRenderer()
    {
        batchStartCount = 0;
        rendererStartCount = 0;
        initTextBuffer();
        shapeType = ShapeRenderer.ShapeType.Line;
        lineWidth = 1f;
    }

    protected void endBatchAndRenderer()
    {
        if (batchStarted)
        {
            batch.end();
            batchStarted = false;
        }
        if (rendererStarted)
        {
            renderer.end();
            rendererStarted = false;
        }
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
        logTexts.add("x=<" + NumberUtil.toString(axis.xMin, 3) + "," + NumberUtil.toString(axis.xMax, 3) + "> grid " + NumberUtil.toString(axis.xGrid, 3));
        logTexts.add("y=<" + NumberUtil.toString(axis.yMin, 3) + "," + NumberUtil.toString(axis.yMax, 3) + "> grid " + NumberUtil.toString(axis.yGrid, 3));
        logTexts.add("zoom=" + camera.zoom);
        logTexts.add("fps=" + Math.round(1 / Gdx.graphics.getDeltaTime()));

        // begin
        initBatchAndRenderer();

        // axis
        renderXAxisLines();
        renderYAxisLines();
        renderXAxisMarks();
        renderYAxisMarks();

        // graph
        renderGraph();

        // texts
        renderXAxisLabels();
        renderYAxisLabels();

        logTexts.add("rendererStartCount=" + rendererStartCount);
        renderLogTexts();

        // end
        renderTextBuffer2Screen();
        endBatchAndRenderer();

        // stage
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

    }

    // --------------------------------------------------
    // Axis
    // --------------------------------------------------
    protected void renderXAxisLines()
    {
        for (int i=0; i < axis.yAxisLineStart.size(); i++)
        {
            renderLine(axis.yAxisLineStart.get(i), axis.yAxisLineEnd.get(i), //
                       1, //
                       Color.LIGHT_GRAY);
        }
    }

    protected void renderXAxisMarks()
    {
        for (int i=0; i < axis.yAxisMarkStart.size(); i++)
        {
            renderLine(axis.yAxisMarkStart.get(i), axis.yAxisMarkEnd.get(i), //
                       3, //
                       Color.BLACK);
        }
    }

    protected void renderXAxisLabels()
    {
        for (int i=0; i < axis.xAxisText.size(); i++)
        {
            renderText(axis.xAxisText.get(i), 
                       axis.xAxisTextPosition.get(i).x,
                       axis.xAxisTextPosition.get(i).y,
                       ALIGN_CENTER, ALIGN_NO);
        }
    }

    protected void renderYAxisLines()
    {
        for (int i=0; i < axis.xAxisLineStart.size(); i++)
        {
            renderLine(axis.xAxisLineStart.get(i), axis.xAxisLineEnd.get(i), //
                       1, //
                       Color.LIGHT_GRAY);
        }
    }

    protected void renderYAxisMarks()
    {
        for (int i=0; i < axis.xAxisMarkStart.size(); i++)
        {
            renderLine(axis.xAxisMarkStart.get(i), axis.xAxisMarkEnd.get(i), //
                       3, //
                       Color.BLACK);
        }
    }

    protected void renderYAxisLabels()
    {
        for (int i=0; i < axis.yAxisText.size(); i++)
        {
            renderText(axis.yAxisText.get(i), 
                       axis.yAxisTextPosition.get(i).x,
                       axis.yAxisTextPosition.get(i).y,
                       ALIGN_LEFT, ALIGN_CENTER);
        }
    }

    // --------------------------------------------------
    // Graph
    // --------------------------------------------------
    public void renderGraph()
    {
        if (graph.plot != null)
        {
            Vector2 start =null;
            Vector2 end=null;
            logTexts.addAll(graph.plot.log);

            for (int i=0; i < graph.plot.graphPoints.size(); i++)
            {
                end = graph.plot.screenPoints.get(i);
                if (start != null && end != null)
                {
                    renderLine(start, end, 1, Color.BLACK);
                }
                start = end;
            }

            float dotSize = 4 * camera.zoom;
            for (int i=0; i < graph.plot.graphPoints.size(); i++)
            {
                end = graph.plot.screenPoints.get(i);
                Vector2 blueDot = graph.plot.blueDotScreenPosition.get(i);

                if (end != null && Graph.PLOT_RED_DOTS)
                {
                    renderDot(end.x, end.y, dotSize, Color.RED);
                }
                if (blueDot != null && Graph.PLOT_BLUE_DOTS)
                {   
                    renderDot(blueDot.x, blueDot.y, dotSize, Color.BLUE);
                }
            }

            for (int i=0; i < graph.plot.pointsCountXAxisText.size(); i++)
            {
                String text = graph.plot.pointsCountXAxisText.get(i);
                Vector2 position = graph.plot.pointsCountXAxisPosition.get(i);
                renderText(text, position.x, position.y, ALIGN_CENTER, ALIGN_NO);
            }
        }
    }

    // --------------------------------------------------
    // render text
    // --------------------------------------------------
    Vector<String> textBufferText = new Vector<>();
    Vector<Float> textBufferX = new Vector<>(); 
    Vector<Float> textBufferY = new Vector<>(); 

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

        renderRect(rectX, rectY - rectHeight, rectWidth, rectHeight, Color.LIGHT_GRAY);
        renderText2TextBuffer(text, x + 2, y - 3);
    }

    protected void initTextBuffer()
    {
        textBufferText.clear();
        textBufferX.clear();
        textBufferY.clear();
    }

    protected void renderText2TextBuffer(String text, float x, float y)
    {
        textBufferText.add(text);
        textBufferX.add(x);
        textBufferY.add(y);
    }

    protected void renderTextBuffer2Screen()
    {
        useBatch();
        for (int i=0; i < textBufferText.size(); i++)
        {
            String text= textBufferText.get(i);
            float x= textBufferX.get(i);
            float y= textBufferY.get(i);
            font.draw(batch, text, x, y);
        }
    }

    // --------------------------------------------------
    // render filled rectangle
    // --------------------------------------------------
    protected void renderRect(float x, float y, float width, float height, Color color)
    {
        if (USE_LINES_FOR_FILLED_RENDERING)
        {
            useRenderer(ShapeRenderer.ShapeType.Line, 1f);
            GdxUtil.renderFilledRectUsingLines(renderer, camera, x, y, width, height, color);
        }
        else
        {
            useRenderer(ShapeRenderer.ShapeType.Filled, 1f);
            renderer.setColor(color);
            renderer.rect(x, y, width, height);
        }
    }

    // --------------------------------------------------
    // render dot
    // --------------------------------------------------
    protected void renderDot(float x, float y, float size, Color color)
    {
        renderRect(x - size / 2, y - size / 2, size, size, color);
    }

    // --------------------------------------------------
    // render line
    // --------------------------------------------------
    protected void renderLine(Vector2 start, Vector2 end, float lineWidth, Color color)
    {
        useRenderer(ShapeRenderer.ShapeType.Line, lineWidth);
        renderer.setColor(color);
        renderer.line(start, end);
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
        int screenWidth = width;
        screenHeight = height;
        stage.getViewport().update(screenWidth, screenHeight, true);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        camera.viewportWidth = screenWidth;
        camera.viewportHeight = screenHeight;
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();

        axis.setScreenSize(screenWidth, screenHeight);
        axis.calculateAxis();
        graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
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

    protected class GraphUpdater extends Graph.PlotCalculatorListener
    {
    }

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
                                    graphUpdater);
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
                graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
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
