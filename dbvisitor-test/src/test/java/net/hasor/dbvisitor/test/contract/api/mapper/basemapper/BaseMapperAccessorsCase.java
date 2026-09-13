/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.basemapper;

import net.hasor.dbvisitor.session.Session;

import java.sql.SQLException;
import net.hasor.dbvisitor.mapper.BaseMapper;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@NxnContract
public abstract class BaseMapperAccessorsCase extends BaseMapperCrudSupport {
    @Test
    @Capability(CapabilityId.BASEMAPPER_ACCESSORS)
    public void baseMapperAccessors_shouldExposeEntityTypeSessionJdbcAndLambdaApis() throws SQLException {
        BaseMapper<?> accessors = accessorMapper();
        assertEquals(accessorEntityType(), accessors.entityType());
        assertNotNull(accessors.session());
        assertNotNull(accessors.lambda());
        assertNotNull(accessors.jdbc());
        if (expectedAccessorSession() != null) {
            assertSame(expectedAccessorSession(), accessors.session());
        }
    }

    protected BaseMapper<?> accessorMapper() {
        return this.mapper;
    }

    protected Class<?> accessorEntityType() {
        return UserInfo.class;
    }

    protected Session expectedAccessorSession() { return null; }
}
