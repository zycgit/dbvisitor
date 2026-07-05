package net.hasor.dbvisitor.test.realdb.mysql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.XmlMappingMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlXmlMappingMetadataContractTest extends XmlMappingMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
