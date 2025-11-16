package com.danielele.rapify.RapModel;


import com.danielele.rapify.BinaryReader;
import com.danielele.rapify.IBinarizedRapEntry;
import com.danielele.rapify.IRapEntry;

public class RapValue extends OrderableEntity
{
    private RapValueType subType;
    private String name;
    private IRapEntry value;

    private boolean flaggedArray = false;

    public void setFlaggedArray(boolean flaggedArray)
    {
        this.flaggedArray = flaggedArray;
    }

    public RapValue(int ordinal)
    {
        super(ordinal);
    }

    public RapValueType getSubType()
    {
        return subType;
    }

    public void setSubType(RapValueType subType)
    {
        this.subType = subType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public IRapEntry getValue()
    {
        return value;
    }

    public void setValue(IRapEntry value)
    {
        this.value = value;
    }

    @Override
    public IBinarizedRapEntry fromBinary(BinaryReader reader, boolean parent, int ordinal)
    {
        if (subType == RapValueType.ARRAY)
        {
            if (flaggedArray)
            {
                long l = reader.readUInt32();
            }

            this.subType = RapValueType.ARRAY;
            this.name = reader.readAsciiZ();
            this.value  = reader.readRapArray();
            return this;
        }

        this.subType = RapValueType.fromId(reader.readByte());
        this.name = reader.readAsciiZ();

        IRapEntry entry = getEntry(reader, this.subType);

        this.value = entry;
        return this;
    }

    private IRapEntry getEntry(BinaryReader reader, RapValueType type)
    {
        return switch (type)
        {
            case STRING -> reader.readRapString();
            case VARIABLE -> reader.readRapVariable();
            case FLOAT -> reader.readRapFloat();
            case LONG -> reader.readRapInt();
            case ARRAY -> reader.readRapArray();
        };
    }

    @Override
    public String toConfigFormat(int depth)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\t".repeat(depth));
        builder.append(name);
        if (value == null) {
            throw new NullPointerException("Value is null");
        }
        IRapEntry value = getValue();
        if (subType == RapValueType.ARRAY) {
            builder.append("[]");
        }

        if (flaggedArray)
        {
            builder.append(" += ");
        }
        else
        {
            builder.append(" = ");
        }

        return builder.append(value.toConfigFormat(depth + 1)).append(';').append("\n").toString();
    }
}
