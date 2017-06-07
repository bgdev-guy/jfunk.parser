package org.funcj.codec;

/**
 * A <code>Codec</code> encapsulates the logic for encoding a value of type <code>T</code>
 * into a value of type <code>E</code> and vice versa.
 * @param <T> type to be encoded/decoded
 * @param <E> encoded type
 */
public interface Codec<T, E> {

    /**
     * Codec for null values.
     * @param <E> encoded type
     */
    interface NullCodec<E> extends Codec<Object, E> {
        /**
         * Check whether and encoded value represents a null value.
         * @param in encoded value
         * @return true if encoded value represents a null value
         */
        boolean isNull(E in);
    }

    /**
     * Codec for {@link java.lang.Boolean} and <code>boolean</code> values.
     * @param <E> encoded type
     */
    abstract class BooleanCodec<E> implements Codec<Boolean, E> {

        @Override
        public E encode(Boolean val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Boolean decode(Class<Boolean> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Boolean decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(boolean val, E out);

        public abstract boolean decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Byte} and <code>byte</code> values.
     * @param <E> encoded type
     */
    abstract class ByteCodec<E> implements Codec<Byte, E> {

        @Override
        public E encode(Byte val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Byte decode(Class<Byte> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Byte decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(byte val, E out);

        public abstract byte decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Character} and <code>char</code> values.
     * @param <E> encoded type
     */
    abstract class CharCodec<E> implements Codec<Character, E> {

        @Override
        public E encode(Character val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Character decode(Class<Character> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Character decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(char val, E out);

        public abstract char decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Short} and <code>short</code> values.
     * @param <E> encoded type
     */
    abstract class ShortCodec<E> implements Codec<Short, E> {

        @Override
        public E encode(Short val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Short decode(Class<Short> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Short decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(short val, E out);

        public abstract short decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Integer} and <code>int</code> values.
     * @param <E> encoded type
     */
    abstract class IntCodec<E> implements Codec<Integer, E> {

        @Override
        public E encode(Integer val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Integer decode(Class<Integer> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Integer decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(int val, E out);

        public abstract int decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Long} and <code>long</code> values.
     * @param <E> encoded type
     */
    abstract class LongCodec<E> implements Codec<Long, E> {

        @Override
        public E encode(Long val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Long decode(Class<Long> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Long decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(long val, E out);

        public abstract long decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Float} and <code>float</code> values.
     * @param <E> encoded type
     */
    abstract class FloatCodec<E> implements Codec<Float, E> {

        @Override
        public E encode(Float val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Float decode(Class<Float> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Float decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(float val, E out);

        public abstract float decodePrim(E in);
    }

    /**
     * Codec for {@link java.lang.Double} and <code>double</code> values.
     * @param <E> encoded type
     */
    abstract class DoubleCodec<E> implements Codec<Double, E> {

        @Override
        public E encode(Double val, E out) {
            return encodePrim(val, out) ;
        }

        @Override
        public Double decode(Class<Double> dynType, E in) {
            return decodePrim(in);
        }

        @Override
        public Double decode(E in) {
            return decodePrim(in);
        }

        public abstract E encodePrim(double val, E out);

        public abstract double decodePrim(E in);
    }

    /**
     * Encode a value of type <code>T</code> into and encoded value of type <code>E</code>.
     * One of the two <code>encode</code> methods must be implemented by implementing
     * classes.
     * @param val unencoded value
     * @param out encoded parent value
     * @return encoded value
     */
    default E encode(T val, E out) {
        return encode(val);
    }

    /**
     * Encode a value of type <code>T</code> into and encoded value of type <code>E</code>.
     * One of the two <code>encode</code> methods must be implemented by implementing
     * classes.
     * @param val unencoded value
     * @return encoded value
     */
    default E encode(T val) {
        throw new CodecException("Operation not implemented");
    }

    /**
     * Decode a value of type <code>E</code> back into a value of type <code>T</code> .
     * One of the two <code>decode</code> methods must be implemented by implementing
     * classes.
     * @param dynType the dynamic type to decode into.
     * @param in the encoded value
     * @return unencoded value
     */
    default T decode(Class<T> dynType, E in) {
        return decode(in);
    }

    /**
     * Decode a value of type <code>E</code> back into a value of type <code>T</code> .
     * One of the two <code>decode</code> methods must be implemented by implementing
     * classes.
     * @param in the encoded value
     * @return unencoded value
     */
    default T decode(E in) {
        throw new CodecException("Operation not implemented");
    }

}
