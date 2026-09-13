/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import net.hasor.dbvisitor.test.contract.feature.mapping.MappingRegistryCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;
import org.junit.Before;

/** Mapping configuration checks; no database command is required. */
public class Elastic7MappingRegistryTest extends MappingRegistryCase {
    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }

    @Override
    @Before
    public void setup() {
        OneApiDataSourceManager.assumeCurrentDataSource(profile().env());
    }
}
