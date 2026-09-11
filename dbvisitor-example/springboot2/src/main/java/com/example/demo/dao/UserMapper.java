/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package com.example.demo.dao;
import com.example.demo.dto.UserDTO;
import net.hasor.dbvisitor.mapper.Mapper;

import java.util.List;

public interface UserMapper extends Mapper {

    List<UserDTO> queryAll();
}
