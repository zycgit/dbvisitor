/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.test.realdb.redis.dto2;

import net.hasor.dbvisitor.mapper.*;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

/**
 * UserInfo2 类型上没有任何注释，通过在 Mapper 自身描述序列化和反序列化。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@SimpleMapper()
public interface UserInfo2Mapper {
    @Insert(value = "set #{'user_' + uid} #{info, typeHandler=net.hasor.dbvisitor.types.handler.json.JsonTypeHandler}")
    int saveUser(@Param("info") UserInfo2 info);

    @Query(value = "get #{'user_' + uid}", resultTypeHandler = JsonTypeHandler.class)
    UserInfo2 loadUser(@Param("uid") String uid);

    @Delete(value = "del #{'user_' + uid}")
    int deleteUser(@Param("uid") String uid);
}
