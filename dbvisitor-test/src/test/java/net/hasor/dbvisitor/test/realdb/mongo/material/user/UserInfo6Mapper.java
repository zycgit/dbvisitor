/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.realdb.mongo.material.user;
import net.hasor.dbvisitor.mapper.*;

@SimpleMapper()
public interface UserInfo6Mapper {
    @Insert("""
        test.user_info.insert({
            "uid": #{info.userId},
            "name": #{info.userName},
            "loginName": #{info.account},
            "loginPassword": #{info.password}
        })
        """)
    int saveUser(@Param("info") UserInfo6 info);

    @Query("test.user_info.find({uid: #{uid}})")
    UserInfo6 loadUser(@Param("uid") String uid);

    @Delete("test.user_info.remove({uid: #{uid}})")
    int deleteUser(@Param("uid") String uid);
}
