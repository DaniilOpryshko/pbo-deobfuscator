package com.danielele.rapify.RapModel;

/**
 * Represents RAP value types.
 * Each enum constant corresponds to a specific binary type ID.
 */
public enum RapValueType {
    /**
     * Type ID: 0, Value: AsciiZ string.
     */
    STRING(0),

    /**
     * Type ID: 1, Value: 4-byte float.
     */
    FLOAT(1),

    /**
     * Type ID: 2, Value: 4-byte long (UInt32).
     */
    LONG(2),

    /**
     * Type ID: 3, Value: RapArray (not used).
     */
    ARRAY(3),

    /**
     * Type ID: 4, Value: public or private variable (XBOX only).
     */
    VARIABLE(4);

    private final int id;

    RapValueType(int id) {
        this.id = id;
    }

    /** Returns the numeric type ID. */
    public int getId() {
        return id;
    }

    /** Returns enum by numeric ID (or null if not found). */
    public static RapValueType fromId(int id) {
        for (RapValueType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }
}
