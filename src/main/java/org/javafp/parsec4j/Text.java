package org.javafp.parsec4j;

import org.javafp.util.Chr;

import static org.javafp.parsec4j.Parser.*;

public class Text {
    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> anyChar() {
        return any();
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> chr(char c) {
        return value(new Chr(c));
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> alpha() {
        return satisfy(Chr::isLetter);
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> digit() {
        return satisfy(Chr::isDigit);
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> alphaNum() {
        return satisfy(Chr::isLetterOrDigit);
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Chr> ws() {
        return satisfy(Chr::isWhitespace);
    }

    private static int digitToInt(char c) {
        return Chr.getNumericValue(c);
    }

    private static int digitToInt(Chr c) {
        return Chr.getNumericValue(c.charValue());
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Integer> uintr() {
        return many1(Text.<CTX>digit().map(Text::digitToInt))
            .map(l -> l.foldl1((x, acc) -> x + acc * 10));
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Integer> intr() {
        return Text.<CTX>chr('+').or(chr('-')).or(pure(Chr.valueOf('+')))
            .and(uintr())
            .map((sign, i) -> sign.charValue() == '+' ? i : -i);
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Double> floating() {
        return many1(Text.<CTX>digit().map(Text::digitToInt))
            .map(l -> l.foldr((d, acc) -> d + acc / 10.0, 0.0) / 10.0);
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, Double> dble() {
        return Text.<CTX>intr().and(optional(Text.<CTX>chr('.').andR(floating())))
            .map((i, f) -> i.doubleValue() + f.orElse(0.0));
    }

    public static <CTX extends Parser.Context<Chr>>
    Parser<Chr, CTX, String> string(String s) {
        switch (s.length()) {
            case 0: return fail();
            case 1: return Text.<CTX>chr(s.charAt(0)).map(Object::toString);
            default: {
                return new ParserImpl<Chr, CTX, String>() {
                    @Override
                    public boolean acceptsEmpty() {
                        return false;
                    }

                    @Override
                    public SymSet<Chr> firstSetCalc() {
                        return SymSet.value(Chr.valueOf(s.charAt(0)));
                    }

                    @Override
                    public Result<Chr, String> parse(CTX ctx, int pos) {
                        int pos2 = pos;
                        for (int i = 0; i < s.length(); ++i) {
                            if (ctx.input().isEof(pos2) || ctx.input().at(pos2).charValue() != s.charAt(i)) {
                                return Result.failure(pos);
                            } else {
                                pos2 = pos2+1;
                            }
                        }

                        return Result.success(s, pos2);
                    }
                };
            }
        }
    }
}
