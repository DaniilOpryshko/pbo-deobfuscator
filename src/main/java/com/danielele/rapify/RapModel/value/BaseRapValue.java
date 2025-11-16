package com.danielele.rapify.RapModel.value;


import com.danielele.rapify.IRapEntry;

public abstract class BaseRapValue<T> implements IRapEntry
{
    protected T value;

    public BaseRapValue(T value)
    {
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }

    public void setValue(T value)
    {
        this.value = value;
    }
}
