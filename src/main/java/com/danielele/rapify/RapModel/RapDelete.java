package com.danielele.rapify.RapModel;

import com.danielele.rapify.BinaryReader;
import com.danielele.rapify.IBinarizedRapEntry;

public class RapDelete extends OrderableEntity
{
    private String name;

    public RapDelete(int ordinal)
    {
        super(ordinal);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public IBinarizedRapEntry fromBinary(BinaryReader reader, boolean arr, int ordinal)
    {
        this.name  = reader.readAsciiZ();
        return this;
    }

    @Override
    public String toConfigFormat(int depth)
    {
        return "";
    }
}
