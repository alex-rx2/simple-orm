package simple.orm.repo.impl.meta;

import io.vavr.collection.Traversable;
import simple.orm.jdbc.query.QueryType;
import simple.orm.loader.builder.QueryParameter;
import simple.orm.repo.anno.ParameterStrategy;

public record QueryMethodMeta(
        String methodName,
        QueryType type,
        String querySQL,
        String queryURI,
        String queryURICharset,
        ParameterStrategy paramStrat,
        Class<?> sourceClass, // not null for named injector only (Seq.class in annotation -> null)
        Class<?> targetClass, // not null for named extractor only (Seq.class in annotation -> null)
        Traversable<QueryParameter> injectParams,
        Traversable<QueryParameter> extractParams,
        int queryTimeout
) {

    public QueryMethodMeta replaceSQL(String newQuerySQL) {
        return new QueryMethodMeta(
                methodName,
                type,
                newQuerySQL,
                queryURI,
                queryURICharset,
                paramStrat,
                sourceClass,
                targetClass,
                injectParams,
                extractParams,
                queryTimeout
        );
    }

    public QueryMethodMeta replaceParams(Traversable<QueryParameter> newInjectParams,
                                         Traversable<QueryParameter> newExtractParams) {
        return new QueryMethodMeta(
                methodName,
                type,
                querySQL,
                queryURI,
                queryURICharset,
                paramStrat,
                sourceClass,
                targetClass,
                newInjectParams,
                newExtractParams,
                queryTimeout
        );
    }

    public QueryMethodMeta replaceTimeout(int newTimeout) {
        return new QueryMethodMeta(
                methodName,
                type,
                querySQL,
                queryURI,
                queryURICharset,
                paramStrat,
                sourceClass,
                targetClass,
                injectParams,
                extractParams,
                newTimeout
        );
    }

}
