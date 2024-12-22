package net.hasor.dbvisitor.dal.repository;
import net.hasor.dbvisitor.dal.MapperRegistry;
import net.hasor.dbvisitor.mapper.def.InsertConfig;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.test.dal.dynamic.TextBuilderContext;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class Mapper3DalTest {
    @Test
    public void bindTest_01() throws Throwable {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/dal_dynamic/mapper/mapper_3.xml");

        InsertConfig dynamicSql = (InsertConfig) registry.findDynamicSql("net.hasor.test.dal.mapper_3", "testInsert");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", "123");
        data1.put("uid", "123");

        SqlBuilder builder1 = dynamicSql.buildQuery(data1, new TextBuilderContext());
        SqlBuilder builder2 = dynamicSql.getSelectKey().buildQuery(data1, new TextBuilderContext());

        assert builder1.getSqlString().contains("insert into auto_id");
        assert builder2.getSqlString().contains("SELECT LAST_INSERT_ID()");
    }
}
