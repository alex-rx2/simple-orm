package simple.orm.loader.builder;

import simple.orm.loader.QueryParser;

/**
 * Parameter type.
 */
public enum ParameterType {

    INJECTION,
    EXTRACTION;

    public static ParameterType of(QueryParser.ParamType pType) {
        return pType == QueryParser.ParamType.INJECTION
                ? INJECTION
                : pType == QueryParser.ParamType.EXTRACTION
                ? EXTRACTION
                : null
                ;
    }

}
