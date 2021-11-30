package net.hasor.db.dal.dynamic;
import net.hasor.cobble.ResourcesUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.db.dal.repository.DalRegistry;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.test.db.dal.Mapper1Dal;
import net.hasor.test.db.dal.dynamic.TextBuilderContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Mapper1DalTest {
    private DalRegistry dalRegistry;

    private String loadString(String queryConfig) throws IOException {
        return IOUtils.readToString(ResourcesUtils.getResourceAsStream(queryConfig), "UTF-8");
    }

    @Before
    public void loadMapping() throws IOException {
        this.dalRegistry = new DalRegistry();
        this.dalRegistry.loadMapper(Mapper1Dal.class);
    }

    @Test
    public void bindTest_01() throws Throwable {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testBind");

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testBind.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("sellerId", "123");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123abc");
    }

    @Test
    public void bindTest_02() throws SQLException {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testBind");

        Map<String, Object> data1 = new HashMap<>();
        data1.put("sellerId", "123");
        data1.put("abc", "aaa");
        parseXml.buildQuery(data1, new TextBuilderContext());

        assert data1.get("abc").equals("123abc");
    }

    @Test
    public void chooseTest_01() throws Throwable {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testChoose");

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testChoose.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("title", "123");
        data1.put("content", "aaa");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
    }

    @Test
    public void chooseTest_02() throws Throwable {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testChoose");

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testChoose.sql_2");
        Map<String, Object> data1 = new HashMap<>();
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
    }

    @Test
    public void foreachTest_01() throws Throwable {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testForeach");

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testForeach.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("eventTypes", Arrays.asList("a", "b", "c", "d", "e"));
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("a");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals("b");
        assert ((SqlArg) builder1.getArgs()[2]).getValue().equals("c");
        assert ((SqlArg) builder1.getArgs()[3]).getValue().equals("d");
        assert ((SqlArg) builder1.getArgs()[4]).getValue().equals("e");
    }

    @Test
    public void ifTest_01() throws Throwable {
        DynamicSql parseXml = this.dalRegistry.findDynamicSql(Mapper1Dal.class, "testIf");

        String querySql1 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testIf.sql_1");
        Map<String, Object> data1 = new HashMap<>();
        data1.put("ownerID", "123");
        data1.put("ownerType", "SYSTEM");
        SqlBuilder builder1 = parseXml.buildQuery(data1, new TextBuilderContext());
        assert builder1.getSqlString().trim().equals(querySql1.trim());
        assert ((SqlArg) builder1.getArgs()[0]).getValue().equals("123");
        assert ((SqlArg) builder1.getArgs()[1]).getValue().equals("SYSTEM");

        String querySql2 = loadString("/net_hasor_db/dal_dynamic/mapper_result/Mapper1Dal_testIf.sql_2");
        Map<String, Object> data2 = new HashMap<>();
        data1.put("ownerID", "123");
        data1.put("ownerType", null);
        SqlBuilder builder2 = parseXml.buildQuery(data2, new TextBuilderContext());
        assert builder2.getSqlString().trim().equals(querySql2.trim());
        assert builder2.getArgs().length == 0;
    }
}
