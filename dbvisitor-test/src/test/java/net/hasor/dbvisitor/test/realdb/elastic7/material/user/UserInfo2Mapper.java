/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.elastic7.material.user;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo2Mapper {
    @Insert("POST /user_info/_doc #{info}")
    int saveUser(@Param("info") UserInfo2 info);

    @Query("POST /user_info/_search {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    UserInfo2 loadUser(@Param("uid") String uid);

    @Delete("POST /user_info/_delete_by_query {\"query\": {\"match\": {\"uid\": #{uid}}}}")
    int deleteUser(@Param("uid") String uid);
}
