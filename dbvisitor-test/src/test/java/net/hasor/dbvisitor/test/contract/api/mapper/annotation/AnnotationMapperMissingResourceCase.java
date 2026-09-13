/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;


@NxnContract
public abstract class AnnotationMapperMissingResourceCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR)
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
