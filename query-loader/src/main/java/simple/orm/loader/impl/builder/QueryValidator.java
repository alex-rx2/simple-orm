package simple.orm.loader.impl.builder;

import io.vavr.collection.Traversable;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.StrategyType;
import simple.orm.loader.builder.QueryParameter;

import static simple.orm.loader.StrategyType.*;
import static simple.orm.loader.builder.ParameterType.*;
import static simple.orm.util.StringUtils.empty;

/**
 * Collection of utility methods validating strategies/parameters for different query types.
 */
public class QueryValidator {

    private static QueryValidator INSTANCE = null;

    public static QueryValidator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QueryValidator();
        }
        return INSTANCE;
    }

    private QueryValidator() {
    }

    public void validateDDL(InjectionStrategy<?> injectionStrategy,
                            ExtractionStrategy<?> extractionStrategy,
                            Traversable<QueryParameter> params) {
        if (injectionStrategy.type != NONE) {
            throw new IllegalArgumentException(
                    "DDL query injection strategy = " + extractionStrategy.type + ", should be " + NONE
            );
        }
        if (extractionStrategy.type != NONE) {
            throw new IllegalArgumentException(
                    "DDL query extraction strategy = " + extractionStrategy.type + ", should be " + NONE
            );
        }
        if (!params.isEmpty()) {
            throw new IllegalArgumentException("DDL query should have no parameters");
        }
    }

    public void validateDML(InjectionStrategy<?> injectionStrategy,
                            ExtractionStrategy<?> extractionStrategy,
                            Traversable<QueryParameter> params) {
        if (extractionStrategy.type != NONE) {
            throw new IllegalArgumentException(
                    "DML query extraction strategy = " + extractionStrategy.type + ", should be " + NONE
            );
        }
        validateInjectionParams(injectionStrategy.type, params);
        validateExtractionParams(extractionStrategy.type, params);
    }

    public void validateSelect(InjectionStrategy<?> injectionStrategy,
                               ExtractionStrategy<?> extractionStrategy,
                               Traversable<QueryParameter> params) {
        if (extractionStrategy.type == NONE) {
            throw new IllegalArgumentException(
                    "select query extraction strategy is " + NONE
            );
        }
        validateInjectionParams(injectionStrategy.type, params);
        validateExtractionParams(extractionStrategy.type, params);
    }

    private void validateInjectionParams(StrategyType type, Traversable<QueryParameter> params) {
        final Traversable<QueryParameter> iParams =
                params.filter(p -> p.type() == INJECTION);
        switch (type) {
            case NONE -> {
                // must be no injection params
                if (!iParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "query has injection parameters, while injection strategy = " + NONE
                    );
                }
            }
            case INDEXED -> {
                // validate actually has params
                if (iParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "should have at least one parameter when using injection strategy " + INDEXED
                    );
                }
                // validate indexing 1...n
                if (iParams.toList()
                        .zipWithIndex()
                        .find(t2 -> t2._1.indexWithinType() != t2._2 + 1)
                        .isDefined()) {
                    throw new IllegalArgumentException(
                            "broken parameter indexing for injection strategy " + INDEXED
                    );
                }
                // validate no directly specified labels
                if (iParams.find(p -> !p.labelGuessed() && !empty(p.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "parameters should have no label defined for injection strategy " + INDEXED
                    );
                }
            }
            case NAMED -> {
                // validate actually has params
                if (iParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "should have at least one parameter when using injection strategy " + NAMED
                    );
                }
                // 1. validate indexing 1...n
                // 2. all parameters have NONEMPTY propName
                // 3. has no labels
                if (iParams.toList()
                        .zipWithIndex()
                        .find(t2 -> t2._1.indexWithinType() != t2._2 + 1)
                        .isDefined()) {
                    throw new IllegalArgumentException(
                            "broken parameter indexing for injection strategy " + NAMED
                    );
                }
                if (iParams.find(p -> empty(p.propName())).isDefined()) {
                    throw new IllegalArgumentException(
                            "each parameter should have nonempty property name for injection strategy " + NAMED
                    );
                }
                if (iParams.find(p -> !empty(p.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "parameters should have no label defined for injection strategy " + INDEXED
                    );
                }
            }
        }
    }

    private void validateExtractionParams(StrategyType type, Traversable<QueryParameter> parsedParams) {
        final Traversable<QueryParameter> eParams =
                parsedParams.filter(p -> p.type() == EXTRACTION);
        switch (type) {
            case NONE -> {
                // must be no extraction params
                if (!eParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "query has extraction parameters, while extraction strategy = " + NONE
                    );
                }
            }
            case INDEXED -> {
                // validate actually has params
                if (eParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "should have at least one parameter when using extraction strategy " + INDEXED
                    );
                }
                // validate indexing 1...n
                if (eParams.toList()
                        .zipWithIndex()
                        .find(t2 -> t2._1.indexWithinType() != t2._2 + 1)
                        .isDefined()) {
                    throw new IllegalArgumentException(
                            "broken parameter indexing for extraction strategy " + INDEXED
                    );
                }
                // validate no directly specified labels
                if (eParams.find(p -> !p.labelGuessed() && !empty(p.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "parameters should have no label defined for extraction strategy " + INDEXED
                    );
                }
            }
            case NAMED -> {
                // validate actually has params
                if (eParams.isEmpty()) {
                    throw new IllegalArgumentException(
                            "should have at least one parameter when using extraction strategy " + NAMED
                    );
                }
                // 1. validate all params either indexed, either labelled
                // 1.1 validate indexing 1...n (in case of indexes)
                // 1.2 validate all labels NONEMPTY (in case of labels)
                // 2. all parameters have NONEMPTY propName
                // 3. propNames should have no duplicates
                final boolean hasLabels = eParams.find(p -> !empty(p.label())).isDefined();
                if (hasLabels) {
                    // all should have nonempty labels
                    if (eParams.find(p -> empty(p.label())).isDefined()) {
                        throw new IllegalArgumentException(
                                "some parameters have labels, some don't" +
                                        " (either all should have nonempty label, either none at all)" +
                                        " for extraction strategy " + NAMED
                        );
                    }
                } else {
                    // all should be properly indexed
                    if (eParams.toList()
                            .zipWithIndex()
                            .find(t2 -> t2._1.indexWithinType() != t2._2 + 1)
                            .isDefined()) {
                        throw new IllegalArgumentException(
                                "broken parameter indexing for extraction strategy " + NAMED
                        );
                    }
                }
                // validate propNames
                if (eParams.find(p -> empty(p.propName()) && empty(p.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "each parameter should have nonempty property name or label (which will be used instead)" +
                                    " for extraction strategy " + NAMED
                    );
                }
                if (eParams
                        .groupBy(p -> empty(p.propName()) ? p.label() : p.propName())
                        .find(t2 -> t2._2.size() > 1).isDefined()) {
                    throw new IllegalArgumentException(
                            "duplicate property names found" +
                                    " (each parameter should be extracted into separate property, label is used if no property name specified)" +
                                    " for extraction strategy " + NAMED
                    );
                }
            }
        }
    }

}
