package org.funcj.parser;

import org.funcj.data.*;
import org.funcj.util.Functions;

import java.util.Optional;

import static org.funcj.parser.Parser.pure;
import static org.funcj.parser.Utils.*;

/**
 * Combinators provides functions for combining parsers to form new parsers.
 */
public abstract class Combinators {

    /**
     * A parser that always fails.
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return a parser that always fails.
     */
    public static <I, A> Parser<I, A> fail() {
        return new ParserImpl<I, A>(LTRUE, SymSet::empty) {
            @Override
            public Result<I, A> parse(Input<I> in, SymSet<I> follow) {
                return failure(this, in);
            }
        };
    }

    /**
     * A parser that succeeds iff we are at the end of the input.
     * @param <I> input stream symbol type
     * @return a parser that succeeds iff we are at the end of the input.
     */
    public static <I> Parser<I, Unit> eof() {
        return new ParserImpl<I, Unit>(LTRUE, SymSet::empty) {
            @Override
            public Result<I, Unit> parse(Input<I> in, SymSet<I> follow) {
                return in.isEof() ?
                        Result.success(Unit.UNIT, in) :
                        failure(this, in);
            }
        };
    }

    /**
     * A parser that succeeds if the next input symbol satisfies the supplied predicate.
     * @param name a name for the parser (used for error messages)
     * @param pred predicate to be applied to the next input
     * @param <I> input stream symbol type
     * @return a parser that succeeds if the next input symbol satisfies the supplied predicate.
     */
    public static <I> Parser<I, I> satisfy(String name, Functions.Predicate<I> pred) {
        return new ParserImpl<I, I>(LFALSE, () -> SymSet.pred(name, pred)) {
            @Override
            public Result<I, I> parse(Input<I> in, SymSet<I> follow) {
                return Result.success(in.get(), in.next());
            }
        };
    }

    /**
     * A parser that succeeds if the next inout symbol equals the supplied {@code value},
     * and returns the value.
     * @param val value returned by the parser
     * @param <I> input stream symbol type
     * @return A parser that succeeds if the next inout symbol equals the supplied {@code value}
     */
    public static <I> Parser<I, I> value(I val) {
        return value(val, val);
    }

    /**
     * A parser that succeeds if the next inout symbol equals the supplied {@code value},
     * and returns the supplied {@code res} value.
     * @param val value returned by the parser
     * @param <I> input stream symbol type
     * @return A parser that succeeds if the next inout symbol equals the supplied {@code value}
     */
    public static <I, A> Parser<I, A> value(I val, A res) {
        return new ParserImpl<I, A>(LFALSE, () -> SymSet.value(val)) {
            @Override
            public Result<I, A> parse(Input<I> in, SymSet<I> follow) {
                return Result.success(res, in.next());
            }
        };
    }

    /**
     * A parser that succeeds on any input symbol, and that returns that symbol.
     * @param <I> input stream symbol type
     * @return a parser that succeeds on any input symbol
     */
    public static <I> Parser<I, I> any() {
        return new ParserImpl<I, I>(LTRUE, SymSet::all) {
            @Override
            public Result<I, I> parse(Input<I> in, SymSet<I> follow) {
                return in.isEof() ?
                        failureEof(this, in) :
                        Result.success(in.get(), in.next());
            }
        };
    }

    /**
     * A parser which applies {@code p} zero or more times until it fails,
     * and then returns an {@link org.funcj.data.IList} of the results.
     * Note, if {@code p} fails on the first attempt then this parser succeeds,
     * with an empty list of results.
     * @param p parser to be applied repeatedly
     * @param <I> input stream symbol type
     * @param <A> parse result type
     * @return a parser which applies {@code p} zero or more times until it fails
     */
    public static <I, A>
    Parser<I, IList<A>> many(Parser<I, A> p) {
        return new ParserImpl<I, IList<A>>(LTRUE, p.firstSet()) {
            @Override
            public Result<I, IList<A>> parse(Input<I> in, SymSet<I> follow) {
                IList<A> acc = IList.of();
                final SymSet<I> follow2 = follow.union(p.firstSet().apply());
                while (true) {
                    if (!in.isEof()) {
                        final I i = in.get();
                        if (firstSet().apply().matches(i)) {
                            final Result<I, A> r = p.parse(in, follow2);
                            if (r.isSuccess()) {
                                final Result.Success<I, A> succ = (Result.Success<I, A>) r;
                                acc = acc.add(succ.value());
                                in = succ.next();
                                continue;
                            } else {
                                return ((Result.Failure<I, A>)r).cast();
                            }
                        }
                    }
                    return Result.success(acc.reverse(), in);
                }
            }
        };
    }

