/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import org.junit.Test;
import static org.junit.Assert.*;

public class PageResultMapperTest {
    @Test
    public void pageResult_shouldMapItsRowTypeInsteadOfThePageWrapper() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper(PagedMapper.class);

        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "scalarPage").getResultType());
        assertEquals(Map.class, registry.findStatement(PagedMapper.class, "mapPage").getResultType());
        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "list").getResultType());
        assertEquals(Integer.class, registry.findStatement(PagedMapper.class, "scalar").getResultType());
        assertTrue(registry.findStatement(PagedMapper.class, "scalarPage").isUsingCollection());
        assertTrue(registry.findStatement(PagedMapper.class, "mapPage").isUsingCollection());
        assertFalse(registry.findStatement(PagedMapper.class, "scalar").isUsingCollection());
    }

    private Session database() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:annotation_pages");
        Session session = new Configuration().newSession(connection);
        session.jdbc().execute("CREATE TABLE user_info(id INT PRIMARY KEY, name VARCHAR(30))");
        session.jdbc().execute("INSERT INTO user_info VALUES(1,'Alice'),(2,'Bob'),(3,'Carol')");
        return session;
    }

    @Test
    public void scalarPagesShouldReturnRowsAndTotalCount() throws Exception {
        try (Session session = database()) {
            PagedMapper mapper = session.createMapper(PagedMapper.class);
            PageResult<Integer> first = mapper.scalarPage(new PageObject(0, 2));
            assertEquals(List.of(1, 2), first.getData());
            assertEquals(3, first.getTotalCount());
            assertEquals(2, first.getTotalPage());
            PageResult<Integer> last = mapper.scalarPage(new PageObject(1, 2));
            assertEquals(List.of(3), last.getData());
            assertEquals(3, last.getTotalCount());
            assertEquals(List.of(1, 2, 3), mapper.list());
            assertEquals(Integer.valueOf(1), mapper.scalar());
        }
    }

    @Test
    public void beanMapAndCustomRowMapperPagesShouldKeepTheWrapper() throws Exception {
        try (Session session = database()) {
            PagedMapper mapper = session.createMapper(PagedMapper.class);
            PageResult<User> beans = mapper.beanPage(new PageObject(0, 2));
            assertEquals(2, beans.getData().size());
            assertEquals("Alice", beans.getData().get(0).getName());
            assertEquals(3, beans.getTotalCount());
            PageResult<Map<String, Object>> maps = mapper.mapPage(new PageObject(0, 2));
            assertEquals(2, maps.getData().size());
            assertTrue(maps.getData().get(0).containsValue(1));
            assertEquals(3, maps.getTotalCount());
            PageResult<String> names = mapper.customPage(new PageObject(0, 2));
            assertEquals(List.of("Alice", "Bob"), names.getData());
            assertEquals(3, names.getTotalCount());
        }
    }

    @Test
    public void emptyPageShouldStillReturnAPageResult() throws Exception {
        try (Session session = database()) {
            PagedMapper mapper = session.createMapper(PagedMapper.class);
            PageResult<Integer> beyondEnd = mapper.scalarPage(new PageObject(4, 2));
            assertTrue(beyondEnd.getData().isEmpty());
            assertEquals(3, beyondEnd.getTotalCount());
            session.jdbc().execute("DELETE FROM user_info");
            PageResult<User> empty = mapper.beanPage(new PageObject(0, 2));
            assertTrue(empty.getData().isEmpty());
            assertEquals(0, empty.getTotalCount());
        }
    }

    public static class User {
        private Integer id;
        private String  name;

        public Integer getId() {
            return this.id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class NameMapper implements RowMapper<String> {
        @Override
        public String mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            return rs.getString("name");
        }
    }

    @SimpleMapper
    public interface PagedMapper {
        @Query("SELECT id FROM user_info ORDER BY id")
        PageResult<Integer> scalarPage(Page page);

        @Query("SELECT id FROM user_info ORDER BY id")
        PageResult<Map<String, Object>> mapPage(Page page);

        @Query("SELECT * FROM user_info ORDER BY id")
        PageResult<User> beanPage(Page page);

        @Query(value = "SELECT name FROM user_info ORDER BY id", resultRowMapper = NameMapper.class)
        PageResult<String> customPage(Page page);

        @Query("SELECT id FROM user_info ORDER BY id")
        List<Integer> list();

        @Query("SELECT id FROM user_info WHERE id=1")
        Integer scalar();
    }
}
