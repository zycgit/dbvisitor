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

@NxnContract
public abstract class AnnotationMapperMissingResourceCase extends AnnotationMapperBoundarySupport {
    // 能力归属：Mapper API / 方法注解。
    @Test
    @Capability(value = CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR, column = "mapper/method-annotations/execution")
    public void annotationMapperExecute_shouldExposeErrorAfterDroppingTemporaryTable() throws Exception {
        dropTableIfExists("temp_anno_test");
        jdbcTemplate.executeUpdate(createSimpleTempTableSql("temp_anno_test"));
        dropTableIfExists("temp_anno_test");

        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectTempData(1);
            }
        });
    }
}
