package org.typemeta.funcj.codec2.json;

import org.typemeta.funcj.codec2.core.*;
import org.typemeta.funcj.codec2.core.PrimitiveCodecs.BooleanCodec;
import org.typemeta.funcj.codec2.core.PrimitiveCodecs.IntegerCodec;
import org.typemeta.funcj.codec2.core.fields.FieldCodec;
import org.typemeta.funcj.codec2.json.JsonTypes.InStream;
import org.typemeta.funcj.codec2.json.JsonTypes.OutStream;
import org.typemeta.funcj.json.parser.JsonEvent;

import java.util.Arrays;
import java.util.Map;

public class JsonCodecFormat implements StreamCodecFormat<InStream, OutStream> {
    private static final JsonCodecFormat INSTANCE = new JsonCodecFormat();

    public static JsonCodecFormat instance () {
        return INSTANCE;
    }

    private final NullCodec<InStream, OutStream> NULL_CODEC = new NullCodec<InStream, OutStream>() {
        @Override
        public <T> EncodeResult<OutStream> encode(EncoderCore<OutStream> core, Context ctx, T val, OutStream os) {
            if (val == null) {
                return new EncodeResult<>(true, os.writeNull());
            } else {
                return new EncodeResult<>(false, os);
            }
        }

        @Override
        public boolean decode(DecoderCore<InStream> core, Context ctx, InStream is) {
            if (is.currentEventType().equals(JsonEvent.Type.NULL)) {
                is.readNull();
                return true;
            } else {
                return false;
            }
        }
    };

    private final BooleanCodec<InStream, OutStream> BOOLEAN_CODEC = new BooleanCodec<InStream, OutStream>() {
        @Override
        public OutStream encodeBool(EncoderCore<OutStream> core, Context ctx, boolean value, OutStream os) {
            return (OutStream)os.writeBoolean(value);
        }

        @Override
        public boolean decodeBool(DecoderCore<InStream> core, Context ctx, InStream is) {
            return is.readBoolean();
        }
    };

    private final Codec<boolean[], InStream, OutStream> BOOLEAN_ARRAY_CODEC = new FinalCodec<boolean[], InStream, OutStream>() {
        @Override
        public Class<boolean[]> type() {
            return boolean[].class;
        }

        @Override
        public OutStream encodeImpl(EncoderCore<OutStream> core, Context ctx, boolean[] value, OutStream os) {
            os.startArray();
            for (boolean val : value) {
                booleanCodec().encodeBool(core, ctx, val, os);
            }
            return os.endArray();
        }

        @Override
        public boolean[] decodeImpl(DecoderCore<InStream> core, Context ctx, InStream is) {
            boolean[] arr = new boolean[core.config().defaultArraySize()];
            is.startArray();
            int i = 0;
            while (is.notEOF() && is.currentEventType() != JsonEvent.Type.ARRAY_END) {
                if (i == arr.length) {
                    arr = Arrays.copyOf(arr, core.config().resizeArray(arr.length));
                }
                arr[i++] = booleanCodec().decodeBool(core, ctx, is);
            }
            is.endArray();
            return Arrays.copyOf(arr, i);
        }
    };

    private final IntegerCodec<InStream, OutStream> INTEGER_CODEC = new IntegerCodec<InStream, OutStream>() {
        @Override
        public OutStream encodeInt(EncoderCore<OutStream> core, Context ctx, int value, OutStream os) {
            return (OutStream)os.writeInt(value);
        }

        @Override
        public int decodeInt(DecoderCore<InStream> core, Context ctx, InStream is) {
            return is.readInt();
        }
    };

    private final Codec<int[], InStream, OutStream> INTEGER_ARRAY_CODEC = new FinalCodec<int[], InStream, OutStream>() {
        @Override
        public Class<int[]> type() {
            return int[].class;
        }

        @Override
        public OutStream encodeImpl(EncoderCore<OutStream> core, Context ctx, int[] value, OutStream os) {
            os.startArray();
            for (int val : value) {
                integerCodec().encodeInt(core, ctx, val, os);
            }
            return os.endArray();
        }

        @Override
        public int[] decodeImpl(DecoderCore<InStream> core, Context ctx, InStream is) {
            if (core.format().nullCodec().decode(core, ctx, is)) {
                return null;
            } else {
                return decodeImpl(core, ctx, is);
            }
        }
    };

    private final Codec<String, InStream, OutStream> STRING_CODEC = new FinalCodec<String, InStream, OutStream>() {
        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public OutStream encodeImpl(EncoderCore<OutStream> core, Context ctx, String value, OutStream os) {
            return (OutStream)os.writeString(value);
        }

        @Override
        public String decodeImpl(DecoderCore<InStream> core, Context ctx, InStream is) {
            return is.readString();
        }
    };

    private final DynamicCodec<InStream, OutStream> DYNAMIC_CODEC = new DynamicCodec<InStream, OutStream>() {

        @Override
        public <T> CodecFormat.EncodeResult<OutStream> encodeDynamic(
                EncoderCore<OutStream> core,
                Context ctx,
                T value,
                OutStream os,
                Encoder<T, OutStream> encoder
        ) {
            final Class<T> dynType = (Class<T>) value.getClass();
            if (core.config().dynamicTypeMatch(encoder.type(), dynType)) {
                return new EncodeResult<>(false, os);
            } else if (!core.config().dynamicTypeTags()) {
                final Codec<T, InStream, OutStream> dynCodec = getDynCodec.apply(dynType);
                dynCodec.encode(core, ctx, value, os);
                return new EncodeResult<>(true, os);
            } else {
                final Codec<T, InStream, OutStream> dynCodec = getDynCodec.apply(dynType);
                os.startObject();

                os.writeField(core.config().get(JsonConfig.TYPE_FIELD_NAME))
                        .writeString(core.config().classToName(dynType));
                os.writeField(core.config().get(JsonConfig.VALUE_FIELD_NAME));
                dynCodec.encode(core, ctx, value, os);

                os.endObject();
                return new EncodeResult<>(true, os);
            }
        }

        @Override
        public <T> T decodeDynamic(EncoderCore<OutStream> core, Context ctx, InStream is) {
            throw null;
        }
    };

    @Override
    public NullCodec<InStream, OutStream> nullCodec() {
        return NULL_CODEC;
    }

    @Override
    public BooleanCodec<InStream, OutStream> booleanCodec() {
        return BOOLEAN_CODEC;
    }

    @Override
    public Codec<boolean[], InStream, OutStream> booleanArrayCodec() {
        return BOOLEAN_ARRAY_CODEC;
    }

    @Override
    public IntegerCodec<InStream, OutStream> integerCodec() {
        return INTEGER_CODEC;
    }

    @Override
    public Codec<int[], InStream, OutStream> integerArrayCodec() {
        return INTEGER_ARRAY_CODEC;
    }

    @Override
    public Codec<String, InStream, OutStream> stringCodec() {
        return STRING_CODEC;
    }

    @Override
    public <T> Codec<T, InStream, OutStream> objectCodec(Class<T> clazz, Map<String, FieldCodec<?, InStream, OutStream>> fieldCodecs, ObjectCreator<T> ctor) {
        return null;
    }

    @Override
    public <T> Codec<T[], InStream, OutStream> objectArrayCodec(Class<T[]> arrType, Class<T> elemType, Codec<T, InStream, OutStream> elemCodec) {
        return null;
    }

    @Override
    public DynamicCodec<InStream, OutStream> dynamicCodec() {
        return DYNAMIC_CODEC;
    }
}
