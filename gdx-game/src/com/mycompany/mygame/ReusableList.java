package com.mycompany.mygame;

import java.util.ArrayList;
import java.util.*;
import com.badlogic.gdx.math.*;
import java.lang.reflect.*;
import com.badlogic.gdx.*;

public abstract class ReusableList<T> implements Iterable
{
    ArrayList<T> list=new ArrayList<>();
    ArrayDeque<T> toReuse=new ArrayDeque<>();
    Class elementClass;

    public ReusableList()
    {
        Exception causeEx=null;
        try
        {
            elementClass = ((Class)((ParameterizedType)this.getClass().
                getGenericSuperclass()).getActualTypeArguments()[0]);
        }
        catch (Exception ex)
        {
            causeEx=ex;
        }
        if (elementClass==null)
        {
            throw new IllegalArgumentException("ReusableList must be instantiated as non-generic subclass!", causeEx);
        }
    }

    public int size()
    {
        return list.size();
    }

    public Iterator<T> iterator()
    {
        return list.iterator();
    }

    public T get(int index)
    {
        return list.get(index);
    }

    public void clear()
    {
        toReuse.addAll(list);
        list.clear();
    }

    public boolean add(T element)
    {
        boolean result = list.add(element);
        return result;
    }

    public T newElement()
    {
        T result = null;
        if (!toReuse.isEmpty())
        {
            result = toReuse.removeLast();
        }
        if (result == null)
        {
            try
            {
                result = (T) elementClass.newInstance();
            }
            catch (Exception ex)
            {
                throw new IllegalArgumentException("Could not create new elemnt of " + this.getClass(), ex);
            }
        }
        return result;
    }

}
