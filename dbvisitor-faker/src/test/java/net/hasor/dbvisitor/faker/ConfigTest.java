package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.generator.*;
import net.hasor.dbvisitor.faker.realdb.DsUtils;
import org.junit.Test;

import java.util.List;

public class ConfigTest {
    @Test
    public void insertTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        FakerGenerator producer = new FakerGenerator(fakerFactory);

        FakerTable table = producer.addTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = producer.generator(OpsType.Insert);

            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }

    @Test
    public void deleteTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        FakerGenerator producer = new FakerGenerator(fakerFactory);

        FakerTable table = producer.addTable(null, null, "tb_user");
        table.setWherePolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = producer.generator(OpsType.Delete);

            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }

    @Test
    public void updateTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        FakerGenerator producer = new FakerGenerator(fakerFactory);

        FakerTable table = producer.addTable(null, null, "tb_user");
        table.setWherePolitic(SqlPolitic.RandomCol);
        table.setUpdateSetPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = producer.generator(OpsType.Delete);

            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }
}