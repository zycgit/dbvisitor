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
public abstract class AnnotationMapperCommandErrorCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_SQL_ERROR)
    public void annotationMapperSqlErrors_shouldPropagateFromSyntaxTableAndColumnFailures() throws Exception {
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectWithSyntaxError();
            }
        });
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectFromNonExistentTable();
            }
        });
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectNonExistentColumn();
            }
        });
    }
}
