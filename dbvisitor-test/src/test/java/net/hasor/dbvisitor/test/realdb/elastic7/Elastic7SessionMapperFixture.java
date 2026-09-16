/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.page.PageObject;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.SessionRefUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.SessionUserMapper;
import net.hasor.dbvisitor.test.contract.material.dao.XmlRefMapperDao;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultMappingMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.realdb.elastic7.material.ElasticMatrixFixture;

public final class Elastic7SessionMapperFixture implements AutoCloseable {
    private final ElasticMatrixFixture fixture = new ElasticMatrixFixture();
    private       String               environment;

    public JdbcTemplate open(String environment) throws SQLException {
        this.environment = environment;
        return fixture.open(environment);
    }

    public String countCommand() {
        return "POST /" + fixture.index() + "/_count";
    }

    public String index() {
        return fixture.index();
    }

    public String temporaryIndex() {
        return fixture.index() + "_temporary";
    }

    public void insert(int id, String name, Integer age, String email) throws SQLException {
        fixture.insert(id, name, age, email);
    }

    public Session session() throws Exception {
        Session session = fixture.session();
        Configuration configuration = session.getConfiguration();
        String path = "POST /" + fixture.index();
        configuration.addMacro("esSessionInsert", path + "/_doc " + "{\"id\": #{id},\"name\": #{name},\"age\": #{age},\"email\": #{email}," + "\"create_time\": " + System.currentTimeMillis() + "}");
        configuration.addMacro("esSessionIndex", fixture.index());
        configuration.addMacro("esXmlPath", path);
        configuration.addMacro("esBulkAction", "es6".equals(environment) ? "{\"index\": {\"_type\": \"_doc\"}}" : "{\"index\": {}}");
        configuration.addMacro("esSessionOne", path + "/_search {\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("esSessionAll", path + "/_search {\"size\": 100,\"sort\": [{\"id\": \"asc\"}]}");
        configuration.addMacro("esSessionCount", countCommand());
        configuration.addMacro("esSessionUpdate", path + "/_update_by_query " + "{\"query\": {\"term\": {\"id\": #{id}}},\"script\": {" + "\"source\": \"ctx._source.name=params.name; ctx._source.age=params.age\"," + "\"params\": {\"name\": #{name},\"age\": #{age}}}}");
        configuration.addMacro("esSessionEmail", path + "/_update_by_query " + "{\"query\": {\"term\": {\"id\": #{id}}},\"script\": {" + "\"source\": \"ctx._source.email=params.email\",\"params\": {\"email\": #{email}}}}");
        configuration.addMacro("esSessionDelete", path + "/_delete_by_query " + "{\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("esBoundaryInsert", "PUT /" + fixture.index() + "/_doc/${id}\\?op_type=create\\&refresh=true " + "{\"id\": #{id},\"name\": #{name},\"age\": #{age},\"email\": #{email}}");
        configuration.addMacro("esBoundaryAge", path + "/_update_by_query " + "{\"query\": {\"term\": {\"id\": #{id}}},\"script\": {" + "\"source\": \"ctx._source.age=params.age\",\"params\": {\"age\": #{age}}}}");
        configuration.addMacro("esBoundaryMissing", "POST /" + temporaryIndex() + "/_search");
        configuration.addMacro("esTempInsert", "POST /" + temporaryIndex() + "/_doc {\"id\": #{id},\"name\": #{name}}");
        configuration.addMacro("esTempSelect", "POST /" + temporaryIndex() + "/_search {\"_source\": [\"name\"],\"query\": {\"term\": {\"id\": #{id}}}}");
        configuration.addMacro("esBoundarySyntax", path + "/_search {\"query\": {\"unknown_query\": {}}}");
        configuration.addMacro("esBoundaryColumn", path + "/_search {\"sort\": [{\"missing_field\": \"asc\"}]}");
        return session;
    }

    @Override
    public void close() throws Exception {
        fixture.close();
    }

    @SimpleMapper
    public interface NativeSimple extends SessionUserMapper {
        @Override
        @Insert("@{macro, esSessionInsert}")
        int insertUser(UserInfo user);

        @Override
        @Query("@{macro, esSessionOne}")
        UserInfo selectById(@Param("id") Integer id);

        @Override
        @Query("@{macro, esSessionAll}")
        List<UserInfo> selectAll();

        @Override
        @Update("@{macro, esSessionUpdate}")
        int updateUser(UserInfo user);

        @Override
        @Delete("@{macro, esSessionDelete}")
        int deleteById(@Param("id") Integer id);

        @Override
        @Query("@{macro, esSessionCount}")
        int countAll();
    }

    @RefMapper("/mapper/elastic/SessionMatrixMapper.xml")
    public interface NativeRef extends SessionRefUserMapper {
        @Override
        default net.hasor.dbvisitor.test.contract.material.model.UserOrderDTO queryOrderWithUser(Integer orderId) {
            throw new UnsupportedOperationException("Elasticsearch does not support joining independent indices");
        }
    }

    @RefMapper("/mapper/elastic/RefMatrix.xml")
    public interface NativeRefDao extends XmlRefMapperDao {
    }

    @SimpleMapper
    public interface NativeResultMapping extends ResultMappingMapper {
        @Override
        @Insert("@{macro, esSessionInsert}")
        int insertUser(UserInfo user);

        @Override
        @Query("@{macro, esSessionOne}")
        UserInfo selectUserById(@Param("id") Integer id);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"id\",\"name\"],\"query\": {\"term\": {\"id\": #{id}}}}")
        UserInfo selectUserPartial(@Param("id") Integer id);

        @Override
        @Query("@{macro, esSessionOne}")
        Map<String, Object> selectUserAsMap(@Param("id") Integer id);

        @Override
        @Query("@{macro, esSessionAll}")
        List<Map<String, Object>> selectUsersAsMapList();

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"age\"],\"query\": {\"term\": {\"id\": #{id}}}}")
        Integer selectAgeById(@Param("id") Integer id);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"name\"],\"query\": {\"term\": {\"id\": #{id}}}}")
        String selectNameById(@Param("id") Integer id);

