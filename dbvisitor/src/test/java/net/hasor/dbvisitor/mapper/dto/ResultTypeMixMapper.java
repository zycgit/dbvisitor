/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import net.hasor.dbvisitor.mapper.RefMapper;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_mix.xml")
public interface ResultTypeMixMapper {
    boolean selectBool_1();

    Boolean selectBool_2();

    String selectBool_3();

    Short selectShort_1();

    String selectShort_2();

    java.util.Date selectDate_1();

    java.util.Date selectDate_2();

    java.util.Date selectDate_3();

}
