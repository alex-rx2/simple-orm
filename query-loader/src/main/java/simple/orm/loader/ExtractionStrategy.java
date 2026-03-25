package simple.orm.loader;

import io.vavr.collection.Seq;
import simple.orm.jdbc.Result;
import simple.orm.jdbc.map.IndexedExtractor;
import simple.orm.jdbc.map.NamedExtractor;
import simple.orm.jdbc.query.Query;
import simple.orm.jdbc.query.QueryType;

import java.util.Objects;

/**
 * Class defining result extraction strategy {@link QueryLoader} should use when creating {@link Query}.
 *
 * @param <T> type of result
 *            (<code>Seq&lt;Object></code> for {@link IndexedExtractor},
 *            <code>Void</code> for query without {@link Result}).
 */
public final class ExtractionStrategy<T> {

    /**
     * Factory method for no-result strategy
     * ({@link QueryLoader} should build {@link Query} without extractor, query has no {@link Result} to return).
     * <br>
     * Returns {@link ExtractionStrategy} compatible with {@link QueryType#DDL} queries typing.
     *
     * @return no-result strategy for DDL queries.
     */
    public static ExtractionStrategy<Void> noneDdl() {
        return new ExtractionStrategy<>(StrategyType.NONE, null);
    }

    /**
     * Factory method for no-result strategy
     * ({@link QueryLoader} should build {@link Query} without extractor, query has no {@link Result} to return).
     * <br>
     * Returns {@link ExtractionStrategy} compatible with {@link QueryType#DML} queries typing.
     *
     * @return no-result strategy for DML queries.
     */
    public static ExtractionStrategy<Integer> noneDml() {
        return new ExtractionStrategy<>(StrategyType.NONE, null);
    }

    /**
     * Factory method for indexed strategy ({@link QueryLoader} should build {@link Query} with {@link IndexedExtractor}).
     *
     * @return indexed strategy.
     */
    public static ExtractionStrategy<Seq<Object>> indexed() {
        return new ExtractionStrategy<>(StrategyType.INDEXED, null);
    }

    /**
     * Factory method for named strategy ({@link QueryLoader} should build {@link Query} with {@link NamedExtractor}).
     *
     * @param targetClass class of result object.
     * @param <T>         type of result object constructed from each {@link java.sql.ResultSet} row.
     * @return named strategy.
     */
    public static <T> ExtractionStrategy<T> named(Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass is null");
        }
        return new ExtractionStrategy<>(StrategyType.NAMED, targetClass);
    }

    public final StrategyType type;
    public final Class<T> targetClass;

    private ExtractionStrategy(StrategyType type, Class<T> targetClass) {
        this.type = type;
        this.targetClass = targetClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, targetClass);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ExtractionStrategy<?> that)) {
            return false;
        }
        return this.type == that.type
                && Objects.equals(this.targetClass, that.targetClass);
    }
}
