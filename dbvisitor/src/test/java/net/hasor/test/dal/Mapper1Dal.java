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
package net.hasor.test.dal;
import net.hasor.dbvisitor.dal.repository.RefMapper;
import net.hasor.test.dto.UserInfo2;

import java.util.List;

/**
 *
 * @version : 2013-12-10
 * @author 赵永春 (zyc@hasor.net)
 */
@RefMapper("/dbvisitor_coverage/dal_dynamic/mapper/mapper_1.xml")
public interface Mapper1Dal {
    List<UserInfo2> testBind(String abc);

    List<UserInfo2> testChoose(String title, String content);

    List<UserInfo2> testForeach(List<String> eventTypes);

    List<UserInfo2> testIf(String ownerID, String ownerType);

    List<UserInfo2> testInsert(String uid, String name);
}
