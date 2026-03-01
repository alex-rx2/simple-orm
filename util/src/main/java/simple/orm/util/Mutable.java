package simple.orm.util;

import io.vavr.Function1;

/**
 * A wrap around immutable object to "make it mutable". This version is not thread-safe.
 * <br>
 * Common usage scenario is to declare a field of a class as <code>Mutable&lt;Immutable></code>
 * and use this wrap to modify the field.
 * <br>
 * <i>Note:</i> for thread-safe variant see {@link SafeMutable}.
 * <i>Note:</i> <code>null</code> values are not allowed.
 */
public class Mutable<T> {

    private T value;

    /**
     * Creates new mutable wrap around immutable object.
     *
     * @param value initial value.
     * @throws NullPointerException if value is null.
     */
    protected Mutable(T value) {
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        this.value = value;
    }

    /**
     * Get the current value concurrently.
     *
     * @return current value.
     */
    public T get() {
        return value;
    }

    /**
     * Set new value.
     *
     * @param newValue new value.
     * @throws NullPointerException if value is null.
     */
    public void set(T newValue) {
        if (newValue == null) {
            throw new NullPointerException("new value is null");
        }
        value = newValue;
    }

    /**
     * Set the new value calculated off the current one.
     *
     * @param mutator a function accepting current value and returning new value to set.
     * @throws NullPointerException if mutator or the new value are null.
     */
    public T apply(Function1<T, T> mutator) {
        if (mutator == null) {
            throw new NullPointerException("mutator is null");
        }
        T newValue = mutator.apply(value);
        if (newValue == null) {
            throw new NullPointerException("new value is null");
        }
        if (value != newValue) {
            value = newValue;
        }
        return value;
    }

    /**
     * Factory method to create a new {@link Mutable} object.
     *
     * @param value the value to "make mutable".
     * @param <T>   type of wrapped value.
     * @return new {@link Mutable} object wrapping provided value.
     */
    public static <T> Mutable<T> of(T value) {
        return new Mutable<>(value);
    }

}
