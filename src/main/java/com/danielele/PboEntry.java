package com.danielele;

import java.util.Arrays;

public class PboEntry {
        public String filename;
        public String packagingMethod;
        public long originalSize;
        public long reserved;
        public long timestamp;
        public long dataSize;
        public byte[] content;
        public boolean rapified = false;
        public boolean binary = false;
        public String unrapified;
        public String noBinaryContet;


    @Override
    public String toString()
    {
        return "PboEntry{" +
                "filename='" + filename + '\'' +
                ", packagingMethod='" + packagingMethod + '\'' +
                ", originalSize=" + originalSize +
                ", reserved=" + reserved +
                ", timestamp=" + timestamp +
                ", dataSize=" + dataSize +
                ", content size=" + content.length +
                ", rapified=" + rapified +
                ", binary=" + binary +
                '}';
    }
}