package net.hasor.dbvisitor.test.realdb.pg.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.MappingTableDefinitionContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlMappingTableDefinitionContractTest extends MappingTableDefinitionContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
