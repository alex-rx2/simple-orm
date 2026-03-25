package simple.orm.util;

import io.vavr.collection.HashMap;

/**
 * A class intended to be used as {@link HashMap} key with some "speed up" quirks.
 * <br>
 * This class cashes {@link Object#hashCode()} and before calling {@link Object#equals(Object)} performs <code>==</code> comparison.
 */
public class FastHashKey<T> {

    public final T key;
    public final int hash;

    public FastHashKey(T key) {
        this.key = key;
        this.hash = key == null ? -1 : key.hashCode();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (key == obj) return true;
        if (key != null) return obj != null && key.equals(obj);
        return false;
    }

    @Override
    public String toString() {
        return "FastHashKey(" + (key == null ? "'null'" : key) + ")";
    }

}
