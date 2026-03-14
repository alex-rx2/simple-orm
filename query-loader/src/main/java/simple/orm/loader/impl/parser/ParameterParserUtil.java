package simple.orm.loader.impl.parser;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Traversable;
import simple.orm.loader.QueryParser;
import simple.orm.util.Mutable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Parameter info is placed in SQL either immediately after parameter
 * (<code>?</code> for injection, label or column name in SELECT clause for extraction)
 * or in separate comments (with slightly different format) anywhere in the query.
 *
 * <br><br>
 * Immediate injection parameter comment should be formatted as follows:
 * <nobr><code>?:property_name:mapper_name(tag):JDBC_type_name:Java_class_name</code></nobr>.
 *
 * <br><br>
 * Immediate extraction parameter comment should be formatted as follows:
 * <nobr><code>/:property_name:mapper_name(tag):JDBC_type_name:Java_class_name</code></nobr>.
 * <br>
 * The word preceding the comment will be used as a label for extraction if preceding word matches label pattern.
 * No in-depth analysis is performed.
 *
 * <br><br>
 * Separate comment is considered an injection parameter if it is formatted as follows:
 * <nobr><code>??:property_name:mapper_name(tag):JDBC_type_name:Java_class_name</code></nobr>.
 *
 * <br><br>
 * Separate comment is considered an extraction parameter if it is formatted as follows:
 * <nobr><code>//label:property_name:mapper_name(tag):JDBC_type_name:Java_class_name</code></nobr>.
 *
 * <br><br>
 * <code>:JDBC_type_name:Java_class_name</code> part of parameter may be omitted.
 * <br>
 * <code>:property_name:mapper_name(tag)</code> must present even if both names are empty.
 *
 * <br><br>
 * For multiline comment each line must be either empty, either match parameter format to be considered a list of parameters.
 */
public class ParameterParserUtil {

    private static final String NAME_PART = "[a-zA-Z][a-zA-Z0-9_]*";
    private static final String PROP_COMMON_PART =
            //@formatter:off
            // property name
            ":(" + NAME_PART + "(\\." + NAME_PART + ")*)?" +
            // mapper name with optional tag
            ":(" + NAME_PART + "(\\(" + NAME_PART + "\\))?)?" +
            "(" + // <- next 2 may be omitted
            // JDBC type name
            ":(" + NAME_PART +")?"+
            // Java class name
            "(:(" + NAME_PART + "(\\." + NAME_PART + ")*)?)?" +
            ")?" // <- last 2 may be omitted
            //@formatter:on
            ;

    private static final String PROP_INJECT_IMMEDIATE = "\\?" + PROP_COMMON_PART;
    private static final String PROP_EXTRACT_IMMEDIATE = "/" + PROP_COMMON_PART;
    private static final String PROP_INJECT_ANYWHERE = "\\?\\?" + PROP_COMMON_PART;
    private static final String PROP_EXTRACT_ANYWHERE = "//(" + NAME_PART + ")*" + PROP_COMMON_PART;

    private static final Pattern PATTERN_NAME = Pattern.compile(NAME_PART);
    private static final Pattern PATTERN_SINGLE_LINE = Pattern.compile(
            "(\\?{1,2}|/{1,2})" + PROP_COMMON_PART + "\\s*"
    );
    private static final Pattern PATTERN_MULTILINE = Pattern.compile(
            //@formatter:off
            "\\s*((" + PROP_INJECT_ANYWHERE + ")|(" + PROP_EXTRACT_ANYWHERE + "))" +
            "(\\s*\\n\\s*((" + PROP_INJECT_ANYWHERE + ")|(" + PROP_EXTRACT_ANYWHERE + ")))*" +
            "\\s*"
            //@formatter:on
    );

    public static Traversable<QueryParser.QueryParam> parseParameters(boolean multiline, String content, String lastWord) {
        final boolean single = PATTERN_SINGLE_LINE.matcher(content).matches();
        final boolean multi = !single && PATTERN_MULTILINE.matcher(content).matches();
        if (!single && !multi) {
            return List.empty();
        }
        final List<String> params = multiline ?
                List.ofAll(content.lines()).map(String::strip).filter(s -> !s.isEmpty()) :
                List.of(content.stripTrailing());
        return params
                .map(p -> extract(p, single, lastWord))
                .filter(Objects::nonNull);
    }

    private static QueryParser.QueryParam extract(String parameter, boolean allowImmediate, String lastWord) {
        if (parameter.startsWith("??")) {
            // injection anywhere - simplest case
            return extractParts(QueryParser.ParamType.INJECTION, parameter.substring(2), false);
        }
        if (parameter.startsWith("?") && allowImmediate) {
            // immediate injection - must follow "?" (or "?,")
            if (lastWord == null
                    || (!lastWord.endsWith("?") && !lastWord.endsWith("?,"))) {
                return null;
            }
            return extractParts(QueryParser.ParamType.INJECTION, parameter.substring(1), false);
        }
        if (parameter.startsWith("//")) {
            // extraction anywhere - same simplest case
            return extractParts(QueryParser.ParamType.EXTRACTION, parameter.substring(2), false);
        }
        if (parameter.startsWith("/") && allowImmediate) {
            // immediate extraction - if follows "label" or "label,"
            // then take this label and prepend for following extraction of parts
            String label;
            if (lastWord.endsWith(",")) {
                lastWord = lastWord.substring(0, lastWord.length() - 1);
            }
            if (PATTERN_NAME.matcher(lastWord).matches()) {
                label = lastWord;
            } else {
                label = "";
            }
            return extractParts(QueryParser.ParamType.EXTRACTION, label + parameter.substring(1), true);
        }
        // should be very-very unreachable
        throw new IllegalStateException("should be unreachable");
    }

    private static QueryParser.QueryParam extractParts(QueryParser.ParamType type,
                                                       String parameterParts,
                                                       boolean labelGuessed) {
        Seq<String> parts = splitParts(parameterParts);
        return new QueryParser.QueryParam(
                type,
                -1, // will be renumbered after all parameters are parsed
                parts.get(0),
                parts.get(0) != null && labelGuessed,
                parts.get(1),
                parts.get(2),
                parts.get(3),
                parts.get(4),
                parts.get(5)
        );
    }

    private static Seq<String> splitParts(String parameterParts) {
        Mutable<Seq<String>> parts = Mutable.of(List.empty());
        int startNext = 0;
        int len = parameterParts.length();
        for (int i = 0; i <= len; i++) {
            // virtually add one more char ':' at the end of the string to cut last part within this for-cycle
            char c = i == len ? ':' : parameterParts.charAt(i);
            if (c == ':') {
                String part = parameterParts.substring(startNext, i);
                if (parts.get().size() == 2) {
                    // split mapper_name(tag) into mapper_name and tag
                    int j = part.indexOf('(');
                    if (j != -1) {
                        String mapperName = part.substring(0, j);
                        String mapperTag = part.substring(j + 1, part.length() - 1);
                        parts.apply(pp -> pp.append(mapperName).append(mapperTag));
                    } else {
                        parts.apply(pp -> pp.append(part).append("")); // no tag
                    }
                } else {
                    parts.apply(pp -> pp.append(part));
                }
                startNext = i + 1;
            }
        }
        while (parts.get().size() < 6) {
            parts.apply(pp -> pp.append(""));
        }
        return parts.get().map(ParameterParserUtil::nullify);
    }

    private static String nullify(String s) {
        return s.isEmpty() ? null : s;
    }

}
