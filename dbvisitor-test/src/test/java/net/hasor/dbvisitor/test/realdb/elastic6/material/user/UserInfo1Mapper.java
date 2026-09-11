/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic6.material.user;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo1Mapper {
    @Insert("POST /user_info/_doc #{info}")
    int saveUser(@Param("info") UserInfo1b info);

    @Update("""
            POST /user_info/_update_by_query {
              "query": {"match": {"uid": #{uid}}},
              "script": {
                "source": "ctx._source.name = params.name",
                "params": {"name": #{name}}
              }
            }
            """)
    int updateName(@Param("uid") String uid, @Param("name") String name);

    @Update("""
            POST /user_info/_update_by_query {
              "query": {"match": {"uid": #{uid}}},
              "script": {
                "source": "ctx._source.name = params.name; ctx._source.loginName = params.loginName",
                "params": {"name": #{name}, "loginName": #{loginName}}
              }
            }
            """)
    int updateUser(@Param("uid") String uid, @Param("name") String name, @Param("loginName") String loginName);

    @Query("POST /user_info/_search {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    UserInfo1b loadUser(@Param("uid") String uid);

    @Delete("POST /user_info/_delete_by_query {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    int deleteUser(@Param("uid") String uid);
}
