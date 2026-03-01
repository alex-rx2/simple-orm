package simple.orm.mapping.named;

import java.util.Objects;

/**
 * Referenced by name. A concept of something referenced by name in reflections API.
 *
 * @param name   the name.
 * @param target the target class.
 */
public record RefName(String name, Class<?> target) {

    public RefName {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RefName(String name2, Class<?> target2))) {
            return false;
        }
        return Objects.equals(name, name2)
                && Objects.equals(target, target2);
    }

    @Override
    public String toString() {
        return "RefName(" + name + ',' + target.getName() + ')';
    }

}
