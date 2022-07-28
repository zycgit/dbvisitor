package net.hasor.dbvisitor.faker;
import net.hasor.dbvisitor.faker.generator.BoundQuery;
import net.hasor.dbvisitor.faker.generator.FakerFactory;
import net.hasor.dbvisitor.faker.generator.GeneratorTable;
import net.hasor.dbvisitor.faker.generator.SqlPolitic;
import org.junit.Test;

import java.util.List;

public class ConfigTest {
    @Test
    public void insertTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());

        GeneratorTable table = fakerFactory.fetchTable(null, null, "tb_user");
        table.getTableInfo().setInsertPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = table.buildInsert(1);
            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }

    @Test
    public void deleteTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        GeneratorTable table = fakerFactory.fetchTable(null, null, "tb_user");
        table.getTableInfo().setWherePolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = table.buildDelete(1);

            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }

    @Test
    public void updateTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        GeneratorTable table = fakerFactory.fetchTable(null, null, "tb_user");
        table.getTableInfo().setWherePolitic(SqlPolitic.RandomCol);
        table.getTableInfo().setUpdateSetPolitic(SqlPolitic.RandomCol);

        for (int j = 0; j < 10; j++) {
            List<BoundQuery> boundQueries = table.buildUpdate(1);

            for (BoundQuery query : boundQueries) {
                System.out.println(query.getSqlString());
            }
        }
    }
}