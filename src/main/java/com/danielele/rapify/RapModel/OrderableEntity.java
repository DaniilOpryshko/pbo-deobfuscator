package com.danielele.rapify.RapModel;

import com.danielele.rapify.IBinarizedRapEntry;

public abstract class OrderableEntity implements IBinarizedRapEntry
{
    private final int ordinal;

    public OrderableEntity(int ordinal)
    {
        this.ordinal = ordinal;
    }

    @Override
    public int getOrder()
    {
        return ordinal;
    }
}
