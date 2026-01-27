package net.hasor.dbvisitor.dynamic;
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.types.SqlArg;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for "Like Lego Blocks" scenario from blog post.
 */
public class LegoRulesTest {

    @Test
    public void testLegoScenario() throws Exception {
        // The template from the blog post
        // Note: Added :role prefix if needed, but blog says 'role'. Assuming OGNL.
        // Also handling whitespace and comments inside.
        String template = "SELECT * FROM tb_report\n" + "@{and, @{case, role, @{when, 'ADMIN', /* 不加限制 */},\n" + "                     @{when, 'MGR',   dept_id = :deptId},\n" + "                     @{else,          user_id = :userId}\n" + "       }\n" + "}";

        // Case 1: ADMIN - No restrictions
        {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "ADMIN");

            SqlBuilder sb = DynamicParsed.getParsedSql(template).buildQuery(ctx, new TestQueryContext());
            String sql = sb.getSqlString().trim();
            Object[] args = sb.getArgs();

            System.out.println("Role ADMIN SQL: " + sql);

            // Expectation: The @{and} rule sees empty content (or just comments/spaces) from @{case}
            // resulting in empty string for the rule part.
            // So just "SELECT * FROM tb_report"
            assertTrue("Should start with SELECT", sql.startsWith("SELECT * FROM tb_report"));
            // Shouldn't have WHERE unless 1=1 was there (which it isn't)
            assertTrue("Should not have WHERE clause", !sql.toUpperCase().contains("WHERE"));
            assertEquals(0, args.length);
        }

        // Case 2: MGR - Department restriction
        {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "MGR");
            ctx.put("deptId", "D001");

            SqlBuilder sb = DynamicParsed.getParsedSql(template).buildQuery(ctx, new TestQueryContext());
            String sql = sb.getSqlString().trim();
            Object[] args = sb.getArgs();

            System.out.println("Role MGR SQL: " + sql);

            // Expectation: @{case} returns "dept_id = :deptId".
            // @{and} sees "dept_id = ?", adds "WHERE" because it's at the start of conditions.
            // Result: "SELECT * FROM tb_report WHERE dept_id = ?" (normalized spaces)

            assertTrue("Should contain WHERE", sql.toUpperCase().contains("WHERE"));
            assertTrue("Should contain dept_id", sql.contains("dept_id"));
            assertEquals(1, args.length);
            assertEquals("D001", ((SqlArg) args[0]).getValue());
        }

        // Case 3: Other - User restriction
        {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("role", "GUEST");
            ctx.put("userId", "U999");

            SqlBuilder sb = DynamicParsed.getParsedSql(template).buildQuery(ctx, new TestQueryContext());
            String sql = sb.getSqlString().trim();
            Object[] args = sb.getArgs();

            System.out.println("Role GUEST SQL: " + sql);

            // Expectation: @{case} hits else, returns "user_id = :userId".
            // @{and} adds WHERE.

            assertTrue("Should contain WHERE", sql.toUpperCase().contains("WHERE"));
            assertTrue("Should contain user_id", sql.contains("user_id"));
            assertEquals(1, args.length);
            assertEquals("U999", ((SqlArg) args[0]).getValue());
        }
    }
}
