package simple.orm.h2.type;

import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.AbstractTypeMapper;
import simple.orm.mapping.type.TypeMapper;

import java.util.Objects;

import static io.vavr.API.*;
import static simple.orm.util.StringUtils.qnn;

/**
 * {@link TypeMapper} storing {@link Boolean} value as a string (VARCHAR) in database.
 */
public class StringBooleanMapper extends AbstractTypeMapper<String, Boolean> {

    private final String trueStr;
    private final String falseStr;
    private final boolean caseSensitive;

    public StringBooleanMapper(ParameterJdbcType<String> jdbcType, String trueStr, String falseStr, boolean caseSensitive) {
        super(jdbcType, Boolean.class);
        if (trueStr == null) {
            throw new NullPointerException("trueStr is null");
        }
        if (falseStr == null) {
            throw new NullPointerException("falseStr is null");
        }
        this.trueStr = trueStr;
        this.falseStr = falseStr;
        this.caseSensitive = caseSensitive;
    }

    @Override
    public String javaToJdbc(Boolean value) {
        return value == null ?
                null :
                value ? trueStr : falseStr;
    }

    @Override
    public Boolean jdbcToJava(String value) {
        return Match(value).of(
                Case($(Objects::isNull), v -> null),
                Case($(this::isTrue), v -> true),
                Case($(this::isFalse), v -> false),
                Case($(), this::throwIllegal)
        );
    }

    private boolean isTrue(String value) {
        return caseSensitive ? trueStr.equals(value) : trueStr.equalsIgnoreCase(value);
    }

    private boolean isFalse(String value) {
        return caseSensitive ? falseStr.equals(value) : falseStr.equalsIgnoreCase(value);
    }

    private Boolean throwIllegal(String value) {
        throw new IllegalArgumentException("unexpected value: " + qnn(value));
    }

}
