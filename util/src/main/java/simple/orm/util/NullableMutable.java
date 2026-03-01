package simple.orm.util;

import io.vavr.Function1;

/**
 * A wrap around immutable object to "make it mutable".
 * This is a version of {@link Mutable} that allow <code>null</code> to be used.
 */
public class NullableMutable<T> {

    private T value;

    /**
     * Creates new mutable wrap around immutable object.
     *
     * @param value initial value.
     */
    protected NullableMutable(T value) {
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
     * Checks whether current value is null.
     *
     * @return <code>true</code> if current value is null, <code>false</code> otherwise.
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * Checks whether current value is not null.
     *
     * @return <code>true</code> if current value is not null, <code>false</code> otherwise.
     */
    public boolean isDefined() {
        return value != null;
    }

    /**
     * Set new value.
     *
     * @param newValue new value.
     */
    public void set(T newValue) {
        value = newValue;
    }

    /**
     * Set the new value calculated off the current one.
     *
     * @param mutator a function accepting current value and returning new value to set.
     */
    public T apply(Function1<T, T> mutator) {
        if (mutator == null) {
            throw new NullPointerException("mutator is null");
        }
        T newValue = mutator.apply(value);
        if (value != newValue) {
            value = newValue;
        }
        return value;
    }

    /**
     * Factory method to create a new {@link NullableMutable} object.
     *
     * @param value the value to "make mutable".
     * @param <T>   type of wrapped value.
     * @return new {@link NullableMutable} object wrapping provided value.
     */
    public static <T> NullableMutable<T> of(T value) {
        return new NullableMutable<>(value);
    }

}
