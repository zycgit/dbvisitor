/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import java.util.List;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MilvusLambdaPageNavigationTest extends MilvusLambdaPaginationSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_PAGINATION_MUTABLE)
    public void mutablePageShouldFollowNativePageSemantics() throws SQLException {
        verifyMutablePage();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_PAGINATION_TRAVERSAL)
    public void fullTraversalShouldFollowNativePageSemantics() throws SQLException {
        verifyFullTraversal();
    }

    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_PAGINATION_OFFSET)
    public void offsetShouldFollowNativePageSemantics() throws SQLException {
        verifyOffset();
    }

    @Test
    @Capability(CapabilityId.LAMBDA_QUERY_PAGE)
    public void lambdaQueryPage_shouldLimitAndOffsetResults() throws SQLException {
        MilvusUserInfoFixture fixture = new MilvusUserInfoFixture();
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(fixture.open());
            LambdaTemplate lambdaTemplate = new LambdaTemplate(jdbcTemplate);
            int baseId = 930000;
            for (int i = 1; i <= 12; i++) {
                jdbcTemplate.executeUpdate("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)",
                        new Object[] { baseId + 60 + i, "PageQ" + i, 20 + i, "page" + i + "@test.com", new Date() });
            }

            List<UserInfo> users = lambdaTemplate.query(UserInfo.class)//
                    .rangeBetween(UserInfo::getId, baseId + 61, baseId + 72)//
                    .orderBy("id")//
                    .initPage(5, 1)//
                    .queryForList();

            assertNotNull(users);
            assertEquals(5, users.size());
            assertEquals(Integer.valueOf(baseId + 66), users.get(0).getId());
            assertEquals(Integer.valueOf(baseId + 70), users.get(4).getId());
        } finally {
            fixture.close();
        }
    }
}
