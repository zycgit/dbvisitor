/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.redis.api.session;

import org.junit.Before;

import net.hasor.dbvisitor.test.contract.api.session.SessionMapperSharingCase;
import net.hasor.dbvisitor.test.nxn.config.OneApiDataSourceManager;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.RedisProfile;

/** BaseMapper-generated CRUD sharing remains distinct from declared Redis commands. */
public class RedisSessionMapperSharingTest extends SessionMapperSharingCase {
    @Override
    protected DataSourceProfile profile() {
        return RedisProfile.INSTANCE;
    }

    @Override
    @Before
    public void setup() {
        OneApiDataSourceManager.assumeCurrentDataSource("redis");
    }
}
