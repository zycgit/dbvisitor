package net.hasor.dbvisitor.test.realdb.db2.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.XmlMappingMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.Db2Profile;

public class Db2XmlMappingMetadataContractTest extends XmlMappingMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return Db2Profile.INSTANCE;
    }
}
