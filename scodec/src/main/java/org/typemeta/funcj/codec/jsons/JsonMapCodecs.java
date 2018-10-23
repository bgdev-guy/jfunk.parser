package org.typemeta.funcj.codec.jsons;

import org.typemeta.funcj.codec.*;
import org.typemeta.funcj.codec.jsons.JsonIO.Input;
import org.typemeta.funcj.codec.jsons.JsonIO.Output;

import java.util.HashMap;
import java.util.Map;

public abstract class JsonMapCodecs {

    public static class MapCodec<K, V> implements Codec<Map<K, V>, Input, Output> {
        private final JsonCodecCoreImpl core;
        private final Class<Map<K, V>> type;
        private final Codec<K, Input, Output> keyCodec;
        private final Codec<V, Input, Output> valueCodec;

        public MapCodec(
                JsonCodecCoreImpl core,
                Class<Map<K, V>> type,
                Codec<K, Input, Output> keyCodec,
                Codec<V, Input, Output> valueCodec) {
            this.core = core;
            this.type = type;
            this.keyCodec = keyCodec;
            this.valueCodec = valueCodec;
        }

        @Override
        public CodecCoreIntl<Input, Output> core() {
            return core;
        }

        @Override
        public Class<Map<K, V>> type() {
            return type;
        }

        private Codec<Map<K, V>, Input, Output> getCodec(Class<Map<K, V>> type) {
            return  core().mapCodec(type, keyCodec, valueCodec);
        }

        @Override
        public Output encode(Map<K, V> map, Output out) {
            final String keyFieldName = core.keyFieldName();
            final String valueFieldName = core.valueFieldName();

            out.startArray();

            map.forEach((k, v) -> {
                out.startObject();
                out.writeField(keyFieldName);
                keyCodec.encodeWithCheck(k, out);
                out.writeField(valueFieldName);
                valueCodec.encodeWithCheck(v, out);
                out.endObject();
            });

            return out.endArray();
        }

        @Override
        public Map<K, V> decode(Input in) {
            final String keyFieldName = core.keyFieldName();
            final String valueFieldName = core.valueFieldName();

            final Map<K, V> map = core.getTypeConstructor(type).construct();

            in.startArray();

            while(in.notEOF() && in.currentEventType() == Input.Event.Type.OBJECT_START) {
                K key = null;
                V val = null;

                in.startObject();

                while (key == null || val == null) {
                    final String name = in.readFieldName();
                    if (name.equals(keyFieldName)) {
                        if (key == null) {
                            key = keyCodec.decodeWithCheck(in);
                        } else {
                            throw new CodecException("Duplicate fields called " + keyFieldName);
                        }
                    } else if (name.equals(valueFieldName)) {
                        if (val == null) {
                            val = valueCodec.decodeWithCheck(in);
                        } else {
                            throw new CodecException("Duplicate fields called " + valueFieldName);
                        }
                    }
                }

                map.put(key, val);

                in.endObject();
            }

            in.endArray();

            return map;
        }

        @Override
        public Output encodeWithCheck(Map<K, V> val, Output out) {
            if (core().encodeNull(val, out)) {
                return out;
            } else {
                if (!core().encodeDynamicType(this, val, out, this::getCodec)) {
                    return encode(val, out);
                } else {
                    return out;
                }
            }
        }

        @Override
        public Map<K, V> decodeWithCheck(Input in) {
            if (core().decodeNull(in)) {
                return null;
            } else {
                final Map<K, V> val = core().decodeDynamicType(in);
                if (val != null) {
                    return val;
                } else {
                    return decode(in);
                }
            }
        }
    }

    public static class StringMapCodec<V> implements Codec<Map<String, V>, Input, Output> {
        private final JsonCodecCoreImpl core;
        private final Class<Map<String, V>> type;
        private final Codec<V, Input, Output> valueCodec;

        public StringMapCodec(
                JsonCodecCoreImpl core,
                Class<Map<String, V>> type,
                Codec<V, Input, Output> valueCodec) {
            this.core = core;
            this.type = type;
            this.valueCodec = valueCodec;
        }

        @Override
        public CodecCoreIntl<Input, Output> core() {
            return core;
        }

        @Override
        public Class<Map<String, V>> type() {
            return type;
        }

        private Codec<Map<String, V>, Input, Output> getCodec(Class<Map<String, V>> type) {
            return  core().mapCodec(type, valueCodec);
        }

        @Override
        public Output encode(Map<String, V> map, Output out) {
            out.startObject();

            map.forEach((key, val) -> {
                out.writeField(key);
                valueCodec.encodeWithCheck(val, out);
            });

            return out.endObject();
        }

        @Override
        public Map<String, V> decode(Input in) {
            in.startObject();

            final Map<String, V> map = core.getTypeConstructor(type).construct();

            while(in.notEOF() && in.currentEventType() == Input.Event.Type.FIELD_NAME) {
                final String key = in.readFieldName();
                final V val = valueCodec.decodeWithCheck(in);
                map.put(key, val);
            }

            in.endObject();

            return map;
        }

        @Override
        public Output encodeWithCheck(Map<String, V> val, Output out) {
            if (core().encodeNull(val, out)) {
                return out;
            } else {
                if (!core().encodeDynamicType(this, val, out, this::getCodec)) {
                    return encode(val, out);
                } else {
                    return out;
                }
            }
        }

        @Override
        public Map<String, V> decodeWithCheck(Input in) {
            if (core().decodeNull(in)) {
                return null;
            } else {
                final Map<String, V> val = core().decodeDynamicType(
                        in,
                        type -> core().mapCodec(
                                core().nameToClass(type),
                                valueCodec
                        ).decode(in)
                );
                if (val != null) {
                    return val;
                } else {
                    return decode(in);
                }
            }
        }
    }
}
