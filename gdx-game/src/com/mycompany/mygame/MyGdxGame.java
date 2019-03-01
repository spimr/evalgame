package com.mycompany.mygame;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.glutils.*;

import java.util.*;
import com.badlogic.gdx.input.*;

public class MyGdxGame implements ApplicationListener
{
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	OrthographicCamera camera;

	BitmapFont font;

	Texture exitTexture;
	Texture startOnTexture;
	Texture startOffTexture;
	Texture decreaseNumSlicesTexture;
	Texture increaseNumSlicesTexture;

	int buttonYOffset;
	int buttonXOffset;
	HashMap<Texture, Rectangle> texture2Rectangle=new HashMap<Texture, Rectangle>();

	int textYOffset;

	boolean started=true;
	int numSlices=100;

	int screenWidth;
	int screenHeight;

	boolean isTouched;
	boolean justTouched;
	int isTouchedX;
	int isTouchedY;

	@Override
	public void create()
	{
		Gdx.app.setLogLevel(Application.LOG_INFO);
		font = new BitmapFont();

		exitTexture = new Texture(Gdx.files.internal("Exit.jpg"));

		startOnTexture = new Texture(Gdx.files.internal("StartOn.jpg"));
		startOffTexture = new Texture(Gdx.files.internal("StartOff.jpg"));

		decreaseNumSlicesTexture = new Texture(Gdx.files.internal("DecreaseNumSlices.jpg"));
		increaseNumSlicesTexture = new Texture(Gdx.files.internal("IncreaseNumSlices.jpg"));

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		int width=Gdx.graphics.getWidth();
		int height=Gdx.graphics.getHeight();
		camera = new OrthographicCamera(width, height);
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		camera.zoom=1f;
		camera.update();

		Gdx.input.setInputProcessor(new GestureDetector(new MyGestureListener ()));
	}

	float lastZoom=-1;
	float lastInitialDistance=-1;

	protected class MyGestureListener implements GestureDetector.GestureListener
	{

		@Override
		public boolean touchDown(float p1, float p2, int p3, int p4)
		{
			return false;
		}

		@Override
		public boolean tap(float p1, float p2, int p3, int p4)
		{
			return false;
		}

		@Override
		public boolean longPress(float p1, float p2)
		{
			return false;
		}

		@Override
		public boolean fling(float p1, float p2, int p3)
		{
			return false;
		}

		@Override
		public boolean pan(float x, float y, float deltaX, float deltaY)
		{
			camera.translate(-deltaX*camera.zoom, deltaY*camera.zoom);
			camera.update();
			return false;
		}

		@Override
		public boolean panStop(float p1, float p2, int p3, int p4)
		{
			return false;
		}

		@Override
		public boolean zoom(float initialDistance, float distance)
		{
			if (lastInitialDistance!=initialDistance)
			{
				lastInitialDistance=initialDistance;
				lastZoom=camera.zoom;
			}
			float zoom=camera.zoom;
			//zoom+=p1>p2 ? 0.01f : -0.01f;
			zoom=lastZoom/(distance/initialDistance);
			if (zoom<0.1f)
			{
				zoom=0.1f;
			}
			if (zoom>3)
			{
				zoom=3;
			}
			camera.zoom=zoom;
			camera.update();
			return false;
		}

		@Override
		public boolean pinch(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4)
		{
			return false;
		}
        
        public void pinchStop()
        {
        }
	}
	@Override
	public void render()
	{
		// global variables
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		// just touched logic
		boolean wasTouched=isTouched;
		isTouched = Gdx.input.isTouched();
		if (isTouched)
		{
			isTouchedX = Gdx.input.getX();
			isTouchedY = Gdx.input.getY();
		}
		else
		{
			isTouchedX = -1;
			isTouchedY = -1;
		}
		justTouched = isTouched && !wasTouched;

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// begin
		Gdx.gl.glLineWidth(1);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		batch.begin();
		buttonXOffset = 0;
		buttonYOffset = 0;
		textYOffset = 0;

		// start and stop buttons
		int gap = 10;
		int buttonSize = 80;

		renderButton(true, exitTexture, exitTexture, buttonSize, gap, false);
		renderButton(started, startOnTexture, startOffTexture, buttonSize, gap, false);
		renderButton(true, decreaseNumSlicesTexture, decreaseNumSlicesTexture, buttonSize, gap, true);
		renderButton(true, increaseNumSlicesTexture, increaseNumSlicesTexture, buttonSize, gap, true);

		if (isButtonTouched(exitTexture, true))
		{
			Gdx.app.exit();
		}
		if (isButtonTouched(startOnTexture, true))
		{
			started = !started;
		}
		if (isButtonTouched(decreaseNumSlicesTexture, false))
		{
			numSlices--;
		}
		if (isButtonTouched(increaseNumSlicesTexture, false))
		{
			numSlices++;
		}
		if (numSlices < 0)
		{
			numSlices = 0;
		}

		// debug output
		renderText("screenWidth=" + screenWidth);
		renderText("screenHeight=" + screenHeight);
		renderText("isTouched=" + isTouched + " " + isTouchedX + "," + isTouchedY);
		renderText("justTouched=" + justTouched);
		renderText("started=" + started);
		renderText("numSlices=" + numSlices);
		renderText("zoom=" + camera.zoom);
		renderText("fps=" + Math.round(1 / Gdx.graphics.getDeltaTime()));

		// rendering
		int renderingWidth=screenWidth;
		int renderingHeight=screenHeight - Math.max(buttonYOffset, textYOffset);

		if (started)
		{
			Vector2 start=new Vector2();
			Vector2 end=new Vector2();

			double step=Math.PI / numSlices;
			double radius=Math.min(renderingWidth, renderingHeight) / 2;
			start.set(renderingWidth / 2, renderingHeight / 2);
			for (double i=0; i < 2 * Math.PI; i += step)
			{
				end.set(start.x + (float)(Math.cos(i) * radius),
						start.y + (float)(Math.sin(i) * radius));
				renderLine(start, end, 5, Color.BLACK);
			}
		}

		// end
		batch.end();
		shapeRenderer.end();
	}

	protected void renderButton(boolean on, Texture onTexture, Texture offTexture, int buttonSize, int gap, boolean newColumn)
	{
		if (newColumn)
		{
			buttonXOffset += buttonSize + gap;
			buttonYOffset = 0;
		}
		Rectangle r=new Rectangle(screenWidth - buttonXOffset - buttonSize, //
				screenHeight - buttonYOffset - buttonSize, //
				buttonSize, buttonSize);
		batch.draw(on ? onTexture: offTexture, r.x, r.y, r.width, r.height);
		texture2Rectangle.put(onTexture, r);
		texture2Rectangle.put(offTexture, r);
		buttonYOffset += buttonSize + gap;
	}

	protected boolean isButtonTouched(Texture texture, boolean just)
	{
		boolean result=false;
		if ((isTouched && !just) || (justTouched && just))
		{
			int x = isTouchedX;
			int y = screenHeight - isTouchedY;
			Rectangle r=texture2Rectangle.get(texture);
			if (r != null && r.contains(x, y))
			{
				result = true;
			}
		}
		return result;
	}

	protected void renderText(String text)
	{
		font.draw(batch, text, 0, screenHeight - textYOffset);
		textYOffset += font.getLineHeight();
	}

	protected void renderLine(Vector2 start, Vector2 end, int lineWidth, Color color)
	{
		shapeRenderer.setColor(color);
		shapeRenderer.line(start, end);
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public void resize(int width, int height)
	{
		create();
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}
