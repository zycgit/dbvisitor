/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.lambda;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class LambdaCountCase extends LambdaQuerySupport {
    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_QUERY_COUNT, column = "builder/queries/query")
    public void lambdaQueryCount_shouldReturnMatchingRowCount() throws SQLException {
        insertUsers("Cnt", new int[] { 31, 31, 32, 33, 31 }, baseId() + 80);

        long count = lambdaTemplate.query(UserInfo.class)//
                .rangeBetween(UserInfo::getId, baseId() + 80, baseId() + 84)//
                .eq(UserInfo::getAge, 31)//
                .queryForCount();

        assertEquals(3, count);
    }

    // 能力归属：构造器 API / 查询操作。
    @Test
    @Capability(value = CapabilityId.LAMBDA_EMPTY_COUNT, column = "builder/queries/query")
    public void lambdaQueryForCount_shouldReturnZeroWhenNoRowsMatch() throws SQLException {
        long count = lambdaTemplate.query(UserInfo.class)//
                .eq(UserInfo::getId, baseId() + 3)//
                .queryForCount();

        assertEquals(0, count);
    }
}
