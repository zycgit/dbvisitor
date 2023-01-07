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
package net.hasor.test.dal.execute;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.test.dto.AutoId;
import net.hasor.test.dto.TbUser2;

import java.util.List;
import java.util.Map;

/**
 *
 * @version : 2013-12-10
 * @author 赵永春 (zyc@hasor.net)
 */
@RefMapper("/dbvisitor_coverage/dal_session/basic_execute.xml")
public interface TestExecuteDal {
    int createUser(TbUser2 tbUser);

    int initUser();

    List<TbUser2> listUserList_1(@Param("abc") String name);

    List<TestUser> listUserList_2(@Param("abc") String name);

    Map<String, Object> callSelectUser(Map<String, Object> args);

    int insertAutoID_1(AutoId autoId);

    int insertAutoID_2(AutoId autoId);
}
