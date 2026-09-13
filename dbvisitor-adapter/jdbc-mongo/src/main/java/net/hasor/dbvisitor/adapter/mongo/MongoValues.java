/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.mongo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import net.hasor.dbvisitor.driver.AdapterType;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

final class MongoValues {
    private MongoValues() {
    }

    static Object jdbcValue(Object value) {
        if (value instanceof Decimal128) {
            return ((Decimal128) value).bigDecimalValue();
        }
        if (value instanceof Binary) {
            return ((Binary) value).getData();
        }
        if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        return value;
    }

    static String jdbcType(Object value) {
        if (value == null) {
            return AdapterType.Null;
        }
        if (value instanceof Integer) {
            return AdapterType.Int;
        }
        if (value instanceof Long) {
            return AdapterType.Long;
        }
        if (value instanceof Double || value instanceof Float) {
            return AdapterType.Double;
        }
        if (value instanceof BigDecimal || value instanceof Decimal128) {
            return AdapterType.BigDecimal;
        }
        if (value instanceof Boolean) {
            return AdapterType.Boolean;
        }
        if (value instanceof Date) {
            return AdapterType.SqlTimestamp;
        }
        if (value instanceof byte[] || value instanceof Binary) {
            return AdapterType.Bytes;
        }
        if (value instanceof List) {
            return AdapterType.Array;
        }
        if (value instanceof String || value instanceof ObjectId) {
            return AdapterType.String;
        }
        return AdapterType.Unknown;
    }
}
