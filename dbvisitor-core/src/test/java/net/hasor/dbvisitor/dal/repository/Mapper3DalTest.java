package net.hasor.dbvisitor.dal.repository;
import net.hasor.dbvisitor.dal.repository.config.InsertSqlConfig;
import net.hasor.dbvisitor.dialect.SqlBuilder;
import net.hasor.test.db.dal.dynamic.TextBuilderContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class Mapper3DalTest {
    @Test
    public void bindTest_01() throws Throwable {
        DalRegistry registry = new DalRegistry();
        registry.loadMapper("/net_hasor_db/dal_dynamic/mapper_3.xml");

        InsertSqlConfig dynamicSql = (InsertSqlConfig) registry.findDynamicSql("net.hasor.test.db.dal.Mapper1Dal", "testInsert");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "123");
        data1.put("uid", "123");

        SqlBuilder builder1 = dynamicSql.buildQuery(data1, new TextBuilderContext());
        SqlBuilder builder2 = dynamicSql.getSelectKey().buildQuery(data1, new TextBuilderContext());

        assert builder1.getSqlString().contains("insert into auto_id");
        assert builder2.getSqlString().contains("SELECT LAST_INSERT_ID()");
    }
}
