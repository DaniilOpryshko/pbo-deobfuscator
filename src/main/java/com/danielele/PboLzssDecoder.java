package com.danielele;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class PboLzssDecoder
{

    private PboLzssDecoder()
    {
    }

    public static byte[] decodeBlock(byte[] data, int offset, int length,
                                     int outputSize, boolean verifyChecksum)
    {
        if (outputSize < 0) throw new IllegalArgumentException("outputSize < 0");
        if (offset < 0 || length < 4 || offset + length > data.length)
            throw new IllegalArgumentException("Invalid data range or too short (need >= 4 for checksum)");

        final int checksumPos = offset + length - 4;
        final long expectedChecksum = readU32LE(data, checksumPos);

        byte[] out = new byte[outputSize];
        int outPos = 0;

        int pos = offset;
        int end = checksumPos;

        while (outPos < outputSize)
        {
            if (pos >= end)
            {
                return out;
            }

            int flags = data[pos++] & 0xFF;

            for (int bit = 0; bit < 8 && outPos < outputSize; bit++)
            {
                boolean isRaw = ((flags >>> bit) & 1) == 1;

                if (isRaw)
                {
                    if (pos >= end)
                    {
                        throw new IllegalStateException("Expected raw byte but reached end of compressed data");
                    }
                    out[outPos++] = data[pos++];
                }
                else
                {
                    if (pos + 1 >= end)
                    {
                        return out;
                    }
                    int b1 = data[pos++] & 0xFF;
                    int b2 = data[pos++] & 0xFF;
                    int word = (b2 << 8) | b1;

                    int rlen = ((word & 0x0F00) >>> 8) + 3;

                    int distLow8 = (word & 0x00FF);
                    int distHigh12 = (word & 0xF000) >>> 4;
                    int backDistance = distLow8 + distHigh12;

                    int rpos = outPos - backDistance;

                    if (rpos >= 0)
                    {
                        for (int i = 0; i < rlen && outPos < outputSize; i++)
                        {
                            out[outPos] = out[rpos + i];
                            outPos++;
                        }
                    }
                    else if (rpos + rlen > 0)
                    {
                        int skip = -rpos;
                        int copyCount = rlen - skip;
                        for (int i = 0; i < copyCount && outPos < outputSize; i++)
                        {
                            out[outPos] = out[i];
                            outPos++;
                        }
                    }
                    else
                    {
                        for (int i = 0; i < rlen && outPos < outputSize; i++)
                        {
                            out[outPos++] = 0x20; // ' '
                        }
                    }
                }
            }
        }

        if (verifyChecksum)
        {
            long actual = checksum(out);
            if ((actual & 0xFFFFFFFFL) != (expectedChecksum & 0xFFFFFFFFL))
            {
                throw new IllegalStateException(String.format(
                        "Checksum mismatch: expected=0x%08X, actual=0x%08X",
                        expectedChecksum, actual));
            }
        }

        return out;
    }

    private static long readU32LE(byte[] a, int off)
    {
        return ((long) (a[off] & 0xFF)) |
                ((long) (a[off + 1] & 0xFF) << 8) |
                ((long) (a[off + 2] & 0xFF) << 16) |
                ((long) (a[off + 3] & 0xFF) << 24);
    }

    private static int readU8(InputStream in) throws IOException
    {
        int v = in.read();
        if (v < 0) throw new EOFException("Unexpected end of stream");
        return v & 0xFF;
    }

    private static long readU32LE(InputStream in) throws IOException
    {
        int b0 = readU8(in);
        int b1 = readU8(in);
        int b2 = readU8(in);
        int b3 = readU8(in);
        return ((long) b0) | ((long) b1 << 8) | ((long) b2 << 16) | ((long) b3 << 24);
    }

    private static long checksum(byte[] out)
    {
        long sum = 0;
        for (byte b : out)
        {
            sum = (sum + (b & 0xFF)) & 0xFFFFFFFFL;
        }
        return sum;
    }
}
