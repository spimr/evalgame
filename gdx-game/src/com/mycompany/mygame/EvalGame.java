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
    TextField expressionField;
    Label expressionStatusLabel;
    ImageTextButton exitButton;
    ImageTextButton resetButton;
    ImageTextButton plusButton;
    ImageTextButton minusButton;

    boolean expressionValid;

    AbstractGestureListener gestureListener;
    
    Axis axis;
    Graph graph;
    GraphUpdater graphUpdater=new GraphUpdater();

    // --------------------------------------------------
    // gdx application
    // --------------------------------------------------
    @Override
    public void create()
    { 
        // gdx init
        GdxUtil.init();

        // actors
        Label expressionLabel = new Label("f(x)=", GdxUtil.getSkin());

        expressionField = new TextField("", GdxUtil.getSkin());
        expressionField.addListener(new ChangeListener(){
                public void changed(com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent p1, com.badlogic.gdx.scenes.scene2d.Actor p2)
                {
                    expressionFieldAction(expressionField.getText());
                }
            });

        expressionStatusLabel = new Label("", GdxUtil.getSkin());
        expressionStatusLabel.setWrap(true);
        Label.LabelStyle expressionStatusLabelStyle = new Label.LabelStyle(expressionStatusLabel.getStyle());
        expressionStatusLabelStyle.background = GdxUtil.getColoredDrawable(Color.WHITE);
        expressionStatusLabel.setStyle(expressionStatusLabelStyle);

        exitButton = GdxUtil.getButton("Exit", "ExitButton-32x32.png", GdxUtil.getSkin());
        exitButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    exitButtonAction();
                    return true;
                }
            });

        resetButton = GdxUtil.getButton("Reset", "ResetButton-32x32.png", GdxUtil.getSkin());
        resetButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    resetButtonAction();
                    return true;
                }
            });

        plusButton = GdxUtil.getButton("Plus", "PlusButton-32x32.png", GdxUtil.getSkin());
        plusButton.addListener(new ClickListener(){
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button)
                {
                    plusButtonAction();
                    return true;
                }
            });

        minusButton = GdxUtil.getButton("Minus", "MinusButton-32x32.png", GdxUtil.getSkin());
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
        expressionTable.add(new Label("", GdxUtil.getSkin()));
        expressionTable.add(expressionStatusLabel).expandX().fillX();

        Table buttonTable = GdxUtil.getButtonTable(new Button[] {exitButton, resetButton, plusButton, minusButton}, true);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.add(new Label("", GdxUtil.getSkin())).expandX().fillX().space(GdxUtil.TABLE_SPACING);
        mainTable.add(expressionTable).expandX().fillX().top().padTop(3).space(GdxUtil.TABLE_SPACING);
        mainTable.add(buttonTable).top().space(GdxUtil.TABLE_SPACING);
        mainTable.right().top().pad(GdxUtil.SCREEN_MARGIN);

        GdxUtil.addActor(mainTable);

        // axis
        axis = new Axis();

        // graph
        graph = new Graph(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        graph.setGdxObjects(GdxUtil.getCamera());

        expressionField.setText(Graph.TEST_EXPRESSION);
        expressionFieldAction(Graph.TEST_EXPRESSION);

        // input processor
        //SimpleCameraGestureListener gestureListener = new SimpleCameraGestureListener(axis);
        //gestureListener = new ExploreGraphGestureListener(axis, graph, graphUpdater);
        gestureListener = new BetterCameraGestureListener(axis, graph, graphUpdater);
        GdxUtil.setInputProcessor(gestureListener);
    }

    @Override
    public void resize(int width, int height)
    {
        // resize is called 
        // - directly after create
        // - when screen is resized (orientation change, bottom control panel shown)
        // in both cases we have to inform GdxUtil axis and graph
        int screenWidth = width;
        int screenHeight = height;

        GdxUtil.setScreenSize(screenWidth, screenHeight);
        axis.calculateAxis();
        graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resume()
    {
    }

    @Override
    public void dispose()
    {
    }

    // --------------------------------------------------
    // actors actions
    // --------------------------------------------------
    public void expressionFieldAction(String newExpression)
    {
        String newExpressionTrimmed = newExpression == null ? "" : newExpression.trim();
        expressionValid = true;
        if (newExpressionTrimmed.length() == 0)
        {
            graph.expression = newExpressionTrimmed;
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
                expressionValid = false;
            }
        }
        setExpressionStatus(graph.expressionStatus);
        if (expressionValid)
        {
            graph.calculateDomain();
            axis.setWorldSize(graph.getXMin(), graph.getXMax(), graph.getXGrid(), graph.getYMin(), graph.getYMax(), graph.getYGrid());
            axis.calculateAxis();
            graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
        }
    }

    public void setExpressionStatus(String expressionStatus)
    {
        expressionStatusLabel.setText(expressionStatus);
        expressionStatusLabel.setWidth(expressionField.getWidth());
        expressionStatusLabel.setHeight(expressionStatusLabel.getPrefHeight());
        expressionStatusLabel.getStyle().font.setColor(expressionValid ? Color.BLACK : Color.RED);
        expressionStatusLabel.invalidate();
        expressionStatusLabel.setVisible(expressionStatus != null && expressionStatus.length() != 0);
    }

    public void exitButtonAction()
    {
        Gdx.app.exit();
    }

    public void resetButtonAction()
    {
        graph.calculateDomain();
        axis.setWorldSize(graph.getXMin(), graph.getXMax(), graph.getXGrid(), graph.getYMin(), graph.getYMax(), graph.getYGrid());
        GdxUtil.setCameraZoom(1f);
        axis.calculateAxis();

        graph.calculatePlot(axis.xMin, axis.xMax, axis.xGrid, axis.yMin, axis.yMax, axis.yGrid, graphUpdater);
    }

    protected void minusButtonAction()
    {
        GdxUtil.setCameraZoom(GdxUtil.getCameraZoom() + 0.1f);
        axis.calculateAxis();
    }

    protected void plusButtonAction()
    {
        GdxUtil.setCameraZoom(GdxUtil.getCameraZoom() - 0.1f);
        axis.calculateAxis();
    }

    // --------------------------------------------------
    // render method
    // --------------------------------------------------
    @Override
    public void render()
    {
        // init
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // begin
        GdxUtil.initBatchAndRenderer();
        
        // axis
        axis.log();
        renderXAxisLines();
        renderYAxisLines();
        renderXAxisMarks();
        renderYAxisMarks();

        // graph
        renderGraph();

        // texts
        renderXAxisLabels();
        renderYAxisLabels();

        // gesture listener log
        gestureListener.log();
        
        // end
        GdxUtil.endBatchAndRenderer();

        // stage
        GdxUtil.renderStage();
    }

    // --------------------------------------------------
    // render axis
    // --------------------------------------------------
    protected void renderXAxisLines()
    {
        for (int i=0; i < axis.yAxisLineStart.size(); i++)
        {
            GdxUtil.renderLine(axis.yAxisLineStart.get(i), axis.yAxisLineEnd.get(i), //
                               1, //
                               axis.yAxisMainIndex == i ? Color.BLACK : Color.LIGHT_GRAY);
        }
    }

    protected void renderXAxisMarks()
    {
        for (int i=0; i < axis.yAxisMarkStart.size(); i++)
        {
            GdxUtil.renderLine(axis.yAxisMarkStart.get(i), axis.yAxisMarkEnd.get(i), //
                               3, //
                               Color.BLACK);
        }
    }

    protected void renderXAxisLabels()
    {
        for (int i=0; i < axis.xAxisText.size(); i++)
        {
            GdxUtil.renderText(axis.xAxisText.get(i), 
                               axis.xAxisTextPosition.get(i).x,
                               axis.xAxisTextPosition.get(i).y,
                               axis.xAxisTextAlign);
        }
    }

    protected void renderYAxisLines()
    {
        for (int i=0; i < axis.xAxisLineStart.size(); i++)
        {
            GdxUtil.renderLine(axis.xAxisLineStart.get(i), axis.xAxisLineEnd.get(i), //
                               1, //
                               axis.xAxisMainIndex == i ? Color.BLACK : Color.LIGHT_GRAY);
        }
    }

    protected void renderYAxisMarks()
    {
        for (int i=0; i < axis.xAxisMarkStart.size(); i++)
        {
            GdxUtil.renderLine(axis.xAxisMarkStart.get(i), axis.xAxisMarkEnd.get(i), //
                               3, //
                               Color.BLACK);
        }
    }

    protected void renderYAxisLabels()
    {
        for (int i=0; i < axis.yAxisText.size(); i++)
        {
            GdxUtil.renderText(axis.yAxisText.get(i), 
                               axis.yAxisTextPosition.get(i).x,
                               axis.yAxisTextPosition.get(i).y,
                               axis.yAxisTextAlign);
        }
    }

    // --------------------------------------------------
    // render graph
    // --------------------------------------------------
    public void renderGraph()
    {
        if (graph.plot != null)
        {
            boolean plotObsolete = graph.isPlotObsolete();
            Color plotLineColor = plotObsolete ? Color.LIGHT_GRAY : Color.BLACK;
            Color blueDotColor = plotObsolete ? Color.LIGHT_GRAY : Color.BLUE;
            Color redDotColor = plotObsolete ? Color.LIGHT_GRAY : Color.RED;

            Vector2 start =null;
            Vector2 end=null;

            for (String logText : graph.plot.log)
            {
                GdxUtil.log(logText);
            }

            for (int i=0; i < graph.plot.graphPoints.size(); i++)
            {
                end = graph.plot.screenPoints.get(i);
                if (start != null && end != null)
                {
                    GdxUtil.renderLine(start, end, 1, plotLineColor);
                }
                start = end;
            }

            for (int i=0; i < graph.plot.graphPoints.size(); i++)
            {
                end = graph.plot.screenPoints.get(i);
                Vector2 blueDot = graph.plot.blueDotScreenPosition.get(i);

                if (end != null && Graph.PLOT_RED_DOTS && !plotObsolete)
                {
                    GdxUtil.renderDot(end.x, end.y, 4, redDotColor);
                }
                if (blueDot != null && Graph.PLOT_BLUE_DOTS && !plotObsolete)
                {   
                    GdxUtil.renderDot(blueDot.x, blueDot.y, 4, blueDotColor);
                }
            }

            if (!plotObsolete)
            {
                for (int i=0; i < graph.plot.pointsCountXAxisText.size(); i++)
                {
                    String text = graph.plot.pointsCountXAxisText.get(i);
                    Vector2 position = graph.plot.pointsCountXAxisPosition.get(i);
                    GdxUtil.renderText(text, position.x, position.y, GdxUtil.HALIGN_CENTER | GdxUtil.VALIGN_NO);
                }
            }
        }
    }

    protected class GraphUpdater extends Graph.PlotCalculatorListener
    {
        public void started()
        {
            if (expressionValid)
            {
                setExpressionStatus("Calculating Plot...");
            }
        }

        public void progress(String progressText)
        {
        }

        public void finished()
        {
            if (expressionValid)
            {
                setExpressionStatus("");
            }
        }

        public void aborted()
        {
        }
    }

}
