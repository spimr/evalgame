package com.mycompany.mygame;
import java.util.*;
import com.badlogic.gdx.math.*;

public class Plot
{
    // calculated bu GraphUtil
    ArrayList<String> log =new ArrayList<>();
    ArrayList<Vector2> graphPoints = new ArrayList<>();

    // infered to speed up rendering
    ArrayList<Vector2> screenPoints = new ArrayList<>();
    ArrayList<Vector2> blueDotScreenPosition = new ArrayList<>();
    ArrayList<String> pointsCountXAxisText = new ArrayList<>();
    ArrayList<Float> pointsCountXAxisGraphPosition = new ArrayList<>();    
    
    Plot()
    {
    }
    
}
