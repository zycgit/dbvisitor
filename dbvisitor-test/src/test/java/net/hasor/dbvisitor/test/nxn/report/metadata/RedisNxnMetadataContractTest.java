/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.report.metadata;

import net.hasor.dbvisitor.test.nxn.report.NxnMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.SupportStatus;
import java.util.Map;

public class RedisNxnMetadataContractTest extends NxnMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    protected Map<String, SupportStatus> expectedProfileStatuses() {
        return Map.of(
                CapabilityId.TYPE_BINARY_NULL, SupportStatus.UNSUPPORTED_BY_DATABASE,
                CapabilityId.TYPE_ENUM_NULL, SupportStatus.UNSUPPORTED_BY_DATABASE,
                CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS, SupportStatus.UNSUPPORTED_BY_DRIVER,
                CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR, SupportStatus.SUPPORTED);
    }
}
