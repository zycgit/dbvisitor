package net.hasor.scene.jdbc.extractor;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** 使用 ResultSetExtractor 接口来接收结果集 */
public class ResultSetExtractorTestCase {

    @Test
    public void setterArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            PreparedStatementSetter setter = ps -> ps.setInt(1, 40);
            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", setter, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void sourceArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlArgSource argSource = new MapSqlArgSource(CollectionUtils.asMap("arg", 40));
            List<String> result = jdbcTemplate.query("select * from user_table where age > :arg order by id", argSource, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void paramArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[] args = new Object[] { SqlArg.valueOf(40) };
            List<String> result = jdbcTemplate.query("select * from user_table where age > ? order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

}