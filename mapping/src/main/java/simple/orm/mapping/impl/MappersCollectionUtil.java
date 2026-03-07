package simple.orm.mapping.impl;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import simple.orm.mapping.param.ParameterJdbcType;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Helper methods for {@link MappersCollectionImpl}.
 */
public final class MappersCollectionUtil {
    private MappersCollectionUtil() {
    }

    // --- EQUALS

    static Predicate<TypeMapperReg> mappersEqualPredicate(boolean caseSensitive, TypeMapperReg mapper) {
        return mapper2 -> mappersEqual(caseSensitive, mapper, mapper2);
    }

    static boolean mappersEqual(boolean caseSensitive, TypeMapperReg mapper1, TypeMapperReg mapper2) {
        if (mapper1 == mapper2) return true;
        else if (mapper1 == null || mapper2 == null) return false;
        else {
            boolean namesEqual = caseSensitive ?
                    Objects.equals(mapper1.name(), mapper2.name()):
                    String.CASE_INSENSITIVE_ORDER.compare(mapper1.name(), mapper2.name()) == 0;
            return namesEqual
                    && Objects.equals(mapper1.jdbcType(), mapper2.jdbcType())
                    && Objects.equals(mapper1.javaType(), mapper2.javaType())
                    && Objects.equals(mapper1.tag(), mapper2.tag())
                    ;
        }
    }

    // --- ADD, REPLACE

    static Map<String, List<TypeMapperReg>> addByName(
            Map<String, List<TypeMapperReg>> map,
            TypeMapperReg mapper
    ) {
        return map.put(
                mapper.name(),
                map.getOrElse(mapper.name(), List.empty())
                        .append(mapper)
        );
    }

    static Map<String, List<TypeMapperReg>> replaceByName(
            boolean caseSensitive,
            Map<String, List<TypeMapperReg>> map,
            TypeMapperReg mapper
    ) {
        return map.put(
                mapper.name(),
                map.getOrElse(mapper.name(), List.empty())
                        .removeFirst(mappersEqualPredicate(caseSensitive, mapper))
                        .append(mapper)
        );
    }

    static Map<ParameterJdbcType<?>, List<TypeMapperReg>> addByType(
            Map<ParameterJdbcType<?>, List<TypeMapperReg>> map,
            TypeMapperReg mapper
    ) {
        return map.put(
                mapper.jdbcType(),
                map.getOrElse(mapper.jdbcType(), List.empty())
                        .append(mapper)
        );
    }

    static Map<ParameterJdbcType<?>, List<TypeMapperReg>> replaceByType(
            boolean caseSensitive,
            Map<ParameterJdbcType<?>, List<TypeMapperReg>> map,
            TypeMapperReg mapper
    ) {
        return map.put(
                mapper.jdbcType(),
                map.getOrElse(mapper.jdbcType(), List.empty())
                        .removeFirst(mappersEqualPredicate(caseSensitive, mapper))
                        .append(mapper)
        );
    }

}
