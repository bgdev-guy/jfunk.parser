package org.javafp.json;

import org.javafp.parsec4j.*;
import org.javafp.util.Chr;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class GrammarTest {
    private static final String test0 = "null";

    private static final String test1 = "true";

    private static final String test2 = "12.0";

    private static final String test3 = "[]";

    private static final String test4 = "{\"key\":[1.0,\"value\",true,null]}";

    private static final String example5 = "{\n" +
        "    \"glossary\": {\n" +
        "        \"title\": \"example glossary\",\n" +
        "\t\t\"GlossDiv\": {\n" +
        "            \"title\": \"S\",\n" +
        "\t\t\t\"GlossList\": {\n" +
        "                \"GlossEntry\": {\n" +
        "                    \"ID\": \"SGML\",\n" +
        "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
        "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
        "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
        "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
        "\t\t\t\t\t\"GlossDef\": {\n" +
        "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
        "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
        "                    },\n" +
        "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}";

    private static final String[] tests = {
        test0, test1, test2, test3, test4
    };

    @Test
    public void testRoundTrip() {
        Arrays.stream(tests).forEach(GrammarTest::roundTrip);
    }

    private static void roundTrip(String json) {
        System.out.println(" Parsing: " + json);
        final Result<Chr, Node> result = Grammar.parse(json);
        final Node node = result.getOrThrow();

        final String json2 = node.toJson();
        System.out.println(json2);

        assertEquals(json, json2);
    }

    @Test
    public void testSuccessParse() {
        final Result<Chr, Node> result = Grammar.parse(example5);
        final Node node = result.getOrThrow();
    }
}
