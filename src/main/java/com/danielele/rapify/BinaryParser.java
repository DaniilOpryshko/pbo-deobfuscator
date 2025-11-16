package com.danielele.rapify;

import com.danielele.rapify.RapModel.*;

public class BinaryParser
{
    private long enumOfsset;
    private final BinaryReader reader;

    public BinaryParser(BinaryReader reader)
    {
        this.reader = reader;
    }

    public Config parse()
    {
        Config config = new Config();
        if (!reader.checkHeader())
        {
            System.err.println("Not rapified");
        }
        var always0 = reader.readUInt32();
        var always8 = reader.readUInt32();

        if (always0 != 0 && always8 != 8)
        {
            System.err.println("Hueta");
        }

        enumOfsset = reader.readUInt32();

        readParentClasses(config);

        readChildClasses(config);

        return config;
    }

    private boolean readParentClasses(Config config)
    {
        reader.readAsciiZ();
        config.entries = reader.readCompressedInteger();

        for (var i = 0; i < config.entries; ++i)
        {
//            reader.readByte();
//            config.classes.add(reader.ReadBinarizedRapEntry(new RapClass(reader), false));
            AddEntryToClass(config, i);
        }

        return !config.classes.isEmpty();
    }

    private boolean readChildClasses(Config config)
    {
        config.classes.forEach(c ->
        {
            if (c instanceof RapClass rapClass)
            {
                loadChildrenClasses(rapClass);
            }
        });
        return !config.classes.isEmpty();
    }

    private void loadChildrenClasses(RapClass child)
    {
        reader.setPosition(child.getOffset());

        child.setInheritedClassname(reader.readAsciiZ());
        child.setEntries(reader.readCompressedInteger());

        // Just used for repeating X times to add all entries.
        for (var i = 0; i < child.getEntries(); ++i)
        {
            AddEntryToClass(child, i);
        }

        // Recursively load child class children.
        child.getClasses().forEach(c ->
        {
            if (c instanceof RapClass rc)
            {
                loadChildrenClasses(rc);
            }
        });
    }

    private void AddEntryToClass(RapClass rapClass, int ordinal)
    {
        var entryType = RapEntryType.fromInt(reader.readByte());

        var entry = createEntryForType(entryType, ordinal);
        rapClass.AddEntry(entry, reader, ordinal);
    }

    private void AddEntryToClass(Config config, int ordinal)
    {
        var entryType = RapEntryType.fromInt(reader.readByte());

        var entry = createEntryForType(entryType, ordinal);
        IBinarizedRapEntry iBinarizedRapEntry = entry.fromBinary(reader, entryType == RapEntryType.RapArray, ordinal);
        config.classes.add(iBinarizedRapEntry);
    }

    private IBinarizedRapEntry createEntryForType(RapEntryType entryType, int ordinal)
    {
        switch (entryType)
        {
            case RapClass:
                return new RapClass(ordinal);
            case RapValue:
                return new RapValue(ordinal);
            case RapArray:
                RapValue rapValue = new RapValue(ordinal);
                rapValue.setSubType(RapValueType.ARRAY);
                rapValue.setFlaggedArray(false);
                return rapValue;
            case RapArrayFlag:
                RapValue rapValue1 = new RapValue(ordinal);
                rapValue1.setFlaggedArray(true);
                rapValue1.setSubType(RapValueType.ARRAY);
                return rapValue1;
            case RapExternClass:
                return new RapExternalClass(ordinal);
            case RapDeleteClass:
                return new RapDelete(ordinal);
            default:
                return null;
        }
    }
}
