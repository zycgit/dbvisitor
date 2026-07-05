package net.hasor.dbvisitor.test.realdb.oracle.api.mapper.xml;

import net.hasor.dbvisitor.test.contract.api.mapper.xml.XmlRefMapperContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleXmlRefMapperTest extends XmlRefMapperContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
