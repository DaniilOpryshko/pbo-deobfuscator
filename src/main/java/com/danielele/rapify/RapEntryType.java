package com.danielele.rapify;

public enum RapEntryType
{
    RapClass,

    RapValue,

    RapArray,
    RapExternClass,
    RapDeleteClass,
    RapArrayFlag;

    public static RapEntryType fromInt(int value)
    {
        return values()[value];
    }

    @Override
    public String toString()
    {
        return super.toString();
    }
}
