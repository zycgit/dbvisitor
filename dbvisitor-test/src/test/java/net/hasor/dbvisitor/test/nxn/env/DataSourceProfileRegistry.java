/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.nxn.env;

import java.util.Arrays;
import java.util.List;

public final class DataSourceProfileRegistry {
    private DataSourceProfileRegistry() {
    }

    public static List<DataSourceProfile> all() {
        // @formatter:off
        return Arrays.asList(
            H2Profile.INSTANCE,
            MySqlProfile.INSTANCE,
            PostgreSqlProfile.INSTANCE,
            MsSqlProfile.INSTANCE,
            OracleProfile.INSTANCE,
            Db2Profile.INSTANCE,
            ClickHouseProfile.INSTANCE,
            RedisProfile.INSTANCE,
            MongoProfile.INSTANCE,
            Elastic6Profile.INSTANCE,
            Elastic7Profile.INSTANCE,
            MilvusProfile.INSTANCE
        );
        // @formatter:on
    }

    public static DataSourceProfile find(String env) {
        for (DataSourceProfile profile : all()) {
            if (profile.env().equals(env)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("No NXN data source profile registered for env: " + env);
    }
}
