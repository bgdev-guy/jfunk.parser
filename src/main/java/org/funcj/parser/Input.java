package org.funcj.parser;

import org.funcj.control.Exceptions;
import org.funcj.data.Chr;

import java.io.Reader;

/**
 * An abstraction for a position in a stream of input symbols,
 * that {link Parser}s operate on.
 * @param <I> input stream symbol type
 */
public interface Input<I> {
    /**
     * Construct an <code>Input</code> from a <code>char</code> array.
     * @param data input data
     * @return parseable input stream
     */
    static Input<Chr> of(char[] data) {
        return new StringInput(data);
    }

    /**
     * Construct an <code>Input</code> from a {link java.lang.String}.
     * @param s input data
     * @return parseable input stream
     */
    static Input<Chr> of(String s) {
        return new StringInput(s.toCharArray());
    }

    /**
     * Construct an <code>Input</code> from a {link java.io.Reader}.
     * @param rdr input data
     * @return parseable input stream
     */
    static Input<Chr> of(Reader rdr) {
        return new ReaderInput(rdr);
    }

    /**
     * Have we reached the end of the input stream?
     * @return true if at the end of the input
     */
    boolean isEof();

    /**
     * Get the current symbol from the stream.
     * Will throw if <code>isEof</code> is true.
     * @return next symbol
     */
    I get();

    /**
     * Get the next position in the input stream.
     * Will throw if <code>isEof</code> is true.
     * @return the next position in the input stream
     */
    Input<I> next();

    /**
     * Return a implementation-specific representation of the
     * current position (e.g. an Integer).
     * @return current position
     */
    Object position();
}

class StringInput implements Input<Chr> {

    private final char[] data;
    private int position;
    private final StringInput other;

    StringInput(char[] data) {
        this.data = data;
        this.position = 0;
        this.other = new StringInput(this, data);
    }

    StringInput(StringInput other, char[] data) {
        this.data = data;
        this.position = 0;
        this.other = other;
    }

    private StringInput setPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public String toString() {
        return "StringInput{" + position + ",data=\"" + data[position] + "\"";
    }

    @Override
    public boolean isEof() {
        return position >= data.length;
    }

    @Override
    public Chr get() {
        return Chr.valueOf(data[position]);
    }

    @Override
    public Input<Chr> next() {
        return other.setPosition(position + 1);
    }

    @Override
    public Object position() {
        return position;
    }
}

class ReaderInput implements Input<Chr> {

    protected int position;
    protected final Reader reader;
    protected Chr current;
    protected boolean isEof = false;

    protected final ReaderInput other;

    ReaderInput(Reader reader) {
        this.position = 0;
        this.reader = reader;
        this.current = null;
        this.other = new ReaderInput(this, reader);
    }

    ReaderInput(ReaderInput other, Reader reader) {
        this.position = -1;
        this.reader = reader;
        this.current = null;
        this.other = other;
    }

    protected ReaderInput setPosition(int position) {
        this.position = position;
        return this;
    }

    @Override
    public String toString() {
        return "ReaderInput{current=\"" + current + "\",isEof=" + isEof + "}";
    }

    @Override
    public boolean isEof() {
        if (!isEof && current == null) {
            Exceptions.wrap(() -> {
                final int ni = reader.read();
                if (ni == -1) {
                    current = null;
                    isEof = true;
                } else {
                    current = Chr.valueOf(ni);
                }
            });
        }
        return isEof;
    }

    @Override
    public Chr get() {
        if (isEof()) {
            throw new RuntimeException("End of input");
        } else {
            return current;
        }
    }

    @Override
    public Input<Chr> next() {
        current = null;
        return other.setPosition(position + 1);
    }

    @Override
    public Object position() {
        return position;
    }
}
