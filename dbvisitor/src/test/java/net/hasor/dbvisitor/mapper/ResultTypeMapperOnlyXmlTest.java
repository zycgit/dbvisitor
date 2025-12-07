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
package net.hasor.dbvisitor.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.mapper.def.QueryType;
import org.junit.Test;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
public class ResultTypeMapperOnlyXmlTest {
    @Test
    public void selectBool_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectBool_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == boolean.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectBool_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectBool_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == boolean.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectBytes_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectBytes_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == byte[].class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectBool_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectBool_3");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Boolean.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectByte_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectByte_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == byte.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectByte_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectByte_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Byte.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectShort_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectShort_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == short.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectShort_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectShort_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Short.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectInt_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectInt_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == int.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectInt_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectInt_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Integer.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLong_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLong_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == long.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLong_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLong_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Long.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectFloat_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectFloat_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == float.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectFloat_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectFloat_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Float.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDouble_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectDouble_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == double.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDouble_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectDouble_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Double.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectBigInt_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectBigInt_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == BigInteger.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDecimal_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectDecimal_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == BigDecimal.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectNumber_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectNumber_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Number.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectNumber_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectNumber_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Number.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectChar_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectChar_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == char.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectChar_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectChar_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Character.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectString_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectString_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == String.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectString_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectString_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == String.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectUrl_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectUrl_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == URL.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectUri_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectUri_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == URI.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectVoid_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectVoid_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == null;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectVoid_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectVoid_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == void.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectVoid_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectVoid_3");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Void.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMap_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMap_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == Map.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMap_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMap_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == HashMap.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMap_3() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMap_3");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == LinkedCaseInsensitiveMap.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDate_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectDate_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.util.Date.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectDate_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectDate_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.util.Date.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlDate_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlDate_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Date.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlDate_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlDate_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Date.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlTime_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlTime_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Time.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlTime_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlTime_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Time.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlTimestamp_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlTimestamp_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Timestamp.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectSqlTimestamp_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectSqlTimestamp_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.sql.Timestamp.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectOffsetDateTime_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectOffsetDateTime_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.OffsetDateTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectOffsetDateTime_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectOffsetDateTime_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.OffsetDateTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectOffsetTime_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectOffsetTime_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.OffsetTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectOffsetTime_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectOffsetTime_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.OffsetTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalDate_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalDate_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalDate.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalDate_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalDate_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalDate.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalTime_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalTime_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalTime_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalTime_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalDateTime_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalDateTime_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalDateTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectLocalDateTime_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectLocalDateTime_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.LocalDateTime.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMonthDay_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMonthDay_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.MonthDay.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMonthDay_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMonthDay_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.MonthDay.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMonth_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMonth_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.Month.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectMonth_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectMonth_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.Month.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectYearMonth_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectYearMonth_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.YearMonth.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectYearMonth_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectYearMonth_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.YearMonth.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectYear_1() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectYear_1");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.Year.class;
        assert def.getConfig().getType() == QueryType.Select;
    }

    @Test
    public void selectYear_2() throws Exception {
        MapperRegistry registry = new MapperRegistry();
        registry.loadMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_only_xml.xml");

        StatementDef def = registry.findStatement("mapper_types", "selectYear_2");
        assert def != null;
        assert def.getConfigNamespace().equals("mapper_types");
        assert def.getResultType() == java.time.Year.class;
        assert def.getConfig().getType() == QueryType.Select;
    }
}
