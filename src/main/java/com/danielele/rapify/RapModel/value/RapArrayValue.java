package com.danielele.rapify.RapModel.value;

import com.danielele.rapify.IRapEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RapArrayValue extends BaseRapValue<List<IRapEntry>>
{

    private int entryCount;
    private List<IRapEntry> value = new ArrayList<>();

    public RapArrayValue(List<IRapEntry> value)
    {
        super(value);
    }

    public int getEntryCount()
    {
        return entryCount;
    }

    public void setEntryCount(int entryCount)
    {
        this.entryCount = entryCount;
    }

    @Override
    public List<IRapEntry> getValue()
    {
        return value;
    }

    public void setValue(List<IRapEntry> value)
    {
        this.value = value;
    }

    @Override
    public String toConfigFormat(int depth)
    {
        if (value == null)
        {
            throw new NullPointerException("Value is null");
        }

        String joined = value.stream()
                .map(iRapEntry -> iRapEntry.toConfigFormat(depth))
                .collect(Collectors.joining(", "));

        return ("{" + joined + "}")
                .replace("{{", "{")
                .replace("}}", "}");
    }
}
