package simple.orm.jdbc.common;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.jdbc.query.NamedParametersMap;

/**
 * {@link DefaultNPProcessor} algorithm implementation. Made as separate class for ease of algorithm state maintenance.
 */
public final class DefaultNPProcessorExtractor {

    private enum State {
        SQL, // some sql character to be analyzed
        NAMED_PARAMETER, // named parameter
        ONE_LINE_COMMENT, // one line comment
        MULTI_LINE_COMMENT, // multi-line comment
        STRING_LITERAL_SQ, // single quote ' string
        STRING_LITERAL_DQ, // double quote " string
    }

    // original sql
    private final String sql;
    // inner state
    private char[] input;
    private char[] output;
    private int inIdx = 0;
    private int outIdx = 0;
    private State state;
    private Seq<StringBuilder> names;

    public DefaultNPProcessorExtractor(String sql) {
        this.sql = sql;
    }

    public Tuple2<String, NamedParametersMap> extract() {
        init();
        process();
        return Tuple.of(
                new String(output, 0, outIdx),
                new NamedParametersMap(names.map(StringBuilder::toString))
        );
    }

    private void init() {
        input = sql.toCharArray();
        output = new char[input.length];
        inIdx = 0;
        outIdx = 0;
        state = State.SQL;
        names = List.empty();
    }

    private void process() {
        while (inIdx < input.length) {
            char c = input[inIdx];
            switch (state) {
                case SQL -> {
                    if (c == ':' && isNameChar(next())) {
                        state = State.NAMED_PARAMETER;
                        names = names.append(new StringBuilder(32));
                        transferChar('?'); // replace with ? in output
                    } else if (c == '-' && next() == '-') {
                        state = State.ONE_LINE_COMMENT;
                        transferChar('-');
                        transferChar('-');
                    } else if (c == '/' && next() == '*') {
                        state = State.MULTI_LINE_COMMENT;
                        transferChar('/');
                        transferChar('*');
                    } else if (c == '\'') {
                        state = State.STRING_LITERAL_SQ;
                        transferChar(c);
                    } else if (c == '"') {
                        state = State.STRING_LITERAL_DQ;
                        transferChar(c);
                    } else {
                        transferChar(c);
                    }
                }
                case NAMED_PARAMETER -> {
                    if (isNameChar(c)) {
                        names.last().append(c);
                        inIdx++;
                    } else {
                        state = State.SQL;
                        // don't move indexes to process char again
                    }
                }
                case ONE_LINE_COMMENT -> {
                    if (c == '\n') {
                        state = State.SQL;
                        transferChar(c);
                    } else {
                        transferChar(c);
                    }
                }
                case MULTI_LINE_COMMENT -> {
                    if (c == '*' && next() == '/') {
                        state = State.SQL;
                        transferChar('*');
                        transferChar('/');
                    } else {
                        transferChar(c);
                    }
                }
                case STRING_LITERAL_SQ -> {
                    if (c == '\'') {
                        if (next() == '\'') {
                            transferChar('\'');
                            transferChar('\'');
                        } else {
                            state = State.SQL;
                            transferChar(c);
                        }
                    } else {
                        transferChar(c);
                    }
                }
                case STRING_LITERAL_DQ -> {
                    if (c == '"') {
                        state = State.SQL;
                        transferChar(c);
                    } else {
                        transferChar(c);
                    }
                }
            }
        }
    }

    /**
     * Preview next character.
     */
    private char next() {
        return inIdx >= input.length - 1 ? 0 : input[inIdx + 1];
    }

    /**
     * Transfers specified character into output and increment indexes.
     */
    private void transferChar(char c) {
        output[outIdx] = c;
        inIdx++;
        outIdx++;
    }

    /**
     * Check if character matches named parameter name pattern.
     */
    private boolean isNameChar(char c) {
        if (c >= 'a' && c <= 'z') return true;
        if (c >= 'A' && c <= 'Z') return true;
        if (c >= '0' && c <= '9') return true;
        if (c == '_') return true;
        return false;
    }
}
