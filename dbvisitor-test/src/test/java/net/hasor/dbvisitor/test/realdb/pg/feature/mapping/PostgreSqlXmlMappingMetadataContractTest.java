package net.hasor.dbvisitor.test.realdb.pg.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.XmlMappingMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlXmlMappingMetadataContractTest extends XmlMappingMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
