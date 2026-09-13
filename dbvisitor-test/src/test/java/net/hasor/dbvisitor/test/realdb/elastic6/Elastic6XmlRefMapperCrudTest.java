/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import net.hasor.dbvisitor.test.realdb.elastic7.Elastic7XmlRefMapperCrudTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;

public class Elastic6XmlRefMapperCrudTest extends Elastic7XmlRefMapperCrudTest {
    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }
}
