package org.funcj.parser;

import org.funcj.util.Functions.F;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A parse result. Either a <code>Success</code> or a <code>Failure</code>
 * @param <I> input stream symbol type
 * @param <A> parse result type
 */
public interface Result<I, A> {
    static <I, A> Result<I, A> success(A result, Input<I> next) {
        return new Success<I, A>(result, next);
    }

    static <I, A> Result<I, A> failure(Input<I> in, SymSet<I> expected) {
        return new FailureOnExpected<I, A>(in, expected);
    }

    static <I, A> Result<I, A> failureEof(Input<I> in, SymSet<I> expected) {
        return new FailureOnExpected<I, A>(in, expected);
    }

    static <I, A> Result<I, A> failureMessage(Input<I> in, String error) {
        return new FailureMessage<I, A>(in, error);
    }

    boolean isSuccess();

    A getOrThrow();

    <B> Result<I, B> map(F<A, B> f);

    void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure);

    <B> B match(F<Success<I, A>, B> success, F<Failure<I, A>, B> failure);

    class Success<I, A> implements Result<I, A> {
        private final A value;
        private final Input<I> next;

        public Success(A value, Input<I> next) {
            this.value = value;
            this.next = next;
        }

        public A value() {
            return value;
        }

        public Input<I> next() {
            return next;
        }

        @Override
        public String toString() {
            return "Success{" +
                "value=" + value +
                ", next=" + next +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?, ?> success = (Success<?, ?>) o;
            return Objects.equals(value, success.value) &&
                    Objects.equals(next, success.next);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, next);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public A getOrThrow() {
            return value;
        }

        @Override
        public <B> Result<I, B> map(F<A, B> f) {
            return success(f.apply(value), next);
        }

        @Override
        public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
            success.accept(this);
        }

        @Override
        public <B> B match(F<Success<I, A>, B> success, F<Failure<I, A>, B> failure) {
            return success.apply(this);
        }
    }

    abstract class Failure<I, A> implements Result<I, A> {
        protected final Input<I> input;

        public Failure(Input<I> input) {
            this.input = input;
        }

        public Input<I> input() {
            return input;
        }

        @Override
        public String toString() {
            return "input=" + input;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?, ?> failure = (Failure<?, ?>) o;
            return Objects.equals(input, failure.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(input);
        }

        public <T> Failure<I, T> cast() {
            return (Failure<I, T>) this;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public <B> Result<I, B> map(F<A, B> f) {
            return this.cast();
        }

        @Override
        public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
            failure.accept(this);
        }

        @Override
        public <B> B match(F<Success<I, A>, B> success, F<Failure<I, A>, B> failure) {
            return failure.apply(this);
        }
    }

    final class FailureOnExpected<I, A> extends Failure<I, A> {
        private final SymSet<I> expected;

        public FailureOnExpected(Input<I> input, SymSet<I> expected) {
            super(input);
            this.expected = expected;
        }

        public SymSet<I> expected() {
            return expected;
        }

        @Override
        public String toString() {
            return "FailureOnExpected{" +
                    super.toString() +
                    ", expected=" + expected +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailureOnExpected<?, ?> that = (FailureOnExpected<?, ?>) o;
            if (!Objects.equals(expected, that.expected)) return false;
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), expected);
        }

        public <T> Failure<I, T> cast() {
            return (Failure<I, T>) this;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public A getOrThrow() {
            throw new RuntimeException(
                    "Failure at position " + input.position() + ", expected=" + expected
            );
        }

        @Override
        public <B> Result<I, B> map(F<A, B> f) {
            return this.cast();
        }

        @Override
        public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
            failure.accept(this);
        }

        @Override
        public <B> B match(F<Success<I, A>, B> success, F<Failure<I, A>, B> failure) {
            return failure.apply(this);
        }
    }

    class FailureMessage<I, A> extends Failure<I, A> {
        private final String error;

        public FailureMessage(Input<I> input, String error) {
            super(input);
            this.error = error;
        }

        public String expected() {
            return error;
        }

        @Override
        public String toString() {
            return "Failure{" +
                    super.toString() +
                    ", error=" + error +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FailureMessage<?, ?> that = (FailureMessage<?, ?>) o;
            if (!Objects.equals(error, that.error)) return false;
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), error);
        }

        public <T> Failure<I, T> cast() {
            return (Failure<I, T>) this;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public A getOrThrow() {
            throw new RuntimeException(
                    "Failure at position " + input.position() + ", error=" + error
            );
        }

        @Override
        public <B> Result<I, B> map(F<A, B> f) {
            return this.cast();
        }

        @Override
        public void handle(Consumer<Success<I, A>> success, Consumer<Failure<I, A>> failure) {
            failure.accept(this);
        }

        @Override
        public <B> B match(F<Success<I, A>, B> success, F<Failure<I, A>, B> failure) {
            return failure.apply(this);
        }
    }
}
