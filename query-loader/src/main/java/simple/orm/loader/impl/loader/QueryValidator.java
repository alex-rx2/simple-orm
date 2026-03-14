package simple.orm.loader.impl.loader;

import io.vavr.collection.Traversable;
import simple.orm.loader.ExtractionStrategy;
import simple.orm.loader.InjectionStrategy;
import simple.orm.loader.QueryParser;
import simple.orm.loader.StrategyType;

import static simple.orm.loader.QueryParser.ParamType.*;
import static simple.orm.loader.StrategyType.*;

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
                            Traversable<QueryParser.QueryParam> parsedParams) {
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
        if (!parsedParams.isEmpty()) {
            throw new IllegalArgumentException("DDL query should have no parameters");
        }
    }

    public void validateDML(InjectionStrategy<?> injectionStrategy,
                            ExtractionStrategy<?> extractionStrategy,
                            Traversable<QueryParser.QueryParam> parsedParams) {
        if (extractionStrategy.type != NONE) {
            throw new IllegalArgumentException(
                    "DML query extraction strategy = " + extractionStrategy.type + ", should be " + NONE
            );
        }
        validateInjectionParams(injectionStrategy.type, parsedParams);
        validateExtractionParams(extractionStrategy.type, parsedParams);
    }

    public void validateSelect(InjectionStrategy<?> injectionStrategy,
                               ExtractionStrategy<?> extractionStrategy,
                               Traversable<QueryParser.QueryParam> parsedParams) {
        if (extractionStrategy.type == NONE) {
            throw new IllegalArgumentException(
                    "select query extraction strategy is " + NONE
            );
        }
        validateInjectionParams(injectionStrategy.type, parsedParams);
        validateExtractionParams(extractionStrategy.type, parsedParams);
    }

    private void validateInjectionParams(StrategyType type, Traversable<QueryParser.QueryParam> parsedParams) {
        final Traversable<QueryParser.QueryParam> iParams =
                parsedParams.filter(qp -> qp.type() == INJECTION);
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
                            "broken parameter indexing returned by parser for injection strategy " + INDEXED
                    );
                }
                // validate no directly specified labels
                if (iParams.find(qp -> !qp.labelGuessed() && !empty(qp.label())).isDefined()) {
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
                            "broken parameter indexing returned by parser for injection strategy " + NAMED
                    );
                }
                if (iParams.find(qp -> empty(qp.propName())).isDefined()) {
                    throw new IllegalArgumentException(
                            "each parameter should have nonempty property name for injection strategy " + NAMED
                    );
                }
                if (iParams.find(qp -> !qp.labelGuessed() && !empty(qp.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "parameters should have no label defined for injection strategy " + INDEXED
                    );
                }
            }
        }
    }

    private void validateExtractionParams(StrategyType type, Traversable<QueryParser.QueryParam> parsedParams) {
        final Traversable<QueryParser.QueryParam> eParams =
                parsedParams.filter(qp -> qp.type() == EXTRACTION);
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
                            "broken parameter indexing returned by parser for extraction strategy " + INDEXED
                    );
                }
                // validate no directly specified labels
                if (eParams.find(qp -> !qp.labelGuessed() && !empty(qp.label())).isDefined()) {
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
                final boolean hasLabels = eParams.find(qp -> !empty(qp.label())).isDefined();
                if (hasLabels) {
                    // all should have nonempty labels
                    if (eParams.find(qp -> empty(qp.label())).isDefined()) {
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
                                "broken parameter indexing returned by parser for extraction strategy " + NAMED
                        );
                    }
                }
                // validate propNames
                if (eParams.find(qp -> empty(qp.propName()) && empty(qp.label())).isDefined()) {
                    throw new IllegalArgumentException(
                            "each parameter should have nonempty property name or label (which will be used instead)" +
                                    " for extraction strategy " + NAMED
                    );
                }
                if (eParams
                        .groupBy(qp -> empty(qp.propName()) ? qp.label() : qp.propName())
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

    private boolean empty(String s) {
        return s == null || s.isBlank();
    }

}
