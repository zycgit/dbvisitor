/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.adapter.milvus.commands;

import java.sql.Connection;
import java.sql.SQLException;
import net.hasor.dbvisitor.adapter.milvus.AbstractJdbcTest;
import org.junit.Test;

public class BasicCommandTest extends AbstractJdbcTest {

    @Test
    public void test_connect() throws SQLException {
        try (Connection conn = milvusConnection()) {
            assert conn != null;
        }
    }
}
