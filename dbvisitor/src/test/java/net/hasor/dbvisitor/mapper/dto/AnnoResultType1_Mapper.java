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

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2013-12-10
 */
@SimpleMapper()
public interface AnnoResultType1_Mapper {
    @Query("select 1")
    byte selectByte_1();

    @Query("select 1")
    Byte selectByte_2();

    @Query("select 1")
    short selectShort_1();

    @Query("select 1")
    Short selectShort_2();

    @Query("select 1")
    int selectInt_1();

    @Query("select 1")
    Integer selectInt_2();

    @Query("select 1")
    long selectLong_1();

    @Query("select 1")
    Long selectLong_2();

    @Query("select 1")
    float selectFloat_1();

    @Query("select 1")
    Float selectFloat_2();

    @Query("select 1")
    double selectDouble_1();

    @Query("select 1")
    Double selectDouble_2();

    @Query("select 1")
    char selectChar_1();

    @Query("select 1")
    Character selectChar_2();

    @Query("select 1")
    void selectVoid();

    @Query("select 1")
    String selectString();
}
