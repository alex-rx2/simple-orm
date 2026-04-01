package simple.orm.mapping.named;

import java.util.Objects;

/**
 * Referenced by name. A concept of something referenced by name in reflections API.
 */
public final class RefName {

    public final String name;
    public final Class<?> target;
    private final int hashCode;

    public RefName(String name, Class<?> target) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }
        this.name = name;
        this.target = target;
        this.hashCode = Objects.hash(name, target);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof RefName that
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.target, that.target);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "RefName(" + name + ',' + target.getName() + ')';
    }

}
