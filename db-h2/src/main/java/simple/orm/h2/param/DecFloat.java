package simple.orm.h2.param;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Implementation of DECFLOAT data type.
 * <br>
 * DECFLOAT is a {@link BigDecimal} plus 3 special values <code>Infinity</code>, <code>-Infinity</code>, and <code>NaN</code>.
 */
public class DecFloat {

    public enum Type {
        BIGDECIMAL(null),
        INFINITY("Infinity"),
        NINFINITY("-Infinity"),
        NAN("NaN");

        public final String special;

        Type(String special) {
            this.special = special;
        }

        public static Type of(String value) {
            if (INFINITY.special.equals(value)) {
                return INFINITY;
            } else if (NINFINITY.special.equals(value)) {
                return NINFINITY;
            } else if (NAN.special.equals(value)) {
                return NAN;
            } else {
                return BIGDECIMAL;
            }
        }
    }

    public final Type type;
    public final BigDecimal value;

    public DecFloat(Type type, BigDecimal value) {
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        if (type == Type.BIGDECIMAL && value == null) {
            throw new NullPointerException("value is null");
        } else if (type != Type.BIGDECIMAL && value != null) {
            throw new IllegalArgumentException("value should be null");
        }
        this.type = type;
        this.value = value;
    }

    public boolean isSpecial() {
        return type != Type.BIGDECIMAL;
    }

    public boolean isInfinity() {
        return type == Type.INFINITY;
    }

    public boolean isNegativeInfinity() {
        return type == Type.NINFINITY;
    }

    public boolean isNotANumber() {
        return type == Type.NAN;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DecFloat other) {
            return this.type == other.type &&
                    this.value == null
                    ? other.value == null
                    : other.value != null && this.value.compareTo(other.value) == 0
                    ;
        }
        return false;
    }

    @Override
    public String toString() {
        return type == Type.BIGDECIMAL ? value.toString() : type.special;
    }

    public static DecFloat of(BigDecimal value) {
        return new DecFloat(Type.BIGDECIMAL, value);
    }

    public static DecFloat infinity() {
        return INF;
    }

    public static DecFloat ninfinity() {
        return NINF;
    }

    public static DecFloat nan() {
        return NAN;
    }

    private static final DecFloat INF = new DecFloat(Type.INFINITY, null);
    private static final DecFloat NINF = new DecFloat(Type.NINFINITY, null);
    private static final DecFloat NAN = new DecFloat(Type.NAN, null);

}
