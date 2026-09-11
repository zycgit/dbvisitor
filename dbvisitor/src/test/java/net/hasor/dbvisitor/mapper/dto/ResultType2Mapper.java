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
import java.util.HashMap;
import java.util.Map;
import net.hasor.dbvisitor.mapper.RefMapper;
import org.h2.value.CaseInsensitiveMap;

/**
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-12-10
 */
@RefMapper("/dbvisitor_coverage/basic_mapper/basic_result_type_2.xml")
public interface ResultType2Mapper {
    boolean selectBool_1();

    Boolean selectBool_2();

    byte[] selectBytes_1();

    byte selectByte_1();

    Byte selectByte_2();

    short selectShort_1();

    Short selectShort_2();

    int selectInt_1();

    Integer selectInt_2();

    long selectLong_1();

    Long selectLong_2();

    float selectFloat_1();

    Float selectFloat_2();

    double selectDouble_1();

    Double selectDouble_2();

    BigInteger selectBigInt_1();

    BigDecimal selectDecimal_1();

    Number selectNumber_1();

    char selectChar_1();

    Character selectChar_2();

    String selectString_1();

    URL selectUrl_1();

    URI selectUri_1();

    void selectVoid_1();

    Void selectVoid_2();

    Map selectMap_1();

    HashMap selectMap_2();

    CaseInsensitiveMap selectMap_3();

    java.util.Date selectDate_1();

    java.sql.Date selectSqlDate_1();

    java.sql.Time selectSqlTime_1();

    java.sql.Timestamp selectSqlTimestamp_1();

    java.time.OffsetDateTime selectOffsetDateTime_1();

    java.time.OffsetTime selectOffsetTime_1();

    java.time.LocalDate selectLocalDate_1();

    java.time.LocalTime selectLocalTime_1();

    java.time.LocalDateTime selectLocalDateTime_1();

    java.time.MonthDay selectMonthDay_1();

    java.time.Month selectMonth_1();

    java.time.YearMonth selectYearMonth_1();

    java.time.Year selectYear_1();
}
