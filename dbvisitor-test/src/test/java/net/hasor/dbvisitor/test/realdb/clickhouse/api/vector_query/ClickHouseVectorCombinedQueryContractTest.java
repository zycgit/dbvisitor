/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.clickhouse.api.vector_query;

import net.hasor.dbvisitor.test.contract.api.vector_query.VectorCombinedQueryContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.ClickHouseProfile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;

public class ClickHouseVectorCombinedQueryContractTest extends VectorCombinedQueryContractTest {
    @Override
    public void setup() throws IOException, SQLException {
        super.setup();
        MappingRegistry registry = new MappingRegistry();
        registry.loadMapping("/mapping/clickhouse_vector.xml");
        lambdaTemplate = new LambdaTemplate(dataSource, registry, null);
    }

    @Override
    protected Object queryVector(List<Float> vector) {
        return vector;
    }

    @Override
    protected DataSourceProfile profile() {
        return ClickHouseProfile.INSTANCE;
    }
}
