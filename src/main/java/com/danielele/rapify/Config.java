package com.danielele.rapify;

import java.util.ArrayList;
import java.util.List;

public class Config
{
    public int entries;
    public List<IBinarizedRapEntry> classes = new ArrayList<>();

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        classes.forEach(c -> stringBuilder.append(c.toConfigFormat(0)));
        return stringBuilder.toString();
    }
}
