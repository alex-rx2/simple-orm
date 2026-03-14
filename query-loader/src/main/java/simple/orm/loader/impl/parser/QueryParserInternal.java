package simple.orm.loader.impl.parser;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import simple.orm.loader.QueryParser;
import simple.orm.loader.QuerySource;
import simple.orm.util.Mutable;

/**
 * Helper class performing actual SQL parsing. Stateful and not thread-safe.
 * <br>
 * Parameters information is embedded into SQL via SQL comments.
 * <br>
 * For more details see {@link ParameterParserUtil}.
 *
 * @see ParameterParserUtil
 */
public final class QueryParserInternal {

    private enum State {
        PRE_PROCESS, // ready to process
        SQL, // some SQL character to be analyzed
        ONE_LINE_COMMENT, // one line comment
        MULTI_LINE_COMMENT, // multi-line comment
        STRING_LITERAL_SQ, // single quote ' string
        STRING_LITERAL_DQ, // double quote " string
    }

    private final CharactersProvider input;
    private final CharactersConsumer output;
    private final Mutable<Seq<QueryParser.QueryParam>> params;
    private State state;

    public QueryParserInternal(QuerySource source) {
        this.input = new CharactersProvider(source.getReader());
        this.output = new CharactersConsumer();
        this.params = Mutable.of(List.empty());
        this.state = State.PRE_PROCESS;
    }

    public QueryParser.ParsedQuery extract() {
        if (state == State.PRE_PROCESS) {
            process();
        }
        return new QueryParser.ParsedQuery(output.getSQL(), number(params.get()));
    }

    private Seq<QueryParser.QueryParam> number(Seq<QueryParser.QueryParam> params) {
        final Mutable<Integer> injection = Mutable.of(1);
        final Mutable<Integer> extraction = Mutable.of(1);
        return params.map(qp -> {
            int index;
            if (qp.type() == QueryParser.ParamType.INJECTION) {
                index = injection.get();
                injection.set(index + 1);
                return reIndex(qp, index);
            } else if (qp.type() == QueryParser.ParamType.EXTRACTION) {
                index = extraction.get();
                extraction.set(index + 1);
                return reIndex(qp, index);
            } else {
                return qp;
            }
        });
    }

    private QueryParser.QueryParam reIndex(QueryParser.QueryParam param, int newIndex) {
        return new QueryParser.QueryParam(
                param.type(),
                newIndex,
                param.label(),
                param.labelGuessed(),
                param.propName(),
                param.mapperName(),
                param.tag(),
                param.jdbcTypeName(),
                param.javaClassName()
        );
    }

    private void process() {
        state = State.SQL;
        while (input.hasMore()) {
            char c = input.getChar();
            switch (state) {
                case SQL -> {
                    if (c == '-' && input.lookupChar() == '-') {
                        state = State.ONE_LINE_COMMENT;
                        transferChar('-', true);
                        transferChar('-', true);
                    } else if (c == '/' && input.lookupChar() == '*') {
                        state = State.MULTI_LINE_COMMENT;
                        transferChar('/', true);
                        transferChar('*', true);
                    } else if (c == '\'') {
                        state = State.STRING_LITERAL_SQ;
                        transferChar(c, false);
                    } else if (c == '"') {
                        state = State.STRING_LITERAL_DQ;
                        transferChar(c, false);
                    } else {
                        transferChar(c, false);
                    }
                }
                case ONE_LINE_COMMENT -> {
                    if (c == '\n') {
                        state = State.SQL;
                        consumeOneLineComment();
                        transferChar(c, false);
                    } else if (c == '\r' && input.lookupChar() == '\n') {
                        state = State.SQL;
                        consumeOneLineComment();
                        transferChar('\r', false);
                        transferChar('\n', false);
                    } else {
                        transferChar(c, true);
                    }
                }
                case MULTI_LINE_COMMENT -> {
                    if (c == '*' && input.lookupChar() == '/') {
                        state = State.SQL;
                        transferChar('*', true);
                        transferChar('/', true);
                        consumeMultiLineComment();
                    } else {
                        transferChar(c, true);
                    }
                }
                case STRING_LITERAL_SQ -> {
                    if (c == '\'') {
                        if (input.lookupChar() == '\'') {
                            transferChar(c, false);
                            input.advancePos();
                        } else {
                            state = State.SQL;
                            transferChar(c, false);
                        }
                    } else {
                        transferChar(c, false);
                    }
                }
                case STRING_LITERAL_DQ -> {
                    if (c == '"') {
                        state = State.SQL;
                        transferChar(c, false);
                    } else {
                        transferChar(c, false);
                    }
                }
            }
        }
        // if no more data but inside comment - try to consume it
        if (state==State.ONE_LINE_COMMENT) {
            consumeOneLineComment();
        }
        if (state==State.MULTI_LINE_COMMENT) {
            consumeMultiLineComment();
        }
    }

    private void transferChar(char c, boolean comment) {
        if (comment) {
            output.appendToComment(c);
        } else {
            output.appendToSQL(c);
        }
        input.advancePos();
    }

    private void consumeOneLineComment() {
        String comment = output.getComment();
        String content = comment.substring(2); // without starting --
        String lastWord = output.lastWord();
        params.apply(pp -> pp.appendAll(ParameterParserUtil.parseParameters(false, content, lastWord)));
        output.appendCommentToSQL();
    }

    private void consumeMultiLineComment() {
        String comment = output.getComment();
        String content = comment.substring(2, comment.length() - 2); // without starting /* and ending */
        String lastWord = output.lastWord();
        params.apply(pp -> pp.appendAll(ParameterParserUtil.parseParameters(true, content, lastWord)));
        output.appendCommentToSQL();
    }

}
