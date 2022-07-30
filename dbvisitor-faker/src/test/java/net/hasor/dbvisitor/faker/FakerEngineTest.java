package net.hasor.dbvisitor.faker;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.faker.generator.*;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.TypeHandler;
import org.junit.Test;

import java.util.List;

public class FakerEngineTest {
    @Test
    public void insertTest() throws Exception {
        FakerFactory fakerFactory = new FakerFactory(DsUtils.dsMySql());
        GeneratorProducer engine = new GeneratorProducer(fakerFactory);

        FakerTable table = engine.addTable(null, null, "tb_user");
        table.setInsertPolitic(SqlPolitic.RandomCol);
        table.findColumns("registerTime").ignoreAct(UseFor.Insert);
        // table.apply();

        JdbcTemplate jdbcTemplate = fakerFactory.getJdbcTemplate();
        for (int j = 0; j < 10; j++) {
            System.out.println(StringUtils.repeat("-", 20));
            List<BoundQuery> boundQueries = engine.generator(OpsType.Insert);
            for (BoundQuery query : boundQueries) {
                System.out.println(query);
                jdbcTemplate.executeUpdate(query.getSqlString(), ps -> {
                    SqlArg[] args = query.getArgs();
                    for (int i = 1; i <= args.length; i++) {
                        SqlArg arg = args[i - 1];
                        if (arg.getObject() == null) {
                            ps.setNull(i, arg.getJdbcType());
                        } else {
                            TypeHandler handler = arg.getHandler();
                            handler.setParameter(ps, i, arg.getObject(), null);
                        }
                    }
                });
            }
        }
    }
}