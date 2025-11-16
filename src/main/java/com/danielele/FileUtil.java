package com.danielele;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FileUtil
{
    public static boolean isBinaryUTF8(byte[] data)
    {
        java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT);
        try
        {
            decoder.decode(ByteBuffer.wrap(data));
            return false;
        }
        catch (java.nio.charset.CharacterCodingException e)
        {
            return true;
        }
    }
}