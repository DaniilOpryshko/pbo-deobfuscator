package com.danielele.rapify;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Rapifier
{
    public static String main(byte[] in)
    {
        long l = System.currentTimeMillis();
        BinaryParser binaryParser = new BinaryParser(new BinaryReader(in));
        Config parse = binaryParser.parse();
        return parse.toString();
    }
}