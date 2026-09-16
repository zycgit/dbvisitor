/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.material.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import static org.junit.Assert.assertEquals;

/** Verifies the explicit JDBC option while making handler selection visible in stored data. */
public class ExplicitVarcharTypeHandler extends UpperCaseTypeHandler {
    @Override
    public void setParameter(PreparedStatement ps, int index, String value, Integer jdbcType) throws SQLException {
        assertEquals(Integer.valueOf(Types.VARCHAR), jdbcType);
        super.setParameter(ps, index, value, jdbcType);
    }
}
