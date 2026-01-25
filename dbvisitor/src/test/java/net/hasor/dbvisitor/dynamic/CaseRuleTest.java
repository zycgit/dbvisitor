package net.hasor.dbvisitor.dynamic;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;

public class CaseRuleTest extends TestCase {
    public void testSwitchMode() throws Exception {
        String sql = "select * from users where @{case, userType, " +//
                "  @{when, 'admin', role = 'administrator'}, " +     //
                "  @{when, 'manager', role = 'manager'}, " +         //
                "  @{else, role = 'guest'} " +                       //
                "}";

        // Case 1: Match first branch
        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql);
        Map<String, Object> data = new HashMap<>();
        data.put("userType", "admin");
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("select * from users where  role = 'administrator'", query.trim());

        // Case 2: Match second branch
        data.put("userType", "manager");
        query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("select * from users where  role = 'manager'", query.trim());

        // Case 3: Default to else
        data.put("userType", "unknown");
        query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("select * from users where  role = 'guest'", query.trim());

        // Case 4: Null value in switch expression
        data.put("userType", null);
        query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("select * from users where  role = 'guest'", query.trim());
    }

    public void testIfElseMode() throws Exception {
        String sql = "update orders set @{case, , " +    //
                "  @{when, age > 60, discount = 0.5}, " +//
                "  @{when, age < 18, discount = 0.8}, " +//
                "  @{else, discount = 1.0} " +           //
                "}";

        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql);
        Map<String, Object> data = new HashMap<>();

        // Case 1: First condition true
        data.put("age", 65);
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("update orders set  discount = 0.5", query.trim());

        // Case 2: Second condition true
        data.put("age", 10);
        query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("update orders set  discount = 0.8", query.trim());

        // Case 3: No condition true
        data.put("age", 30);
        query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("update orders set  discount = 1.0", query.trim());
    }

    public void testComplexExpressions() throws Exception {
        String mapSql = "select * from items where @{case, item.status.name, " +//
                "  @{when, 'ACTIVE', state = 1}, " +//
                "  @{when, 'DELETED', state = 0} " +//
                "}";

        Map<String, Object> item = new HashMap<>();
        Map<String, Object> status = new HashMap<>();
        status.put("name", "ACTIVE");
        item.put("status", status);

        Map<String, Object> data = new HashMap<>();
        data.put("item", item);

        PlanDynamicSql plan = DynamicParsed.getParsedSql(mapSql);
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("select * from items where  state = 1", query.trim());
    }

    public void testElseFragmentation() throws Exception {
        String sql = "select type from t where @{case, typeId, " +//
                "  @{when, 1, type = 'A'}, " +                    //
                "  @{else, type = 'B', or type = 'C'} " +         //
                "}";

        Map<String, Object> data = new HashMap<>();
        data.put("typeId", 99);

        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql);
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();

        // Expecting full concatenation WITH comma restored
        assertEquals("select type from t where  type = 'B', or type = 'C'", query.trim());
    }

    public void testWhitespaceRobustness() throws Exception {
        // Lots of spaces
        String sql = "result = @{case , val ,  @{ when , 1 , one } ,  @{ else , other } }";

        Map<String, Object> data = new HashMap<>();
        data.put("val", 1);

        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql);
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();

        // The output will preserve the space in " one"
        assertEquals("result =  one", query.trim());
    }

    public void testVariableVisibility() throws Exception {
        // Variable 'x' and 'y' available
        String sql = "@{case, , @{when, x + y == 10, 'ten'}, @{else, 'not-ten'}}";
        Map<String, Object> data = new HashMap<>();
        data.put("x", 4);
        data.put("y", 6);

        PlanDynamicSql plan = DynamicParsed.getParsedSql(sql);
        String query = plan.buildQuery(new MapSqlArgSource(data), new TestQueryContext()).getSqlString();
        assertEquals("'ten'", query.trim());
    }
}
