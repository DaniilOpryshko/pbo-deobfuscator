package com.danielele.rapify.RapModel.value;

public final class RapStringValue extends BaseRapValue<String>
{
    public RapStringValue(String str)
    {
        super(str);
    }

    private String getQuoted()
    {
        return "\"" + value + "\"";
    }

    @Override
    public String toConfigFormat(int depth)
    {
        return getQuoted();
    }
}
