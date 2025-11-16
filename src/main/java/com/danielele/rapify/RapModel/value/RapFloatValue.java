package com.danielele.rapify.RapModel.value;

public class RapFloatValue extends BaseRapValue<Float>
{
    public RapFloatValue(Float value)
    {
        super(value);
    }

    @Override
    public String toConfigFormat(int depth)
    {
        return Float.toString(value);
    }
}
