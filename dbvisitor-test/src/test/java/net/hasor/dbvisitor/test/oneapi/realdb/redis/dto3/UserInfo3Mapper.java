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
package net.hasor.dbvisitor.test.oneapi.realdb.redis.dto3;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.test.oneapi.realdb.redis.dto1.UserInfo1;

/**
 * UserInfo3 类型上没有任何注释，通过在 Mapper XML 中描述序列化和反序列化。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("oneapi/realdb/redis/user-mapper-3.xml")
public interface UserInfo3Mapper {
    int saveUser(@Param("info") UserInfo3 info);

    UserInfo3 loadUser1(@Param("uid") String uid);

    UserInfo1 loadUser2(@Param("uid") String uid);

    int deleteUser(@Param("uid") String uid);
}
