/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.elastic;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import net.hasor.cobble.StringUtils;

/** Shared temporal, size and JSON utilities, independent of individual commands. */
public final class ElasticUtils {
    private ElasticUtils() {
    }

    // Parameter values

    /** Keeps Java-time parameters on the JDBC temporal serialization path. */
    public static Object normalizeParameter(Object value) {
        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        }
        if (value instanceof LocalDate) {
            return Date.valueOf((LocalDate) value);
        }
        if (value instanceof LocalTime) {
            return Time.valueOf((LocalTime) value);
        }
        if (value instanceof Instant) {
            return Timestamp.from((Instant) value);
        }
        return value;
    }

    // Size configuration

    /** Parses B/KB/MB/GB; values without a unit are megabytes. */
    static long parseSize(String sizeStr, long defaultValue) {
        if (StringUtils.isBlank(sizeStr)) {
            return defaultValue;
        }
        sizeStr = sizeStr.toUpperCase().trim();
        long multiplier = 1;
        if (StringUtils.endsWithIgnoreCase(sizeStr, "KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "MB")) {
            multiplier = 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "GB")) {
            multiplier = 1024 * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (StringUtils.endsWithIgnoreCase(sizeStr, "B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        } else {
            multiplier = 1024 * 1024;
        }
        try {
            return Long.parseLong(sizeStr.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // JSON traversal and values

    /** Positions the parser at an array field in the current object, skipping other fields. */
    static boolean navigateToArray(JsonParser parser, String name) throws IOException {
        if (parser.currentToken() == null) {
            parser.nextToken();
        }
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            return false;
        }

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken(); // move to value

            if (name.equals(fieldName) && parser.currentToken() == JsonToken.START_ARRAY) {
                return true;
            } else {
                parser.skipChildren();
            }
        }
        return false;
    }

    static Object rawJsonValue(JsonNode value) {
        if (value.isNull()) {
            return null;
        }
        if (value.isArray()) {
            List<Object> items = new ArrayList<>();
            for (JsonNode item : value) {
                items.add(rawJsonValue(item));
            }
            return items;
        }
        if (value.isObject()) {
            Map<String, Object> object = new LinkedHashMap<>();
            value.fields().forEachRemaining(entry -> object.put(entry.getKey(), rawJsonValue(entry.getValue())));
            return object;
        }
        return value.isBoolean() ? value.booleanValue() : value.isNumber() ? value.numberValue() : value.asText();
    }
}
