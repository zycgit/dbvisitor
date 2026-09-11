/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.jdbc.extractor;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;
import static net.hasor.test.utils.TestUtils.*;

/***
 * @version 2020-11-12
 * @author 赵永春 (zyc@hasor.net)
 */
public class ColumnMapResultSetExtractorTest extends AbstractDbTest {
    @Test
    public void testColumnMapResultSetExtractor_2() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            List<Map<String, Object>> mapList1 = jdbcTemplate.query("select * from user_info", new ColumnMapResultSetExtractor(1));
            List<Map<String, Object>> mapList2 = jdbcTemplate.query("select * from user_info", new ColumnMapResultSetExtractor());
            List<Map<String, Object>> mapList3 = jdbcTemplate.query("select * from user_info", new ColumnMapResultSetExtractor(1, TypeHandlerRegistry.DEFAULT));
            List<Map<String, Object>> mapList4 = jdbcTemplate.query("select * from user_info", new ColumnMapResultSetExtractor(1, TypeHandlerRegistry.DEFAULT, false));

            assert mapList1.size() == 1;
            assert mapList2.size() == 3;
            assert mapList3.size() == 1;
            assert mapList4.size() == 1;
        }
    }

    @Test
    public void testRowMapperResultSetExtractor_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);
            ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
            List<Map<String, Object>> mapList1 = jdbcTemplate.query("select * from user_info", new RowMapperResultSetExtractor<>(rowMapper, 1));
            List<Map<String, Object>> mapList2 = jdbcTemplate.query("select * from user_info", new RowMapperResultSetExtractor<>(rowMapper));

            assert mapList1.size() == 1;
            assert mapList2.size() == 3;
        }
    }

    @Test
    public void testColumnMapResultSetExtractor_1() throws Throwable {
        try (Connection c = DsUtils.h2Conn()) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(c);

            String dataId = beanForData4().getUserUuid();
            Object[] dataArgs = arrayForData4();
            List<Map<String, Object>> mapList = null;

            // before
            mapList = jdbcTemplate.query("select * from user_info where user_uuid =?", new Object[] { dataId }, new ColumnMapResultSetExtractor());
            assert mapList.size() == 0;
            // after
            jdbcTemplate.executeUpdate(INSERT_ARRAY, dataArgs);
            mapList = jdbcTemplate.query("select * from user_info where user_uuid =?", new Object[] { dataId }, new ColumnMapResultSetExtractor());
            assert mapList.size() == 1;
            assert mapList.get(0).get("user_name").equals(beanForData4().getName());
        }
    }
}
