package net.hasor.dbvisitor.test.realdb.mssql.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MsSqlProfile;

public class MsSqlXmlMapperKeyGenerationContractTest extends XmlMapperKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MsSqlProfile.INSTANCE;
    }

    @Override
    protected String mapperResource() {
        return "/realdb/mssql/material/XmlKeyGenerationMapper.xml";
    }
}
