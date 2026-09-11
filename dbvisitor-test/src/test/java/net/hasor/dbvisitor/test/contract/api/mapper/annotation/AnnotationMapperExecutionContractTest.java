/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnotationMapperExecutionContractTest extends AnnotationMapperAttributeSupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_STATEMENT_TYPE)
    public void annotationAttributes_shouldSupportPreparedAndStatementTypes() throws Exception {
        UserInfo prepared = this.mapper.selectByIdPrepared(baseId() + 1);
        UserInfo statement = this.mapper.selectByIdStatement(baseId() + 2);

        assertNotNull(prepared);
        assertEquals("AttrNxn1", prepared.getName());
        assertNotNull(statement);
        assertEquals("AttrNxn2", statement.getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_TIMEOUT)
    public void annotationAttributes_shouldApplyTimeoutOnQueryAndDml() throws Exception {
        assertEquals("AttrNxn3", this.mapper.selectByIdDefaultTimeout(baseId() + 3).getName());
        assertEquals("AttrNxn4", this.mapper.selectByIdWithTimeout(baseId() + 4).getName());
        assertEquals("AttrNxn5", this.mapper.selectWithMaxTimeout(baseId() + 5).getName());

        assertEquals(1, this.mapper.updateWithTimeout(baseId() + 6, 99));
        assertEquals(Integer.valueOf(99), this.mapper.selectByIdPrepared(baseId() + 6).getAge());
        assertEquals(1, this.mapper.deleteWithTimeout(baseId() + 7));
        assertNull(this.mapper.selectByIdPrepared(baseId() + 7));

        int insertId = explicitId(100);
        assertEquals(1, this.mapper.insertWithTimeout(user(insertId, "AttrTimeoutInsert", 25, "timeout@nxn.test")));
        assertEquals("AttrTimeoutInsert", this.mapper.selectByIdPrepared(insertId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_FETCH_SIZE)
    public void annotationAttributes_shouldApplyFetchSizeVariantsWithoutChangingResults() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithDefaultFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithSmallFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithLargeFetchSize(PATTERN));
        assertAtLeastSeedRows(this.mapper.selectWithFetchSizeOne(PATTERN));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_MULTILINE)
    public void annotationAttributes_shouldJoinMultilineSqlValueArray() throws Exception {
        int insertId = explicitId(102);
        assertEquals(1, this.mapper.insertMultiLine(user(insertId, "AttrMultiLine", 27, "multiline@nxn.test")));

        assertEquals("AttrMultiLine", this.mapper.selectByIdPrepared(insertId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_ATTRIBUTE_COMBINED)
    public void annotationAttributes_shouldSupportCombinedAndDefaultAttributes() throws Exception {
        assertAtLeastSeedRows(this.mapper.selectWithCombinedAttributes(PATTERN));
        assertEquals("AttrNxn1", this.mapper.selectWithAllDefaults(baseId() + 1).getName());
    }
}
