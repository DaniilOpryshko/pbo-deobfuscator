package com.danielele;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pbo {
        public Map<String, String> headers = new LinkedHashMap<>();
        public List<PboEntry> entries = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Pbo{" +
                    "headers=" + headers);
            entries.forEach(entry -> stringBuilder.append("\n\t").append("Entry").append(entry));
            return stringBuilder.toString();
        }
    }