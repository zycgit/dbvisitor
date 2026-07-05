package net.hasor.dbvisitor.test.realdb.mysql.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlMapperResultHandlerContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.MySqlProfile;

public class MySqlXmlMapperResultHandlerContractTest extends XmlMapperResultHandlerContractTest {
    @Override
    protected DataSourceProfile profile() {
        return MySqlProfile.INSTANCE;
    }
}
