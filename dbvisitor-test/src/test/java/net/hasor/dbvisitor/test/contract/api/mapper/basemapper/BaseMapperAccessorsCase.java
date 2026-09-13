/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@NxnContract
public abstract class BaseMapperAccessorsCase extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_ACCESSORS)
    public void baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis() throws SQLException {
        int lambdaId = baseId() + 141;
        int jdbcId = baseId() + 142;

        assertEquals(UserInfo.class, this.mapper.entityType());
        assertNotNull(this.mapper.session());
        assertNotNull(this.mapper.lambda());
        assertNotNull(this.mapper.jdbc());

        int lambdaResult = this.mapper.lambda().insert(UserInfo.class).applyEntity(user(lambdaId, "BaseAccessorLambda", 141, null)).executeSumResult();
        int jdbcResult = this.mapper.jdbc().executeUpdate(//
                "INSERT INTO user_info (id, name, age, create_time) VALUES (?, ?, ?, ?)", //
                new Object[] { jdbcId, "BaseAccessorJdbc", 142, new Date() });

        assertEquals(1, lambdaResult);
        assertEquals(1, jdbcResult);
        assertNotNull(this.mapper.selectById(lambdaId));
        assertNotNull(this.mapper.selectById(jdbcId));
    }
}
