package net.hasor.dbvisitor.test.realdb.oracle.feature.mapping;

import net.hasor.dbvisitor.test.contract.feature.mapping.AnnotationTableMetadataContractTest;
import net.hasor.dbvisitor.test.nxn.env.DataSourceProfile;
import net.hasor.dbvisitor.test.nxn.env.OracleProfile;

public class OracleAnnotationTableMetadataContractTest extends AnnotationTableMetadataContractTest {
    @Override
    protected DataSourceProfile profile() {
        return OracleProfile.INSTANCE;
    }
}
