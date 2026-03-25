package simple.orm.repo.anno;

import io.vavr.collection.Seq;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.IndexedInjector;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.map.NamedInjector;
import simple.orm.jdbc.query.QueryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide {@link RepoType#QUERY} repository method with query metadata.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleQuery {

    /**
     * Query type.
     *
     * @return query type.
     */
    QueryType type();

    /**
     * Strategy to obtain query parameters (both for injection and extraction).
     *
     * @return strategy to obtain query parameters.
     */
    ParameterStrategy parameters() default ParameterStrategy.PROVIDED;

    /**
     * Type of injection parameter(s).
     * <br>
     * Ignored if query has no injection parameters.
     * If equals {@link Seq} class - denotes {@link IndexedInjector},
     * otherwise defines a class for {@link NamedInjector}.
     *
     * @return type of injection parameter(s).
     */
    Class<?> sourceClass() default Seq.class;

    /**
     * Type of extraction parameter(s).
     * <br>
     * Ignored if query has no extraction parameters.
     * If equals {@link Seq} class - denotes {@link IndexedExtractor},
     * otherwise defines a class for {@link NamedExtractor}.
     *
     * @return type of injection parameter(s).
     */
    Class<?> targetClass() default Seq.class;

    /**
     * Query timeout in seconds.
     * <br><code>0</code> - no timeout.
     * <br><code>-1</code> - take default timeout from connection.
     *
     * @return query timeout in seconds.
     */
    int timeout() default -1;

}
