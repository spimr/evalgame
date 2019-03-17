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
import java.util.*;
import java.util.Vector;
import java.text.*;

public class GdxUtil
{
    public static final int HALIGN_NO = 1;
    public static final int HALIGN_LEFT = 2;
    public static final int HALIGN_CENTER = 4;
    public static final int HALIGN_RIGHT = 8;    
    public static final int VALIGN_NO = 16;
    public static final int VALIGN_TOP = 32;
    public static final int VALIGN_CENTER = 64;
    public static final int VALIGN_BOTTOM = 128;

    public static final float TABLE_SPACING = 5;
    public static final float SCREEN_MARGIN = 40;

    private static final String SKIN_FILE_NAME = "clean-crispy-skin/skin/clean-crispy-ui.json";
    // "my-skin/uiskin.json";
    // "default-skin/skin/uiskin.json";

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final String TIME_FORMAT_PATTERN = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final boolean USE_LINES_FOR_FILLED_RENDERING = false;

    private static ArrayList<String> logTexts = new ArrayList<>();
    private static DateFormat dateFormat;
    private static DateFormat timeFormat;
    private static DateFormat dateTimeFormat;

    private static Skin skin;
    private static Stage stage;

    private static SpriteBatch batch;
    private static ShapeRenderer renderer;

    private static OrthographicCamera camera;
    private static BitmapFont font;
    private static GlyphLayout layout;

    private static int screenWidth;
    private static int screenHeight;

    // --------------------------------------------------
    // init
    // --------------------------------------------------
    public static void init()
    {
        // logging
        Gdx.app.setLogLevel(Application.LOG_INFO);
        logTexts = new ArrayList<>();
        dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
        timeFormat = new SimpleDateFormat(TIME_FORMAT_PATTERN);
        dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT_PATTERN);

        // skin
        skin = new Skin(Gdx.files.internal(SKIN_FILE_NAME));
        TextField.TextFieldStyle textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font.getData().scale(0.5f);

        // batch and renderer
        batch = new SpriteBatch();
        renderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        font = skin.get("graph-font", BitmapFont.class);
        layout = new GlyphLayout();

