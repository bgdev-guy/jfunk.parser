package org.typemeta.funcj.codec.jsons;

import org.junit.Assert;
import org.junit.Test;
import org.typemeta.funcj.codec.*;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class JsonCodecTest extends TestBase {

    final static JsonCodecCore codec = Codecs.jsonsCodec();

    static {
        codec.registerTypeConstructor(TestTypes.NoEmptyCtor.class, () -> TestTypes.NoEmptyCtor.create(false));
        registerCustomCodec(codec);
    }

    @Override
    protected <T> void roundTrip(T val, Class<T> clazz) {
        final StringWriter sw = new StringWriter();
        codec.encode(clazz, val, sw);

        final StringReader sr = new StringReader(sw.toString());

        if (printData) {
            System.out.println(sw);
        }

        final T val2 = codec.decode(clazz, sr);

        if (!printData && !val.equals(val2)) {
            System.out.println(sw);
        }

        Assert.assertEquals(val, val2);
    }
}
