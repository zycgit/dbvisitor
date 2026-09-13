/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.util.List;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperQueryResultCase extends AnnotationMapperCrudSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_QUERY)
    public void annotationMapperQuery_shouldReturnObjectListAndScalar() throws Exception {
        prepareQueryRows();
        assertEquals(2, queryObjectRows().size());
        assertEquals(2, queryScalarCount());
        assertEquals(2, queryOtherObjectRows().size());
    }

    protected void prepareQueryRows() throws Exception {
        this.mapper.insertUserWithParams(baseId() + 8, "AnnoQueryOne", 51, "q1@test.com");
        this.mapper.insertUserWithParams(baseId() + 9, "AnnoQueryTwo", 51, "q2@test.com");
        this.mapper.insertUserWithParams(baseId() + 10, "OtherQuery", 52, "q3@test.com");

    }

    protected List<?> queryObjectRows() throws Exception {
        return this.mapper.selectByAge(51);
    }

    protected List<?> queryOtherObjectRows() throws Exception {
        return this.mapper.selectByNameLike("AnnoQuery%");
    }

    protected int queryScalarCount() throws Exception {
        return this.mapper.countByAge(51);
    }
}