        // stage
        stage = new Stage(new ScreenViewport());
    }

    public static Skin getSkin()
    {
        return skin;
    }

    public static void runInGdxLater(Runnable runnable)
    {
        Gdx.app.postRunnable(runnable);
    }

    // --------------------------------------------------
    // batch and renderer
    // --------------------------------------------------
    private static boolean batchStarted = false;
    private static int batchStartCount =0;

    private static boolean rendererStarted = false;
    private static int rendererStartCount=0;
    private static ShapeRenderer.ShapeType shapeType;
    private static float lineWidth;

    public static void useBatch()
    {
        if (!batchStarted)
        {
            endBatchAndRendererInternal();
        }
        batch.begin();
        batchStarted = true;
        batchStartCount++;
    }

    public static void useRenderer(ShapeRenderer.ShapeType shapeType, float lineWidth)
    {
        if (shapeType == null)
        {
            shapeType = GdxUtil.shapeType;
        }
        if (GdxUtil.shapeType != shapeType || GdxUtil.lineWidth != lineWidth || !rendererStarted)
        {
            endBatchAndRendererInternal();
            Gdx.gl.glLineWidth(lineWidth);
            renderer.setProjectionMatrix(camera.combined);
            renderer.begin(shapeType);
            rendererStarted = true;
            rendererStartCount++;
            GdxUtil.shapeType = shapeType;
            GdxUtil.lineWidth = lineWidth;
        }
    }

    public static void initBatchAndRenderer()
    {
        batchStartCount = 0;
        rendererStartCount = 0;
        shapeType = ShapeRenderer.ShapeType.Line;
        lineWidth = 1f;
    }

    public static void endBatchAndRenderer()
    {
        // render all text before the log texts
        // to make sure log texts will be on the top (including white background)
        renderTextBuffer2Screen(); 

        // render log texts
        // first adding one which is needed to render log 
        rendererStartCount++;
        batchStartCount++;

        // log gdx util internal variablea
        log();

        // render all log texts 
        renderLogTexts();
        renderTextBuffer2Screen(); 

        // end batch and renderer
        endBatchAndRendererInternal();
    }

    public static void endBatchAndRendererInternal()
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

    // --------------------------------------------------
    // camera
    // --------------------------------------------------
    private static Vector3 projectVector = new Vector3();

    public static float getScreenWidth()
    {
        return screenWidth;
    }

    public static float getScreenHeight()
    {
        return screenHeight;
    }

    public static void setScreenSize(int screenWidth, int screenHeight)
    {
        GdxUtil.screenWidth = screenWidth;
        GdxUtil.screenHeight = screenHeight;

        stage.getViewport().update(screenWidth, screenHeight, true);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, screenWidth, screenHeight);

        camera.viewportWidth = screenWidth;
        camera.viewportHeight = screenHeight;
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.update();
    }

    public static float getCameraZoom()
    {
        return camera.zoom;
    }

    public static void setCameraZoom(float zoom)
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

    public static void setCameraTranslation(float deltaX, float deltaY)
    {
        camera.translate(deltaX, deltaY);
        camera.update();
    }

    public static void project(Vector2 result)
    {
        projectVector.set(result, 0);
        camera.project(projectVector);
        result.set(projectVector.x, projectVector.y);
    }

    public static OrthographicCamera getCamera()
    {
        return camera;
    }

    // --------------------------------------------------
    // logging
    // --------------------------------------------------
    private static float renderText_TextYOffset;

    public static void log(String logText)
    {
        logTexts.add(logText);    
    }

    public static void log(Vector<String> logTextsToAdd)
    {
        for (String logText:logTextsToAdd)
        {
            logTexts.add(logText);    
        }
    }

    public static String getDate()
    {
        String result;
        Date today = Calendar.getInstance().getTime();        
        result = dateFormat.format(today);
        return result;
    }

    public static String getTime()
    {
        String result;
        Date today = Calendar.getInstance().getTime();        
        result = timeFormat.format(today);
        return result;
    }

    public static String getDateTime()
    {
        String result;
        Date today = Calendar.getInstance().getTime();        
        result = dateTimeFormat.format(today);
        return result;
    }

    public static void log()
    {
        log("GdxUtil");
        log("time=" + getTime());
        log("screenWidth=" + screenWidth);
        log("screenHeight=" + screenHeight);
        log("zoom=" + camera.zoom);   
        log("fps=" + Math.round(1 / Gdx.graphics.getDeltaTime()));
        log("rendererStartCount=" + rendererStartCount);
        log("batchStartCount=" + batchStartCount);
        log("");
    }

    public static void renderLogTexts()
    {
        renderText_TextYOffset = SCREEN_MARGIN;
        for (String logText : logTexts)
        {
            renderLogText(logText);
        }
        logTexts.clear();
    }

    public static void renderLogText(String text)
    {
        renderText(text, SCREEN_MARGIN, screenHeight - renderText_TextYOffset, HALIGN_NO | VALIGN_NO);
        renderText_TextYOffset += renderText_RenderedTextSize.y + 5;
    }    

    // --------------------------------------------------
    // render text
    // --------------------------------------------------
    private static Vector<String> textBufferText = new Vector<>();
    private static Vector2List textBufferPosition = new Vector2List(); 

    private static Vector2 renderText_RenderedTextSize = new Vector2();
    private static Vector2 renderText_RenderedTextBoxSize = new Vector2();
    private static Vector3 renderText_ProjectVector = new Vector3();

    public static Vector2 renderText(String text, float x, float y, int align)
    {
        getRenderedTextSize(text);
        layout.setText(font, text);
        renderText_RenderedTextSize.set(layout.width, layout.height);
        renderText_RenderedTextBoxSize.set(renderText_RenderedTextSize.x + 4, renderText_RenderedTextSize.y + 4);
        if ((align & HALIGN_LEFT) == HALIGN_LEFT)
        {
            x = x - renderText_RenderedTextBoxSize.x;
        }
        else if ((align & HALIGN_CENTER) == HALIGN_CENTER)
        {
            x = x - renderText_RenderedTextBoxSize.x / 2;
        }
        else if ((align & HALIGN_RIGHT) == HALIGN_RIGHT)
        {
            x = x + renderText_RenderedTextBoxSize.x;
        }
        if ((align & VALIGN_TOP) == VALIGN_TOP)
        {
            y = y + renderText_RenderedTextBoxSize.y;
        }
        else if ((align & VALIGN_CENTER) == VALIGN_CENTER)
        {
            y = y + renderText_RenderedTextBoxSize.y / 2;
        }
        else if ((align & VALIGN_BOTTOM) == VALIGN_BOTTOM)
        {
            y = y - renderText_RenderedTextBoxSize.y;
        }
        renderText_ProjectVector.set(x, screenHeight - y, 0);
        camera.unproject(renderText_ProjectVector);
        float rectX = renderText_ProjectVector.x;
        float rectY = renderText_ProjectVector.y;
        float rectWidth = camera.zoom * renderText_RenderedTextBoxSize.x;
        float rectHeight = camera.zoom * renderText_RenderedTextBoxSize.y;

        renderRect(rectX, rectY - rectHeight, rectWidth, rectHeight, Color.WHITE);
        renderText2TextBuffer(text, x + 2, y - 3);
        return renderText_RenderedTextSize;
    }

    public static Vector2 getRenderedTextSize(String text)
    {
        layout.setText(font, text);
        renderText_RenderedTextSize.set(layout.width, layout.height);
        renderText_RenderedTextBoxSize.set(renderText_RenderedTextSize.x + 4, renderText_RenderedTextSize.y + 4);
        return renderText_RenderedTextSize;
    }

    public static void renderText2TextBuffer(String text, float x, float y)
    {
        textBufferText.add(text);
        textBufferPosition.add(textBufferPosition.newElement().set(x, y));
    }

    public static void renderTextBuffer2Screen()
    {
        useBatch();
        for (int i=0; i < textBufferText.size(); i++)
        {
            String text= textBufferText.get(i);
            Vector2 position=textBufferPosition.get(i); 
            font.draw(batch, text, position.x, position.y);
        }
        textBufferText.clear();
        textBufferPosition.clear();
    }

    // --------------------------------------------------
    // render filled rectangle
    // --------------------------------------------------
    public static void renderRect(float x, float y, float width, float height, Color color)
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
    public static void renderDot(float x, float y, float size, Color color)
    {
        size = size * GdxUtil.getCameraZoom();
        renderRect(x - size / 2, y - size / 2, size, size, color);
    }

    // --------------------------------------------------
    // render line
    // --------------------------------------------------
    public static void renderLine(Vector2 start, Vector2 end, float lineWidth, Color color)
    {
        useRenderer(ShapeRenderer.ShapeType.Line, lineWidth);
        renderer.setColor(color);
        renderer.line(start, end);
    }

    public static void renderFilledRectUsingLines(ShapeRenderer shapeRenderer, OrthographicCamera camera, float x, float y, float width, float height, Color color)
    {
        shapeRenderer.setColor(color);
        for (float i=0; i <= height; i = i + camera.zoom)
        {
            shapeRenderer.line(x, y + i, x + width, y + i);
        }
    }    

    // --------------------------------------------------
    // style support
    // --------------------------------------------------
    public static Drawable getColoredDrawable(Color color)
    {
        Drawable result;
        Pixmap labelColor = new Pixmap(1, 1, Pixmap.Format.RGB888);
        labelColor.setColor(Color.WHITE);
        labelColor.fill();
        result = new Image(new Texture(labelColor)).getDrawable();
        return result;
    }

    // --------------------------------------------------
    // stage
    // --------------------------------------------------
    public static Stage getStage()
    {
        return stage;
    }

    public static void addActor(Actor actor)
    {
        stage.addActor(actor);
    }

    public static void setInputProcessor(GestureDetector.GestureAdapter gestureListener)
    {
        GestureDetector gestureDetector = new GestureDetector(gestureListener);
        InputMultiplexer inputMultiplexer = new InputMultiplexer(GdxUtil.getStage(), gestureDetector);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public static void renderStage()
    {
        GdxUtil.getStage().act(Gdx.graphics.getDeltaTime());
        GdxUtil.getStage().draw();
    }

    public static ImageTextButton getButton(String text, String fileName, Skin skin)
    {
        ImageTextButton result;
        result = new ImageTextButton(text, skin);
        TextureRegionDrawable exitButtonImage = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(fileName))));
        ImageTextButton.ImageTextButtonStyle style = result.getStyle();
        style = new ImageTextButton.ImageTextButtonStyle(style);
        result.setStyle(style);
        style.imageUp = exitButtonImage;
        style.imageDown = exitButtonImage;
        return result;
    }

    public static Table getButtonTable(Button[] buttons, boolean vertical)
    {
        Table result;
        result = new Table();
        for (Button button: buttons)
        {
            result.add(button).expand().fill().uniform().space(TABLE_SPACING);
            if (vertical)
            {
                result.row();
            }
        }
        return result;
    }
}
