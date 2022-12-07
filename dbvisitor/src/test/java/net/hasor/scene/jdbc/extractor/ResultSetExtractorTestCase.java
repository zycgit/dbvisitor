package net.hasor.scene.jdbc.extractor;
import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.jdbc.PreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.SqlParameterSource;
import net.hasor.dbvisitor.jdbc.SqlParameterUtils;
import net.hasor.dbvisitor.jdbc.core.ArgPreparedStatementSetter;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.paramer.MapSqlParameterSource;
import net.hasor.scene.UserNameResultSetExtractor;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class ResultSetExtractorTestCase {
    @Test
    public void noArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[] args = new Object[] { SqlParameterUtils.withInput(40) };
            List<String> result = jdbcTemplate.query("select * from user where age > ? order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void arrayArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<String> result = jdbcTemplate.query("select * from user where age > 40 order by id", new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void mapArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Map<String, Object> args = CollectionUtils.asMap("age", 40);
            List<String> result = jdbcTemplate.query("select * from user where age > :age order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void setterArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            PreparedStatementSetter setter = new ArgPreparedStatementSetter(new Object[] { 40 });
            List<String> result = jdbcTemplate.query("select * from user where age > ? order by id", setter, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void sourceArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            SqlParameterSource argSource = new MapSqlParameterSource(CollectionUtils.asMap("arg", 40));
            List<String> result = jdbcTemplate.query("select * from user where age > :arg order by id", argSource, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

    @Test
    public void paramArgsExtractor_0() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            Object[] args = new Object[] { SqlParameterUtils.withInOut(40, Types.INTEGER) };
            List<String> result = jdbcTemplate.query("select * from user where age > ? order by id", args, new UserNameResultSetExtractor());

            assert result.size() == 2;
            assert result.get(0).equals("jon wes");
            assert result.get(1).equals("mary");
        }
    }

}