package net.hasor.dbvisitor.test.realdb.pg.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperKeyGenerationContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.PostgreSqlProfile;

public class PostgreSqlXmlMapperKeyGenerationContractTest extends XmlMapperKeyGenerationContractTest {
    @Override
    protected DataSourceProfile profile() {
        return PostgreSqlProfile.INSTANCE;
    }
}
