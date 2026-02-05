package simple.orm.util;

import io.vavr.Function1;

/**
 * A wrap around immutable object to "make it mutable" in a thread-safe manner.
 * All changes to the internal value are performed in a synchronized code block, so no concurrent changes are possible.
 * <br>
 * Common usage scenario is to declare a field of a class as <code>SafeMutable&lt;Immutable></code>
 * and use this wrap to modify the field in a safe way.
 * <br>
 * <i>Note:</i> <code>null</code> values are not allowed.
 */
public class SafeMutable<T> {

    private T value;

    /**
     * Creates new mutable wrap around immutable object.
     *
     * @param value initial value.
     * @throws NullPointerException if value is null.
     */
    protected SafeMutable(T value) {
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
     * Get the current value thread-safe way (in synchronized block).
     *
     * @return current value.
     */
    public T getSafe() {
        synchronized (this) {
            return value;
        }
    }

    /**
     * Set new value.
     * <br>
     * Operation is performed in synchrtonized block.
     *
     * @param newValue new value.
     * @throws NullPointerException if value is null.
     */
    public void set(T newValue) {
        if (newValue == null) {
            throw new NullPointerException("new value is null");
        }
        synchronized (this) {
            value = newValue;
        }
    }

    /**
     * Set the new value calculated off the current one.
     * <br>
     * Application of mutator is performed in synchronized block.
     *
     * @param mutator a function accepting current value and returning new value to set.
     * @throws NullPointerException if mutator or the new value are null.
     */
    public T apply(Function1<T, T> mutator) {
        if (mutator == null) {
            throw new NullPointerException("mutator is null");
        }
        synchronized (this) {
            T newValue = mutator.apply(value);
            if (newValue == null) {
                throw new NullPointerException("new value is null");
            }
            if (value != newValue) {
                value = newValue;
            }
            return value;
        }
    }

    /**
     * Factory method to create a new {@link SafeMutable} object.
     *
     * @param value the value to "make mutable".
     * @param <T>   type of wrapped value.
     * @return new {@link SafeMutable} object wrapping provided value.
     */
    public static <T> SafeMutable<T> of(T value) {
        return new SafeMutable<>(value);
    }

}
