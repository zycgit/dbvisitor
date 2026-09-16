/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.feature.type;

import net.hasor.dbvisitor.test.contract.feature.type.ArrayTypeJdbcCase;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.types.handler.array.ArrayTypeHandler;

public class ClickHouseArrayTypeJdbcTest extends ArrayTypeJdbcCase {
    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }

    @Override
    protected ArrayTypeHandler nullableElementArrayHandler() {
        // ClickHouse requires an explicit Nullable element type; createArrayOf("INTEGER", ...) drops nulls.
        return new ArrayTypeHandler() {
            @Override
            protected String resolveTypeName(Class<?> type) {
                if (type == Integer.class) {
                    return "Nullable(Int32)";
                }
                if (type == String.class) {
                    return "Nullable(String)";
                }
                return super.resolveTypeName(type);
            }
        };
    }
}
