package com.danielele.rapify;

public interface IBinarizedRapEntry extends IRapEntry
{
    IBinarizedRapEntry fromBinary(BinaryReader reader, boolean arr, int ordinal);

    int getOrder();
}