        @Override
        @Query("@{macro, esSessionCount}")
        Long selectCount();

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"create_time\"],\"query\": {\"term\": {\"id\": #{id}}}}")
        Date selectCreateTimeById(@Param("id") Integer id);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"range\": {\"age\": {\"gte\": #{minAge},\"lte\": #{maxAge}}}}}")
        List<UserInfo> selectUsersByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"name\"],\"size\": 100,\"query\": {\"range\": {\"id\": {\"gte\": #{minId},\"lte\": #{maxId}}}}}")
        List<String> selectAllNames(@Param("minId") Integer minId, @Param("maxId") Integer maxId);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"_source\": [\"id\"],\"size\": 100,\"query\": {\"range\": {\"id\": {\"gte\": #{minId},\"lte\": #{maxId}}}}}")
        List<Integer> selectIdRange(@Param("minId") Integer minId, @Param("maxId") Integer maxId);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}")
        List<UserInfo> selectUsersWithPagination(@Param("pattern") String pattern, PageObject page);
    }

    @SimpleMapper
    public interface NativeBoundary extends AnnotationTestMapper {
        @Override
        @Insert({ "POST /@{macro,esSessionIndex}/_doc", "{\"id\": #{id},\"name\": #{name},", "\"age\": #{age},\"email\": #{email}}" })
        int insertUserMultiLine(UserInfo user);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"query\": {\"term\": {\"age\": #{age}}},\"size\": 100}")
        List<UserInfo> selectByAge(@Param("age") Integer age);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_search {\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"size\": 100}")
        List<UserInfo> selectByNameLike(@Param("pattern") String pattern);

        @Override
        @Query("POST /@{macro,esSessionIndex}/_count {\"query\": {\"term\": {\"age\": #{age}}}}")
        int countByAge(@Param("age") Integer age);

        @Override
        @Delete("POST /@{macro,esSessionIndex}/_delete_by_query {\"query\": {\"term\": {\"age\": #{age}}}}")
        int deleteByAge(@Param("age") Integer age);

        @Override
        @Insert("@{macro, esTempInsert}")
        void insertTempData(@Param("id") Integer id, @Param("name") String name);

        @Override
        @Insert("@{macro, esBoundaryInsert}")
        int insertUserWithParams(@Param("id") Integer id, @Param("name") String name, @Param("age") Integer age, @Param("email") String email);

        @Override
        @Query("@{macro, esSessionOne}")
        UserInfo selectById(@Param("id") Integer id);

        @Override
        @Update("@{macro, esBoundaryAge}")
        int updateUserAge(@Param("id") Integer id, @Param("age") Integer age);

        @Override
        @Delete("@{macro, esSessionDelete}")
        int deleteById(@Param("id") Integer id);

        @Override
        @Query("@{macro, esBoundarySyntax}")
        UserInfo selectWithSyntaxError();

        @Override
        @Query("@{macro, esBoundaryMissing}")
        UserInfo selectFromNonExistentTable();

        @Override
        @Query("@{macro, esBoundaryColumn}")
        String selectNonExistentColumn();

        @Override
        @Query("@{macro, esTempSelect}")
        String selectTempData(@Param("id") Integer id);
    }
}
