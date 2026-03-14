package simple.orm.loader.impl.parser;

import simple.orm.loader.RuntimeIOException;

import java.io.IOException;
import java.io.Reader;

/**
 * {@link Reader} wrap that buffers it and provides only operations required for {@link QueryParserInternal}.
 */
public class CharactersProvider {

    private final Reader reader;
    private final char[] buffer = new char[1024];
    private int pos = 0; // position of next char to return; -1 = no more chars left; -2 = exception was thrown by reader
    private int bufferedChars = 0; // number of chars available in buffer

    public CharactersProvider(Reader reader) {
        this.reader = reader;
    }

    public boolean hasMore() {
        ensureBuffer();
        return pos != -1;
    }

    public char getChar() {
        ensureBuffer();
        if (pos == -1) {
            throw new IllegalStateException("no more characters left");
        }
        if (pos == -2) {
            throw new IllegalStateException("IOException has occurred earlier");
        }
        return buffer[pos];
    }

    public void advancePos() {
        if (pos >= 0) {
            pos++;
        }
    }

    public char lookupChar() {
        ensureBuffer();
        return pos < 0 ? 0 : buffer[pos + 1];
    }

    // ensures that there are chars available to return in buffer or no more chars left
    private void ensureBuffer() {
        if (pos < 0) {
            // no more chars left or error occurred
            return;
        }
        if (bufferedChars >= pos + 1) {
            // we have at least 1 more character in buffer
            return;
        }
        try {
            bufferedChars = reader.read(buffer);
            pos = bufferedChars == -1 ? -1 : 0;
        } catch (IOException e) {
            pos = -2;
            throw new RuntimeIOException(e);
        }
    }

}
