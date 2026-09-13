/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo;
import net.hasor.dbvisitor.test.contract.api.adapter.NativeNamedFieldTypeCase;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MongoProfile;

public class MongoNamedFieldTypeTest extends NativeNamedFieldTypeCase {
    @Override
    protected DataSourceProfile profile() {
        return MongoProfile.INSTANCE;
    }
}
