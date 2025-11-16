package com.danielele.rapify.RapModel;

import com.danielele.rapify.BinaryReader;
import com.danielele.rapify.IBinarizedRapEntry;
import com.danielele.rapify.IRapEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RapClass extends OrderableEntity
{
    private String name;
    private long offset;

    private String inheritedClassname;

    private long entries;

    private List<IBinarizedRapEntry> classes = new ArrayList<>();
    private List<IBinarizedRapEntry> externalClasses = new ArrayList<>();
    private List<IBinarizedRapEntry> deleteStatements = new ArrayList<>();
    private List<IBinarizedRapEntry> values = new ArrayList<>();

    public RapClass(int ordinal)
    {
        super(ordinal);
    }

    public List<IBinarizedRapEntry> getClasses()
    {
        return classes;
    }

    public void setClasses(List<IBinarizedRapEntry> classes)
    {
        this.classes = classes;
    }

    public List<IBinarizedRapEntry> getExternalClasses()
    {
        return externalClasses;
    }

    public void setExternalClasses(List<IBinarizedRapEntry> externalClasses)
    {
        this.externalClasses = externalClasses;
    }

    public List<IBinarizedRapEntry> getDeleteStatements()
    {
        return deleteStatements;
    }

    public void setDeleteStatements(List<IBinarizedRapEntry> deleteStatements)
    {
        this.deleteStatements = deleteStatements;
    }

    public List<IBinarizedRapEntry> getValues()
    {
        return values;
    }

    public void setValues(List<IBinarizedRapEntry> values)
    {
        this.values = values;
    }


    @Override
    public IBinarizedRapEntry fromBinary(BinaryReader reader, boolean arr, int ordinal)
    {
        this.setName(reader.readAsciiZ());
        this.setOffset(reader.readUInt32());
        return this;
    }

    public void AddEntry(IRapEntry entry, BinaryReader reader, int ordinal)
    {
//        switch (entry) {
//            case RapClass:
//                Classes.Add(reader.ReadBinarizedRapEntry<RapClass>());
//                break;
//            case RapExtern:
//                ExternalClasses.Add(reader.ReadBinarizedRapEntry<RapExtern>());
//                break;
//            case RapDelete:
//                DeleteStatements.Add(reader.ReadBinarizedRapEntry<RapDelete>());
//                break;
//            case RapValue val:
//                if (val.SubType == RapValueType.Array) {
//                    Values.Add(reader.ReadBinarizedRapEntry<RapValue>(true));
//                    break;
//                }
//
//                Values.Add(reader.ReadBinarizedRapEntry<RapValue>());
//                break;
//            default: throw new Exception("How did we get here?");
//        }

        switch (entry)
        {
            case RapClass c -> classes.add(reader.ReadBinarizedRapEntry(c, false, ordinal));
            case RapExternalClass exc -> externalClasses.add(reader.ReadBinarizedRapEntry(exc, false, ordinal));
            case RapDelete rd -> externalClasses.add(reader.ReadBinarizedRapEntry(rd, false, ordinal));
            case RapValue rv ->
            {
                if (rv.getSubType() == RapValueType.ARRAY)
                {
                    values.add(reader.ReadBinarizedRapEntry(rv, true, ordinal));
                    break;
                }
                values.add(reader.ReadBinarizedRapEntry(rv, false, ordinal));
            }

            default -> throw new IllegalStateException("Unexpected value: " + entry);
        }
    }

    @Override
    public String toConfigFormat(int depth)
    {
        ArrayList<IBinarizedRapEntry> objects = new ArrayList<>();
        objects.addAll(classes);
        objects.addAll(externalClasses);
        objects.addAll(deleteStatements);
        objects.addAll(values);
        objects.sort(Comparator.comparingInt(IBinarizedRapEntry::getOrder));

        String indent = "\t".repeat(depth);
        String innerIndent = "\t".repeat(depth + 1);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(indent).append("class ").append(name);

        if (!inheritedClassname.isBlank())
        {
            stringBuilder.append(" : ").append(inheritedClassname);
        }

        stringBuilder.append("\n").append(indent).append("{\n");

        objects.forEach(c -> stringBuilder
                .append(c.toConfigFormat(depth + 1)));

        stringBuilder.append("\n").append(indent).append("};").append("\n");

        return stringBuilder.toString();
    }

    public String getName()
    {
        return name;
    }

    public long getOffset()
    {
        return offset;
    }

    public String getInheritedClassname()
    {
        return inheritedClassname;
    }

    public void setInheritedClassname(String inheritedClassname)
    {
        this.inheritedClassname = inheritedClassname;
    }

    public long getEntries()
    {
        return entries;
    }

    public void setEntries(long entries)
    {
        this.entries = entries;
    }

    public void setOffset(long offset)
    {
        this.offset = offset;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
