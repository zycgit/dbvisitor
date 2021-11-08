/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.test.db.dal.execute;
import net.hasor.db.dal.repository.Param;
import net.hasor.db.dal.repository.RefMapper;
import net.hasor.test.db.dto.AutoId;
import net.hasor.test.db.dto.TbUser2;

import java.util.List;
import java.util.Map;

/**
 *
 * @version : 2013-12-10
 * @author 赵永春 (zyc@hasor.net)
 */
@RefMapper("/net_hasor_db/dal_dynamic/execute/basic_execute.xml")
public interface TestExecuteDal {
    public int createUser(TbUser2 tbUser);

    public int initUser();

    public List<TbUser2> listUserList_1(@Param("abc") String name);

    public List<TestUser> listUserList_2(@Param("abc") String name);

    public Map<String, Object> callSelectUser(@Param("abc") String name);

    public int insertAutoID_1(AutoId autoId);

    public int insertAutoID_2(AutoId autoId);

}
