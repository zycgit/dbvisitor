/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;

import net.hasor.dbvisitor.test.realdb.elastic7.Elastic7IdentifierCasingTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Elastic6Profile;

public class Elastic6IdentifierCasingTest extends Elastic7IdentifierCasingTest {
    @Override
    protected DataSourceProfile profile() {
        return Elastic6Profile.INSTANCE;
    }
}
