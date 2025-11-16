package com.danielele;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

public class MappedReader
{
    private final MappedByteBuffer buffer;
    private int position = 0;

    MappedReader(MappedByteBuffer buffer)
    {
        this.buffer = buffer;
    }

    int readByte() throws IOException
    {
        if (position >= buffer.limit()) throw new EOFException();
        return buffer.get(position++) & 0xFF;
    }

    String readStringZ() throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int total = 0;
        while (true)
        {
            int b = readByte();
            total++;
            if (b == 0) break;
            out.write(b);
            if (total > 100000000)
            {
                throw new IOException("Filename length too long, invalid PBO entry?");
            }
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    String readString(int length) throws IOException
    {
        byte[] buf = new byte[length];
        for (int i = 0; i < length; i++)
        {
            if (position >= buffer.limit()) throw new EOFException();
            buf[i] = buffer.get(position++);
        }
        return new String(buf, StandardCharsets.UTF_8);
    }

    long readU32() throws IOException
    {
        if (position + 4 > buffer.limit()) throw new EOFException();
        long val = ((long) (buffer.get(position++) & 0xFF)) |
                ((long) (buffer.get(position++) & 0xFF) << 8) |
                ((long) (buffer.get(position++) & 0xFF) << 16) |
                ((long) (buffer.get(position++) & 0xFF) << 24);
        return val;
    }

    PboEntry readPboEntry() throws IOException
    {
        PboEntry e = new PboEntry();
        e.filename = readStringZ();
        e.packagingMethod = readString(4);
        e.originalSize = readU32();
        e.reserved = readU32();
        e.timestamp = readU32();
        e.dataSize = readU32();
        return e;
    }

    int getPosition()
    {
        return position;
    }
}