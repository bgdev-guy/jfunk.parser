package org.typemeta.funcj.codec.jsons;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class JsonIO {
    interface Input  {
        interface Event {
            Type type();

            enum Type implements Event {
                ARRAY_END,
                ARRAY_START,
                COMMA,          // internal use only
                COLON,          // internal use only
                EOF,
                FALSE,
                FIELD_NAME,
                NULL,
                NUMBER,
                OBJECT_END,
                OBJECT_START,
                STRING,
                TRUE;

                @Override
                public Type type() {
                    return this;
                }
            }

            final class FieldName implements Event {
                public final String value;

                public FieldName(String value) {
                    this.value = Objects.requireNonNull(value);
                }

                @Override
                public Type type() {
                    return Type.FIELD_NAME;
                }

                @Override
                public String toString() {
                    return "FieldName{" + value + "}";
                }

                @Override
                public boolean equals(Object rhs) {
                    if (this == rhs) {
                        return true;
                    } else if (rhs == null || getClass() != rhs.getClass()) {
                        return false;
                    } else {
                        FieldName rhsJS = (FieldName) rhs;
                        return value.equals(rhsJS.value);
                    }
                }

                @Override
                public int hashCode() {
                    return value.hashCode();
                }
            }

            final class JString implements Event {
                public final String value;

                public JString(String value) {
                    this.value = Objects.requireNonNull(value);
                }

                @Override
                public Type type() {
                    return Type.STRING;
                }

                @Override
                public String toString() {
                    return "JString{" + value + "}";
                }

                @Override
                public boolean equals(Object rhs) {
                    if (this == rhs) {
                        return true;
                    } else if (rhs == null || getClass() != rhs.getClass()) {
                        return false;
                    } else {
                        final JString rhsJS = (JString) rhs;
                        return value.equals(rhsJS.value);
                    }
                }

                @Override
                public int hashCode() {
                    return value.hashCode();
                }
            }

            final class JNumber implements Event {
                public final String value;

                public JNumber(String value) {
                    this.value = Objects.requireNonNull(value);
                }

                @Override
                public Type type() {
                    return Type.NUMBER;
                }

                @Override
                public String toString() {
                    return "JNumber{" + value + "}";
                }

                @Override
                public boolean equals(Object rhs) {
                    if (this == rhs) {
                        return true;
                    } else if (rhs == null || getClass() != rhs.getClass()) {
                        return false;
                    } else {
                        final JNumber rhsJN = (JNumber) rhs;
                        return value.equals(rhsJN.value);
                    }
                }

                @Override
                public int hashCode() {
                    return value.hashCode();
                }
            }
        }


        boolean notEOF();

        Event.Type currentEventType();

        Event event(int lookahead);

        <T> T readNull();

        boolean readBool();

        String readStr();
        char readChar();

        byte readByte();
        short readShort();
        int readInt();
        long readLong();
        float readFloat();
        double readDbl();
        BigDecimal readBigDec();
        Number readNumber();

        void startObject();
        String readFieldName();
        void endObject();

        void startArray();
        void endArray();
    }

    interface Output {

        Output writeNull();

        Output writeBool(boolean value);

        Output writeStr(String value);
        Output writeChar(char value);

        Output writeNum(byte value);
        Output writeNum(short value);
        Output writeNum(int value);
        Output writeNum(long value);
        Output writeNum(float value);
        Output writeNum(double value);
        Output writeNum(Number value);
        Output writeNum(BigDecimal value);
        Output writeNum(String value);

        Output startObject();
        Output writeField(String name);
        Output endObject();

        Output startArray();
        Output endArray();

        void close();
    }
}
