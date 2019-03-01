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

public class GdxUtil
{
    public static final float TABLE_SPACING = 5;
    public static final float TABLE_PADDING = 10;

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

    public static Drawable getFilledRectangle(float width, float height, Color backgroundColor)
    {
        Drawable result;
        Pixmap labelColor = new Pixmap((int)width, (int)height, Pixmap.Format.RGB888);
        labelColor.setColor(backgroundColor);
        labelColor.fill();
        result = new Image(new Texture(labelColor)).getDrawable();
        return result;
    }

    public static void renderFilledRectangle(ShapeRenderer shapeRenderer, OrthographicCamera camera, float x, float y, Color color, int boxSize)
    {
        shapeRenderer.setColor(color);
        int boxHalfSize = boxSize / 2;
        for (int i=0; i <= boxSize; i++)
        {
            float pI=i * camera.zoom;
            float pBoxHalfSize = boxHalfSize * camera.zoom;
            shapeRenderer.line(x - pBoxHalfSize, y + pI, x + pBoxHalfSize, y + pI);
        }
    }    

}
