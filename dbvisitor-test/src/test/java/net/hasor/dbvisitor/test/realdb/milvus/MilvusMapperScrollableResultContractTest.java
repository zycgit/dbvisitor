/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.milvus;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import org.junit.Test;
import static org.junit.Assert.*;

public class MilvusMapperScrollableResultContractTest extends MilvusMapperResultSqlSupport {
    @Test
    @Capability(CapabilityId.ADAPTER_MILVUS_MAPPER_RESULT_SCROLL_BOUNDARY)
    public void annotationExtractorShouldRejectUnsupportedScrollableResults() throws Exception {
        NativeResultMapper mapper = this.session.createMapper(NativeResultMapper.class);
        assertThrows(java.sql.SQLFeatureNotSupportedException.class, () -> mapper.scrollExtracted(1));
    }
}
