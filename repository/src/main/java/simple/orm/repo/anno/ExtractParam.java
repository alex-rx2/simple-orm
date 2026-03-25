package simple.orm.repo.anno;

import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.mapping.param.ParameterJdbcType;
import simple.orm.mapping.type.TypeMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extraction parameter metadata.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ResultExtractor.class)
public @interface ExtractParam {

    /**
     * Column index in a query result (starting with 1 as per JDBC API).
     * <br>
     * May be equal to <code>-1</code>; if all indexes are equal to <code>-1</code>
     * parameters will be indexed according to the order of annotations (if no labels are provided).
     *
     * @return column index.
     */
    int index() default -1;

    /**
     * Column label in a query result.
     * <br>
     * Empty string means label is not specified.
     * <br>
     * All query {@link ExtractParam} labels must be either empty, either non-empty.
     * If labels are provided they are used to extract values from ResultSet row instead of indexes
     * (which may be omitted then and are ignored).
     *
     * @return column label.
     */
    String label() default "";

    /**
     * Property name, if {@link NamedExtractor} is intended to be used.
     * <br>
     * Empty string means property name is not specified.
     *
     * @return property name for {@link NamedExtractor}.
     */
    String prop() default "";

    /**
     * {@link TypeMapper} name.
     * <br>
     * Empty string means mapper name is not specified.
     *
     * @return type mapper name.
     */
    String mapper() default "";

    /**
     * {@link TypeMapper} tag.
     * <br>
     * Empty string means mapper tag is not specified.
     *
     * @return type mapper tag.
     */
    String tag() default "";

    /**
     * {@link TypeMapper} {@link ParameterJdbcType} name.
     * <br>
     * Empty string means JDBC type name is not specified.
     *
     * @return type mapper JDBC type name.
     */
    String jdbc() default "";

    /**
     * {@link TypeMapper} Java type.
     * <br>
     * <code>Object.class</code> is considered as Java type is not unspecified.
     *
     * @return type mapper Java type.
     */
    Class<?> java() default Object.class;

}
