/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import java.sql.SQLException;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeLambdaScalarResultSupport;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;

public class Elastic6LambdaScalarResultTest extends NativeLambdaScalarResultSupport {
    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }

    @Override
    protected void dropCollection() throws SQLException {
        this.jdbcTemplate.execute("DELETE /" + this.collection);
    }
}
