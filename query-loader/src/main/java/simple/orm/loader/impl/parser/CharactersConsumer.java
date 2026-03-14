package simple.orm.loader.impl.parser;

/**
 * A {@link CharactersProvider} counterpart that provides only operations required for {@link QueryParserInternal}.
 * <br>
 * This class accumulates processed SQL query.
 */
public class CharactersConsumer {

    private final StringBuilder sqlChars = new StringBuilder(1024);
    private final StringBuilder commentChars = new StringBuilder(128);

    public String getSQL() {
        return sqlChars.toString();
    }

    public String getComment() {
        return commentChars.toString();
    }

    public void appendToSQL(char c) {
        sqlChars.append(c);
    }

    public void appendToComment(char c) {
        commentChars.append(c);
    }

    public void appendCommentToSQL() {
        sqlChars.append(commentChars);
        commentChars.setLength(0);
    }

    public String lastWord() {
        int i = sqlChars.length() - 1;
        while (i >= 0 && Character.isWhitespace(sqlChars.charAt(i))) {
            i--;
        }
        int toIdx = i + 1;
        while (i >= 0 && !Character.isWhitespace(sqlChars.charAt(i))) {
            i--;
        }
        int fromIdx = i + 1;
        return toIdx > fromIdx ? sqlChars.substring(fromIdx, toIdx) : null;
    }

}
