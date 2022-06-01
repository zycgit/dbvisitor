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
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.test.db.dto.TbUser2;

import java.util.List;

/**
 *
 * @version : 2013-12-10
 * @author 赵永春 (zyc@hasor.net)
 */
@RefMapper("/net_hasor_db/dal_dynamic/execute/page_execute.xml")
public interface PageExecuteDal extends BaseMapper<TbUser2> {
    public void deleteAll();

    public int createUser(TbUser2 tbUser);

    public List<TbUser2> listByPage1(@Param("name") String name, Page pageInfo);

    public PageResult<TbUser2> listByPage2(@Param("name") String name, Page pageInfo);
}
