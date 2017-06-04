package org.funcj.codec.json;

import org.funcj.codec.Codec;
import org.funcj.codec.utils.ReflectionUtils;
import org.funcj.control.Exceptions;
import org.funcj.json.*;
import org.funcj.util.Functions;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public abstract class JsonMapCodecs {

    public static class MapCodec<K, V> implements Codec<Map<K, V>, JSValue> {
        private final JsonCodecCore core;
        private final Codec<K, JSValue> keyCodec;
        private final Codec<V, JSValue> valueCodec;

        public MapCodec(
                JsonCodecCore core,
                Codec<K, JSValue> keyCodec,
                Codec<V, JSValue> valueCodec) {
            this.core = core;
            this.keyCodec = keyCodec;
            this.valueCodec = valueCodec;
        }

        @Override
        public JSValue encode(Map<K, V> map, JSValue out) {
            final String keyFieldName = core.keyFieldName();
            final String valueFieldName = core.valueFieldName();


            final Functions.F<Map.Entry<K, V>, JSValue> encode =
                    en -> {
                        final K key = en.getKey();
                        final V value = en.getValue();
                        final LinkedHashMap<String, JSValue> elemFields = new LinkedHashMap<>();
                        elemFields.put(keyFieldName, keyCodec.encode(key, out));
                        elemFields.put(valueFieldName, valueCodec.encode(value, out));
                        return Json.object(elemFields);
                    };

            final List<JSValue> nodes = map.entrySet().stream()
                    .map(encode::apply)
                    .collect(toList());

            return Json.array(nodes);
        }

        @Override
        public Map<K, V> decode(Class<Map<K, V>> dynType, JSValue in) {
            final String keyFieldName = core.keyFieldName();
            final String valueFieldName = core.valueFieldName();

            final Functions.F<Map<K, V>, Consumer<JSValue>> decodeF = m -> elemNode -> {
                final JSObject elemObjNode = elemNode.asObject();
                final K key = keyCodec.decode(elemObjNode.get(keyFieldName));
                final V val = valueCodec.decode(elemObjNode.get(valueFieldName));
                m.put(key, val);
            };

            final Map<K, V> map = Exceptions.wrap(
                    () -> ReflectionUtils.newInstance(dynType),
                    JsonCodecException::new);

            final JSArray objNode = in.asArray();
            final Consumer<JSValue> decode = decodeF.apply(map);
            objNode.forEach(decode);

            return map;
        }
    }

    public static class StringMapCodec<V> implements Codec<Map<String, V>, JSValue> {
        private final JsonCodecCore core;
        private final Codec<V, JSValue> valueCodec;

        public StringMapCodec(JsonCodecCore core, Codec<V, JSValue> valueCodec) {
            this.core = core;
            this.valueCodec = valueCodec;
        }

        @Override
        public JSValue encode(Map<String, V> map, JSValue out) {
            final LinkedHashMap<String, JSValue> fields = new LinkedHashMap<>();

            map.forEach((k, v) -> {
                final JSValue value = valueCodec.encode(v, out);
                fields.put(k, value);
            });

            return Json.object(fields);
        }

        @Override
        public Map<String, V> decode(Class<Map<String, V>> dynType, JSValue in) {
            final JSObject objNode = in.asObject();

            final Map<String, V> map = Exceptions.wrap(
                    () -> ReflectionUtils.newInstance(dynType),
                    JsonCodecException::new);

            objNode.forEach((k, v) -> {
                final V value = valueCodec.decode(v);
                map.put(k, value);
            });

            return map;
        }
    }
}
