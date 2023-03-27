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
package net.hasor.dbvisitor.generate.provider;

import net.hasor.cobble.StringUtils;
import net.hasor.cobble.convert.ConverterUtils;
import net.hasor.dbvisitor.generate.CaseSensitivityType;
import net.hasor.dbvisitor.generate.MetadataProvider;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.Date;
import java.util.*;

/**
 * MetadataSupplier 系列的公共类。
 * @version : 2020-01-22
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractMetadataProvider implements MetadataProvider {
    protected final JdbcTemplate        jdbcTemplate;
    protected       CaseSensitivityType plainCaseSensitivityType;
    protected       CaseSensitivityType delimitedCaseSensitivityType;

    /**
     * Connection will be proxy, Calling the close method in an AbstractMetadatasupplier subclass will be invalid.
     */
    public AbstractMetadataProvider(Connection connection) {
        this.jdbcTemplate = new JdbcTemplate(() -> connection);
    }

    /**
     * Each time data is requested in the AbstractMetadatasupplier subclass a new Connection is created and then closed.
     */
    public AbstractMetadataProvider(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public CaseSensitivityType getPlain() throws SQLException {
        if (this.plainCaseSensitivityType == null) {
            this.plainCaseSensitivityType = this.jdbcTemplate.execute((ConnectionCallback<CaseSensitivityType>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                if (metaData.supportsMixedCaseIdentifiers()) {
                    return CaseSensitivityType.Exact;
                } else if (metaData.storesUpperCaseIdentifiers()) {
                    return CaseSensitivityType.Upper;
                } else if (metaData.storesLowerCaseIdentifiers()) {
                    return CaseSensitivityType.Lower;
                } else if (metaData.storesMixedCaseIdentifiers()) {
                    return CaseSensitivityType.Fuzzy;
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return this.plainCaseSensitivityType;
    }

    @Override
    public CaseSensitivityType getDelimited() throws SQLException {
        if (this.delimitedCaseSensitivityType == null) {
            this.delimitedCaseSensitivityType = this.jdbcTemplate.execute((ConnectionCallback<CaseSensitivityType>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                if (metaData.supportsMixedCaseQuotedIdentifiers()) {
                    return CaseSensitivityType.Exact;
                } else if (metaData.storesUpperCaseQuotedIdentifiers()) {
                    return CaseSensitivityType.Upper;
                } else if (metaData.storesLowerCaseQuotedIdentifiers()) {
                    return CaseSensitivityType.Lower;
                } else if (metaData.storesMixedCaseQuotedIdentifiers()) {
                    return CaseSensitivityType.Fuzzy;
                } else {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return this.delimitedCaseSensitivityType;
    }

    protected Map<String, Integer> extractColumn(ResultSetMetaData resultSetMetaData) throws SQLException {
        Map<String, Integer> columnInfo = new LinkedHashMap<>();
        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String name = resultSetMetaData.getColumnLabel(i);
            if (name == null || name.length() < 1) {
                name = resultSetMetaData.getColumnName(i);
            }
            columnInfo.put(name, i);
        }
        return columnInfo;
    }

    protected Integer tryWasNull(int value, ResultSet record) throws SQLException {
        if (record.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    protected Boolean tryWasNull(boolean value, ResultSet record) throws SQLException {
        if (record.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    protected Long tryWasNull(long value, ResultSet record) throws SQLException {
        if (record.wasNull()) {
            return null;
        } else {
            return value;
        }
    }

    protected Integer tryWasNull(short value, ResultSet record) throws SQLException {
        if (record.wasNull()) {
            return null;
        } else {
            return (int) value;
        }
    }

    protected static String safeToString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    protected static Integer safeToInteger(Object obj) {
        return (obj == null) ? null : (Integer) ConverterUtils.convert(Integer.class, obj);
    }

    protected static int safeToInt(Object obj) {
        return (obj == null) ? 0 : (Integer) ConverterUtils.convert(Integer.TYPE, obj);
    }

    protected static Long safeToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return (Long) ConverterUtils.convert(Long.class, obj);
    }

    protected static Boolean safeToBoolean(Object obj) {
        return (obj == null) ? null : (Boolean) ConverterUtils.convert(Boolean.class, obj);
    }

    protected static Date safeToDate(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Date) {
            return (Date) obj;
        } else if (obj instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = ((ZonedDateTime) obj);
            return Timestamp.from(zonedDateTime.toInstant());
        } else if (obj instanceof OffsetDateTime) {
            ZonedDateTime zonedDateTime = ((OffsetDateTime) obj).atZoneSameInstant(ZoneOffset.systemDefault());
            return Timestamp.from(zonedDateTime.toInstant());
        } else if (obj instanceof OffsetTime) {
            ZonedDateTime zonedDateTime = ((OffsetTime) obj).atDate(LocalDate.ofEpochDay(0)).atZoneSameInstant(ZoneOffset.UTC);
            return Timestamp.from(zonedDateTime.toInstant());
        } else if (obj instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) obj);
        } else if (obj instanceof LocalDate) {
            LocalDateTime dateTime = LocalDateTime.of((LocalDate) obj, LocalTime.of(0, 0, 0, 0));
            return Timestamp.valueOf(dateTime);
        } else if (obj instanceof Number) {
            return new Date(((Number) obj).longValue());
        } else {
            throw new ClassCastException(obj.getClass() + " Type cannot be converted to Date");
        }
    }

    protected static String buildWhereIn(Collection<?> paramMap) {
        StringBuilder whereIn = new StringBuilder();
        whereIn.append("(");
        whereIn.append(StringUtils.repeat("?,", paramMap.size()));
        whereIn.deleteCharAt(whereIn.length() - 1);
        whereIn.append(")");
        return whereIn.toString();
    }

    protected static List<String> stringArray2List(List<String> stringArray) {
        if (stringArray == null || stringArray.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> stringList = new ArrayList<>();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string)) {
                stringList.add(string);
            }
        }
        return stringList;
    }
}
