package com.danielele.rapify.RapModel.value;

public class RapIntegerValue extends BaseRapValue<Long>
{
    public RapIntegerValue(Long value)
    {
        super(value);
    }

    @Override
    public String toConfigFormat(int depth)
    {
        return String.valueOf(value);
    }
}
