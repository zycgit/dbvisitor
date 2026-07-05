package net.hasor.dbvisitor.test.realdb.mssql.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.XmlMappingMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlXmlMappingMetadataContractTest extends XmlMappingMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }
}
