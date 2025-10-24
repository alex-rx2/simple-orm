package simple.orm.jdbc;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import simple.orm.jdbc.exc.JdbcException;
import simple.orm.jdbc.map.out.IndexedExtractor;
import simple.orm.jdbc.map.out.NamedExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link Result} implementation.
 */
public class ResultImpl<T> implements Result<T> {

    public static ResultImpl<Seq<Object>> indexed(ResultSet rs, IndexedExtractor extractor) {
        return new ResultImpl<>(rs, Either.left(extractor));
    }

    public static <T> ResultImpl<T> named(ResultSet rs, NamedExtractor<T> extractor) {
        return new ResultImpl<>(rs, Either.right(extractor));
    }

    private enum State {
        THE_START, NEXT_READY, NEXT_DONE, THE_END
    }

    private final ResultSet resultSet;
    private final Either<IndexedExtractor, NamedExtractor<T>> extractor;

    private boolean shouldAutoClose = true;
    private State state = State.THE_START;

    private ResultImpl(ResultSet resultSet, Either<IndexedExtractor, NamedExtractor<T>> extractor) {
        this.resultSet = resultSet;
        this.extractor = extractor;
    }

    @Override
    public boolean shouldBeClosedAutomatically() {
        return shouldAutoClose;
    }

    @Override
    public void setShouldBeClosedAutomatically(boolean shouldBeClosedAutomatically) {
        this.shouldAutoClose = shouldBeClosedAutomatically;
    }

    @Override
    public boolean isClosed() {
        try {
            return resultSet.isClosed();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public void close() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public boolean hasNextRow() {
        switch (state) {
            case THE_START:
            case NEXT_DONE:
                try {
                    boolean next = resultSet.next();
                    state = next ? State.NEXT_READY : State.THE_END;
                    return hasNextRow();
                } catch (SQLException e) {
                    throw new JdbcException(e);
                }
            case NEXT_READY:
                return true;
            case THE_END:
                if (shouldAutoClose) {
                    close();
                }
                return false;
            default:
                throw new IllegalStateException("unreachable");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T nextRow() {
        switch (state) {
            case THE_START:
            case NEXT_DONE:
                hasNextRow();
                return nextRow();
            case NEXT_READY:
                final T row;
                if (extractor.isLeft()) {
                    row = (T) extractor.getLeft().extractRow(resultSet);
                } else {
                    row = extractor.get().extractRow(resultSet);
                }
                state = State.NEXT_DONE;
                return row;
            case THE_END:
                if (shouldAutoClose) {
                    close();
                }
                throw new IllegalStateException("no next row");
            default:
                throw new IllegalStateException("unreachable");
        }
    }

    @Override
    public Seq<T> extractAll() {
        if (state != State.THE_START) {
            throw new IllegalStateException("hasNextRow was already called");
        }
        List<T> res = List.empty();
        while (hasNextRow()) {
            res = res.append(nextRow());
        }
        if (shouldAutoClose) {
            close();
        }
        return res;
    }

    @Override
    public T exactlySingleRow() {
        if (state != State.THE_START) {
            throw new IllegalStateException("hasNextRow was already called");
        }
        final T res = hasNextRow() ? nextRow() : null;
        if (hasNextRow()) {
            throw new IllegalStateException("has more than one row");
        }
        if (shouldAutoClose) {
            close();
        }
        return res;
    }

}
