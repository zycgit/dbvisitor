/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;


@NxnContract
public abstract class AnnotationMapperCommandErrorCase extends AnnotationMapperBoundarySupport {
    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_SQL_ERROR)
    public void annotationMapperSqlErrors_shouldPropagateFromSyntaxTableAndColumnFailures() throws Exception {
        prepareInvalidCommands();
        for (int index = 0; index < 3; index++) {
            final int scenario = index;
            Exception error = assertThrows(Exception.class, () -> executeInvalidCommand(scenario));
            verifyCommandFailure(scenario, error);
        }
        verifyAfterCommandFailures();
    }

    protected void prepareInvalidCommands() throws Exception {
    }

    protected void executeInvalidCommand(int scenario) throws Exception {
        switch (scenario) {
            case 0 -> mapper.selectWithSyntaxError();
            case 1 -> mapper.selectFromNonExistentTable();
            case 2 -> mapper.selectNonExistentColumn();
            default -> throw new IllegalArgumentException("Unknown failure scenario: " + scenario);
        }
    }

    protected void verifyCommandFailure(int scenario, Exception error) {
        assertNotNull(error);
    }

    protected void verifyAfterCommandFailures() throws Exception {
    }
}
