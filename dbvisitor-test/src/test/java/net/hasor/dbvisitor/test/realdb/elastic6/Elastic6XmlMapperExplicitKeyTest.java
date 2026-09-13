/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6;
import net.hasor.dbvisitor.test.realdb.elastic7.Elastic7XmlMapperExplicitKeyTest;
import net.hasor.dbvisitor.test.nxn.env.*;
public class Elastic6XmlMapperExplicitKeyTest extends Elastic7XmlMapperExplicitKeyTest {
    @Override
    protected DataSourceProfile profile() { return Elastic6Profile.INSTANCE; }
}
