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
package net.hasor.dbvisitor.mapper.dto;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;

import java.util.List;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper()
public interface AnnoResultType2_Mapper {
    @Query("select 1")
    List<Byte> selectByte_1();

    @Query("select 1")
    List<Short> selectShort_1();

    @Query("select 1")
    List<Integer> selectInt_1();

    @Query("select 1")
    List<Long> selectLong_1();

    @Query("select 1")
    List<Float> selectFloat_1();

    @Query("select 1")
    List<Double> selectDouble_1();

    @Query("select 1")
    List<Character> selectChar_1();

    @Query("select 1")
    List<Void> selectVoid();

    @Query("select 1")
    List<String> selectString();
}
