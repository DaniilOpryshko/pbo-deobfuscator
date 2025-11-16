package com.danielele.rapify;

import com.danielele.rapify.RapModel.RapValueType;
import com.danielele.rapify.RapModel.value.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class BinaryReader
{
    private ByteBuffer byteBuffer;

    public BinaryReader(byte[] content)
    {
        byteBuffer = ByteBuffer.wrap(content);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public boolean checkHeader()
    {
        if (byteBuffer.remaining() < 4) return false;

        byte b0 = byteBuffer.get();
        byte b1 = byteBuffer.get();
        byte b2 = byteBuffer.get();
        byte b3 = byteBuffer.get();

        return b0 == 0x00 && b1 == 'r' && b2 == 'a' && b3 == 'P';
    }

    public long readUInt32()
    {
        int signed = byteBuffer.getInt();
        return Integer.toUnsignedLong(signed);
    }

    public long readInt32()
    {
        return byteBuffer.getInt();
    }

    public String readAsciiZ()
    {
        StringBuilder sb = new StringBuilder();
        while (byteBuffer.hasRemaining())
        {
            byte b = byteBuffer.get();
            if (b == 0)
            {
                break;
            }
            sb.append((char) (b & 0xFF));
        }
        return sb.toString();
    }

    public byte readByte()
    {
        if (!byteBuffer.hasRemaining()) return 0;
        return byteBuffer.get();
    }

    public int readCompressedInteger()
    {
        if (!byteBuffer.hasRemaining()) return 0;

        int value = Byte.toUnsignedInt(byteBuffer.get());
        if (value == 0)
        {
            return 0;
        }

        while ((value & 0x80) != 0)
        {
            if (!byteBuffer.hasRemaining()) return 0;

            int extra = Byte.toUnsignedInt(byteBuffer.get());
            if (extra == 0)
            {
                return 0;
            }

            value += (extra - 1) * 0x80;
        }

        return value;
    }

    public IBinarizedRapEntry ReadBinarizedRapEntry(IBinarizedRapEntry entry, boolean arr, int ordinal)
    {
        return entry.fromBinary(this, arr, ordinal);
    }

    public boolean setPosition(long position)
    {
        if (position >= byteBuffer.capacity())
        {
            return false;
        }
        else
        {
            byteBuffer.position(Math.toIntExact(position));
            return true;
        }
    }

    public RapArrayValue readRapArray()
    {
        ArrayList<IRapEntry> iRapEntries = new ArrayList<>();
        RapArrayValue rapArrayValue = new RapArrayValue(iRapEntries);
        rapArrayValue.setEntryCount(readCompressedInteger());

        if (rapArrayValue.getEntryCount() == 0) return rapArrayValue;

        for (var i = 0; i < rapArrayValue.getEntryCount(); ++i)
        {
            switch (RapValueType.fromId(readByte()))
            {
                case RapValueType.STRING:
                    rapArrayValue.getValue().add(readRapString());
                    break;
                case RapValueType.FLOAT:
                    rapArrayValue.getValue().add(readRapFloat());
                    break;
                case RapValueType.LONG:
                    rapArrayValue.getValue().add(readRapUInt());
                    break;
                case RapValueType.ARRAY:
                    rapArrayValue.getValue().add(readRapArray());
                    break;
                case RapValueType.VARIABLE:
                    rapArrayValue.getValue().add(readRapVariable());
                    break;
                default:
                    throw new RuntimeException("How the fuck did you get here?");
            }
        }

        return rapArrayValue;
    }

    public RapStringValue readRapString()
    {
        return new RapStringValue(readAsciiZ());
    }

    public RapVariableValue readRapVariable()
    {
        return new RapVariableValue(readAsciiZ());
    }

    public RapIntegerValue readRapInt()
    {
        return new RapIntegerValue(readInt32());
    }

    public RapIntegerValue readRapUInt()
    {
        return new RapIntegerValue(readUInt32());
    }

    public RapFloatValue readRapFloat()
    {
        return new RapFloatValue(readFloat());
    }

    public float readFloat()
    {
        return byteBuffer.getFloat();
    }
}
