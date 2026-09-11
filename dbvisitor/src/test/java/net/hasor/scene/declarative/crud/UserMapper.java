/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.scene.declarative.crud;

import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.scene.declarative.crud.dto.UserTable;

@SimpleMapper
public interface UserMapper {
    @Insert({ "insert into user_info (user_uuid, user_name, login_name, login_password, email, seq, register_time)",//
            "values (#{userUuid}, #{name}, #{loginName}, #{loginPassword}, #{email}, #{seq}, #{registerTime})" })
    int createUser(UserTable tbUser);
}
