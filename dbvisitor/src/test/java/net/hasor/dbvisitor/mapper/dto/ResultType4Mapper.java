/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper.dto;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.RefMapper;
import org.h2.value.CaseInsensitiveMap;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_4.xml")
public interface ResultType4Mapper {

    List<Boolean> selectBool_1();

    List<byte[]> selectBytes_1();

    List<Byte> selectByte_1();

    List<Short> selectShort_1();

    List<Integer> selectInt_1();

    List<Long> selectLong_1();

    List<Float> selectFloat_1();

    List<Double> selectDouble_1();

    List<BigInteger> selectBigInt_1();

    List<BigDecimal> selectDecimal_1();

    List<Number> selectNumber_1();

    List<Character> selectChar_1();

    List<String> selectString_1();

    List<URL> selectUrl_1();

    List<URI> selectUri_1();

    List<Void> selectVoid_1();

    List<Map> selectMap_1();

    List<HashMap> selectMap_2();

    List<CaseInsensitiveMap> selectMap_3();

    List<java.util.Date> selectDate_1();

    List<java.sql.Date> selectSqlDate_1();

    List<java.sql.Time> selectSqlTime_1();

    List<java.sql.Timestamp> selectSqlTimestamp_1();

    List<java.time.OffsetDateTime> selectOffsetDateTime_1();

    List<java.time.OffsetTime> selectOffsetTime_1();

    List<LocalDate> selectLocalDate_1();

    List<java.time.LocalTime> selectLocalTime_1();

    List<java.time.LocalDateTime> selectLocalDateTime_1();

    List<java.time.MonthDay> selectMonthDay_1();

    List<java.time.Month> selectMonth_1();

    List<java.time.YearMonth> selectYearMonth_1();

    List<java.time.Year> selectYear_1();
}
