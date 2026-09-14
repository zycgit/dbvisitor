/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7;

import java.util.List;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.ResultSetType;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.mapper.StatementType;
import net.hasor.dbvisitor.mapper.Update;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationAttributesMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;

@SimpleMapper
public interface Elastic7AttributeMapper extends AnnotationAttributesMapper {
    @Override
    @Query(value = "@{macro, esSessionOne}", statementType = StatementType.Prepared)
    UserInfo selectByIdPrepared(@Param("id") Integer id);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"query\": {\"term\": {\"id\": ${id}}}}", statementType = StatementType.Statement)
    UserInfo selectByIdStatement(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, esSessionOne}", timeout = -1)
    UserInfo selectByIdDefaultTimeout(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, esSessionOne}", timeout = 30)
    UserInfo selectByIdWithTimeout(@Param("id") Integer id);

    @Override
    @Query(value = "@{macro, esSessionOne}", timeout = 3600)
    UserInfo selectWithMaxTimeout(@Param("id") Integer id);

    @Override
    @Update(value = "@{macro, esBoundaryAge}", timeout = 30)
    int updateWithTimeout(@Param("id") Integer id, @Param("age") Integer age);

    @Override
    @Delete(value = "@{macro, esSessionDelete}", timeout = 30)
    int deleteWithTimeout(@Param("id") Integer id);

    @Override
    @Insert(value = "@{macro, esSessionInsert}", timeout = 30)
    int insertWithTimeout(UserInfo user);

    @Override
    @Insert({ "POST /@{macro,esSessionIndex}/_doc", "{\"id\": #{id},\"name\": #{name},",
            "\"age\": #{age},\"email\": #{email},\"create_time\": #{createTime}}" })
    int insertMultiLine(UserInfo user);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", fetchSize = 256)
    List<UserInfo> selectWithDefaultFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", fetchSize = 10)
    List<UserInfo> selectWithSmallFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", fetchSize = 1000)
    List<UserInfo> selectWithLargeFetchSize(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", fetchSize = 1)
    List<UserInfo> selectWithFetchSizeOne(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", resultSetType = ResultSetType.DEFAULT)
    List<UserInfo> selectWithDefaultResultSetType(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", resultSetType = ResultSetType.FORWARD_ONLY)
    List<UserInfo> selectWithForwardOnly(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", resultSetType = ResultSetType.SCROLL_INSENSITIVE)
    List<UserInfo> selectWithScrollInsensitive(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", resultSetType = ResultSetType.SCROLL_SENSITIVE)
    List<UserInfo> selectWithScrollSensitive(@Param("pattern") String pattern);

    @Override
    @Query(value = "POST /@{macro,esSessionIndex}/_search {\"size\": 100,\"query\": {\"wildcard\": {\"name\": #{pattern.replace('%', '*')}}},\"sort\": [{\"id\": \"asc\"}]}", statementType = StatementType.Prepared, timeout = 60, fetchSize = 100, resultSetType = ResultSetType.FORWARD_ONLY)
    List<UserInfo> selectWithCombinedAttributes(@Param("pattern") String pattern);

    @Override
    @Query(value = "@{macro, esSessionOne}")
    UserInfo selectWithAllDefaults(@Param("id") Integer id);
}
