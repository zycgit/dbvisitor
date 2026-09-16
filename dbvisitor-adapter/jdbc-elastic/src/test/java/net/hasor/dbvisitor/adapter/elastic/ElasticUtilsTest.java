/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ElasticUtilsTest {
    private final ObjectMapper json = new ObjectMapper();

    @Test
    public void sizeValuesSupportUnitsWhitespaceAndUnitlessMegabytes() {
        assertEquals(3, ElasticUtils.parseSize("3B", -1));
        assertEquals(2 * 1024, ElasticUtils.parseSize(" 2 kb ", -1));
        assertEquals(5 * 1024 * 1024, ElasticUtils.parseSize("5MB", -1));
        assertEquals(2L * 1024 * 1024 * 1024, ElasticUtils.parseSize("2GB", -1));
        assertEquals(4 * 1024 * 1024, ElasticUtils.parseSize("4", -1));
        assertEquals(0, ElasticUtils.parseSize("0B", -1));
    }

    @Test
    public void missingAndInvalidSizesKeepTheConfiguredDefault() {
        for (String value : new String[] { null, "", " ", "invalid", "1.5MB", "2TB", "MB" }) {
            assertEquals(123, ElasticUtils.parseSize(value, 123));
        }
    }

    @Test
    public void arrayNavigationSkipsNestedMatchesAndUnrelatedFields() throws Exception {
        String response = """
                {"metadata":{"docs":["nested"]},"ignored":[1,2],"docs":["actual"],"after":true}
                """;
        try (JsonParser parser = json.getFactory().createParser(response)) {
            assertTrue(ElasticUtils.navigateToArray(parser, "docs"));
            assertEquals(JsonToken.START_ARRAY, parser.currentToken());
            assertEquals(List.of("actual"), json.readValue(parser, List.class));
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("after", parser.currentName());
        }
    }

    @Test
    public void arrayNavigationAcceptsEmptyArraysAndAnAlreadyStartedObject() throws Exception {
        try (JsonParser parser = json.getFactory().createParser("{\"responses\":[]}")) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertTrue(ElasticUtils.navigateToArray(parser, "responses"));
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
    }

    @Test
    public void arrayNavigationRejectsMissingFieldsAndNonArrayValues() throws Exception {
        for (String response : new String[] { "{}", "[]", "null", "{\"docs\":null}", "{\"docs\":{}}", "{\"other\":[]}" }) {
            try (JsonParser parser = json.getFactory().createParser(response)) {
                assertFalse(ElasticUtils.navigateToArray(parser, "docs"));
            }
        }
    }

    @Test
    public void jsonValuesPreserveNestedStructuresNumbersBooleansAndNulls() throws Exception {
        Object value = ElasticUtils.rawJsonValue(json.readTree("""
                {"items":[1,2147483648,1.5,true,null,{"name":"nested"}],"empty":[],"missing":null}
                """));
        Map<?, ?> object = (Map<?, ?>) value;
        assertEquals(Arrays.asList(1, 2147483648L, 1.5, true, null, Map.of("name", "nested")), object.get("items"));
        assertEquals(Collections.emptyList(), object.get("empty"));
        assertTrue(object.containsKey("missing"));
        assertNull(object.get("missing"));
        assertNull(ElasticUtils.rawJsonValue(json.readTree("null")));
    }
}
