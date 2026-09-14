/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.api.jdbc.metadata;

import net.hasor.dbvisitor.test.contract.api.jdbc.metadata.JdbcTableTypeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic7Profile;

public class Elastic7JdbcTableTypeTest extends JdbcTableTypeCase {
    @Override
    protected DataSourceProfile profile() {
        return Elastic7Profile.INSTANCE;
    }
}