    /**
     * A parser which applies {@code p} one or more times until it fails,
     * and then returns an {@link IList} of the results.
     * Note, if {@code p} fails on the first attempt then this parser fails.
     * @param p parser to be applied repeatedly
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return a parser which applies {@code p} repeatedly until it fails
     */
    public static <I, A>
    Parser<I, IList.NonEmpty<A>> many1(Parser<I, A> p) {
        return p.and(many(p))
            .map(a -> l -> l.add(a));
    }

    /**
     * A parser which applies {@code p} zero or more times until it fails,
     * and throws away the results.
     * Note, if {@code p} fails on the first attempt then this parser succeeds.
     * @param p parser to be applied repeatedly
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return a parser which applies {@code p} repeatedly until it fails
     */
    public static <I, A>
    Parser<I, Unit> skipMany(Parser<I, A> p) {
        return many(p).map(u -> Unit.UNIT);
    }

    /**
     * A parser which applies {@code p} zero or more times until it fails,
     * alternating with calls to the {@code sep} parser.
     * The results of the {@code p} parser are collected in a {@link IList}
     * and returned by the parser.
     * @param p symbol parser
     * @param sep separator parser
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @param <SEP> separator type
     * @return a parser which applies {@code p} zero or more times alternated with {@code sep}
     */
    public static <I, A, SEP>
    Parser<I, IList<A>> sepBy(Parser<I, A> p, Parser<I, SEP> sep) {
        return sepBy1(p, sep).or(pure(IList.nil()));
    }

    /**
     * A parser which applies {@code p} one or more times until it fails,
     * alternating with calls to the {@code sep} parser.
     * The results of the {@code p} parser are collected in a {@link IList}
     * and returned by the parser.
     * @param p symbol parser
     * @param sep separator parser
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @param <SEP> separator type
     * @return a parser which applies {@code p} one or more times alternated with {@code sep}
     */
    public static <I, A, SEP>
    Parser<I, IList<A>> sepBy1(Parser<I, A> p, Parser<I, SEP> sep) {
        return p.and(many(sep.andR(p)))
            .map(a -> l -> l.add(a));
    }

    /**
     * A parser that applies the {@code p} parser, and, if it succeeds,
     * returns the result wrapped in an {@link Optional},
     * otherwise returns an empty {@code Optional}.
     * @param p symbol parser
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return an optional parser
     */
    public static <I, A>
    Parser<I, Optional<A>> optional(Parser<I, A> p) {
        return p.map(Optional::of).or(pure(Optional.empty()));
    }

    /**
     * A parser which applies the {@code open} parser, then the {@code p} parser,
     * and then {@code close} parser.
     * If all three succeed then the result of the {@code p} parser is returned.
     * @param open open symbol parser
     * @param close close symbol parser
     * @param p enclosed symbol parser
     * @param <I> input stream symbol type
     * @param <A> enclosed parser result type
     * @param <OPEN> open parser result type
     * @param <CLOSE> close parser result type
     * @return
     */
    public static <I, A, OPEN, CLOSE>
    Parser<I, A> between(
            Parser<I, OPEN> open,
            Parser<I, CLOSE> close,
            Parser<I, A> p) {
        return open.andR(p).andL(close);
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     * @param ps var-arg list of parsers
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(Parser<I, A>... ps) {
        return choice(IList.ofArray(ps));
    }

    /**
     * A parser that attempts one or more parsers in turn and returns the result
     * of the first that succeeds, or else fails.
     * @param ps list of parsers
     * @param <I> input stream symbol type
     * @param <A> parser result type
     * @return a parser that attempts one or more parsers in turn
     */
    public static <I, A>
    Parser<I, A> choice(IList<Parser<I, A>> ps) {
        return ps.foldLeft1(Parser::or);
    }
}