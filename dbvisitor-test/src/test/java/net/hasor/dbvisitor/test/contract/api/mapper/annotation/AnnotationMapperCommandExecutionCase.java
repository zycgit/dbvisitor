/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

@NxnContract
public abstract class AnnotationMapperCommandExecutionCase extends AnnotationMapperCrudSupport {
    // 能力归属：Mapper API / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_EXECUTE, column = "mapper/method-annotations/execution")
    public void annotationMapperExecute_shouldRunDdlAndDml() throws Exception {
        prepareCommandResource();
        this.mapper.insertTempData(1, "temp-one");

        assertEquals("temp-one", this.mapper.selectTempData(1));

        dropCommandResource();
    }

    protected void prepareCommandResource() throws Exception {
        dropTableIfExists("temp_anno_test");
        jdbcTemplate.executeUpdate(createSimpleTempTableSql("temp_anno_test"));
    }

    protected void dropCommandResource() throws Exception {
        dropTableIfExists("temp_anno_test");
    }
}
