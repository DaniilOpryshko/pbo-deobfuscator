package com.danielele.rapify.RapModel.value;

public class RapVariableValue extends BaseRapValue<String>
{

    public RapVariableValue(String value)
    {
        super(value);
    }

    @Override
    public String toConfigFormat(int depth)
    {
        return value;
    }
}
