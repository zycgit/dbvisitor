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
package net.hasor.db.types;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.reflect.TypeReference;
import net.hasor.db.types.handler.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.chrono.JapaneseDate;
import java.util.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDBC 4.2 full  compatible
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public final class TypeHandlerRegistry {
    private static final Map<Class<? extends TypeHandler<?>>, TypeHandler<?>> cachedSingleHandlers  = new ConcurrentHashMap<>();
    private static final Map<String, Integer>                                 javaTypeToJdbcTypeMap = new ConcurrentHashMap<>();
    private static final Map<Integer, Class<?>>                               jdbcTypeToJavaTypeMap = new ConcurrentHashMap<>();

    public static final TypeHandlerRegistry                       DEFAULT            = new TypeHandlerRegistry();
    private final       UnknownTypeHandler                        defaultTypeHandler = new UnknownTypeHandler(this);
    // mappings
    private final       Map<String, TypeHandler<?>>               javaTypeHandlerMap = new ConcurrentHashMap<>();
    private final       Map<Integer, TypeHandler<?>>              jdbcTypeHandlerMap = new ConcurrentHashMap<>();
    private final       Map<String, Map<Integer, TypeHandler<?>>> typeHandlerMap     = new ConcurrentHashMap<>();

    static {
        // primitive and wrapper
        javaTypeToJdbcTypeMap.put(Boolean.class.getName(), JDBCType.BIT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(boolean.class.getName(), JDBCType.BIT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Byte.class.getName(), JDBCType.TINYINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(byte.class.getName(), JDBCType.TINYINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Short.class.getName(), JDBCType.SMALLINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(short.class.getName(), JDBCType.SMALLINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Integer.class.getName(), JDBCType.INTEGER.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(int.class.getName(), JDBCType.INTEGER.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Long.class.getName(), JDBCType.BIGINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(long.class.getName(), JDBCType.BIGINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Float.class.getName(), JDBCType.FLOAT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(float.class.getName(), JDBCType.FLOAT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Double.class.getName(), JDBCType.DOUBLE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(double.class.getName(), JDBCType.DOUBLE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Character.class.getName(), JDBCType.CHAR.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(char.class.getName(), JDBCType.CHAR.getVendorTypeNumber());
        // java time
        javaTypeToJdbcTypeMap.put(Date.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(java.sql.Date.class.getName(), JDBCType.DATE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(java.sql.Timestamp.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(java.sql.Time.class.getName(), JDBCType.TIME.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Instant.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(LocalDateTime.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(LocalDate.class.getName(), JDBCType.DATE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(LocalTime.class.getName(), JDBCType.TIME.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(ZonedDateTime.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(JapaneseDate.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(YearMonth.class.getName(), JDBCType.VARCHAR.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Year.class.getName(), JDBCType.SMALLINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Month.class.getName(), JDBCType.SMALLINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(OffsetDateTime.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(OffsetTime.class.getName(), JDBCType.TIMESTAMP.getVendorTypeNumber());
        // java extensions Types
        javaTypeToJdbcTypeMap.put(String.class.getName(), JDBCType.VARCHAR.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(BigInteger.class.getName(), JDBCType.BIGINT.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(BigDecimal.class.getName(), JDBCType.DECIMAL.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Reader.class.getName(), JDBCType.CLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(InputStream.class.getName(), JDBCType.BLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(URL.class.getName(), JDBCType.DATALINK.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Byte[].class.getName(), JDBCType.VARBINARY.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(byte[].class.getName(), JDBCType.VARBINARY.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Object[].class.getName(), JDBCType.ARRAY.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put(Object.class.getName(), JDBCType.JAVA_OBJECT.getVendorTypeNumber());
        // oracle types
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleBlob", JDBCType.BLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleClob", JDBCType.CLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleNClob", JDBCType.NCLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.DATE", JDBCType.DATE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMP", JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPTZ", JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPLTZ", JDBCType.TIMESTAMP.getVendorTypeNumber());
    }

    public TypeHandlerRegistry() {
        // primitive and wrapper
        this.register(Boolean.class, createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(boolean.class, createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(Byte.class, createSingleTypeHandler(ByteTypeHandler.class));
        this.register(byte.class, createSingleTypeHandler(ByteTypeHandler.class));
        this.register(Short.class, createSingleTypeHandler(ShortTypeHandler.class));
        this.register(short.class, createSingleTypeHandler(ShortTypeHandler.class));
        this.register(Integer.class, createSingleTypeHandler(IntegerTypeHandler.class));
        this.register(int.class, createSingleTypeHandler(IntegerTypeHandler.class));
        this.register(Long.class, createSingleTypeHandler(LongTypeHandler.class));
        this.register(long.class, createSingleTypeHandler(LongTypeHandler.class));
        this.register(Float.class, createSingleTypeHandler(FloatTypeHandler.class));
        this.register(float.class, createSingleTypeHandler(FloatTypeHandler.class));
        this.register(Double.class, createSingleTypeHandler(DoubleTypeHandler.class));
        this.register(double.class, createSingleTypeHandler(DoubleTypeHandler.class));
        this.register(Character.class, createSingleTypeHandler(CharacterTypeHandler.class));
        this.register(char.class, createSingleTypeHandler(CharacterTypeHandler.class));
        // java time
        this.register(Date.class, createSingleTypeHandler(DateTypeHandler.class));
        this.register(java.sql.Date.class, createSingleTypeHandler(SqlDateTypeHandler.class));
        this.register(java.sql.Timestamp.class, createSingleTypeHandler(SqlTimestampTypeHandler.class));
        this.register(java.sql.Time.class, createSingleTypeHandler(SqlTimeTypeHandler.class));
        this.register(Instant.class, createSingleTypeHandler(InstantTypeHandler.class));
        this.register(JapaneseDate.class, createSingleTypeHandler(JapaneseDateTypeHandler.class));
        this.register(Year.class, createSingleTypeHandler(YearOfTimeTypeHandler.class));
        this.register(Month.class, createSingleTypeHandler(MonthOfTimeTypeHandler.class));
        this.register(YearMonth.class, createSingleTypeHandler(YearMonthOfTimeTypeHandler.class));
        this.register(MonthDay.class, createSingleTypeHandler(MonthDayOfTimeTypeHandler.class));
        this.register(LocalDate.class, createSingleTypeHandler(LocalDateTypeHandler.class));
        this.register(LocalTime.class, createSingleTypeHandler(LocalTimeTypeHandler.class));
        this.register(LocalDateTime.class, createSingleTypeHandler(LocalDateTimeTypeHandler.class));
        this.register(ZonedDateTime.class, createSingleTypeHandler(ZonedDateTimeTypeHandler.class));
        this.register(OffsetDateTime.class, createSingleTypeHandler(OffsetDateTimeForUTCTypeHandler.class));
        this.register(OffsetTime.class, createSingleTypeHandler(OffsetTimeForUTCTypeHandler.class));
        // java extensions Types
        this.register(String.class, createSingleTypeHandler(StringTypeHandler.class));
        this.register(BigInteger.class, createSingleTypeHandler(BigIntegerTypeHandler.class));
        this.register(BigDecimal.class, createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(Reader.class, createSingleTypeHandler(StringReaderTypeHandler.class));
        this.register(InputStream.class, createSingleTypeHandler(BytesInputStreamTypeHandler.class));
        this.register(Byte[].class, createSingleTypeHandler(BytesForWrapTypeHandler.class));
        this.register(byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.register(Object[].class, createSingleTypeHandler(ArrayTypeHandler.class));
        this.register(Object.class, createSingleTypeHandler(UnknownTypeHandler.class));
        this.register(Number.class, createSingleTypeHandler(NumberTypeHandler.class));
        this.register(Clob.class, createSingleTypeHandler(ClobTypeHandler.class));
        this.register(NClob.class, createSingleTypeHandler(NClobTypeHandler.class));
        this.register(Blob.class, createSingleTypeHandler(BlobBytesTypeHandler.class));

        this.register(JDBCType.BIT.getVendorTypeNumber(), createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(JDBCType.BOOLEAN.getVendorTypeNumber(), createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(JDBCType.TINYINT.getVendorTypeNumber(), createSingleTypeHandler(ByteTypeHandler.class));
        this.register(JDBCType.SMALLINT.getVendorTypeNumber(), createSingleTypeHandler(ShortTypeHandler.class));
        this.register(JDBCType.INTEGER.getVendorTypeNumber(), createSingleTypeHandler(IntegerTypeHandler.class));
        this.register(JDBCType.BIGINT.getVendorTypeNumber(), createSingleTypeHandler(LongTypeHandler.class));
        this.register(JDBCType.FLOAT.getVendorTypeNumber(), createSingleTypeHandler(FloatTypeHandler.class));
        this.register(JDBCType.DOUBLE.getVendorTypeNumber(), createSingleTypeHandler(DoubleTypeHandler.class));
        this.register(JDBCType.REAL.getVendorTypeNumber(), createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(JDBCType.NUMERIC.getVendorTypeNumber(), createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(JDBCType.DECIMAL.getVendorTypeNumber(), createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(JDBCType.CHAR.getVendorTypeNumber(), createSingleTypeHandler(CharacterTypeHandler.class));
        this.register(JDBCType.NCHAR.getVendorTypeNumber(), createSingleTypeHandler(NCharacterTypeHandler.class));
        this.register(JDBCType.CLOB.getVendorTypeNumber(), createSingleTypeHandler(ClobTypeHandler.class));
        this.register(JDBCType.VARCHAR.getVendorTypeNumber(), createSingleTypeHandler(StringTypeHandler.class));
        this.register(JDBCType.LONGVARCHAR.getVendorTypeNumber(), createSingleTypeHandler(StringTypeHandler.class));
        this.register(JDBCType.NCLOB.getVendorTypeNumber(), createSingleTypeHandler(NClobTypeHandler.class));
        this.register(JDBCType.NVARCHAR.getVendorTypeNumber(), createSingleTypeHandler(NStringTypeHandler.class));
        this.register(JDBCType.LONGNVARCHAR.getVendorTypeNumber(), createSingleTypeHandler(NStringTypeHandler.class));
        this.register(JDBCType.TIMESTAMP.getVendorTypeNumber(), createSingleTypeHandler(DateTypeHandler.class));
        this.register(JDBCType.DATE.getVendorTypeNumber(), createSingleTypeHandler(DateOnlyTypeHandler.class));
        this.register(JDBCType.TIME.getVendorTypeNumber(), createSingleTypeHandler(TimeOnlyTypeHandler.class));
        this.register(JDBCType.TIME_WITH_TIMEZONE.getVendorTypeNumber(), createSingleTypeHandler(OffsetTimeForSqlTypeHandler.class));
        this.register(JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), createSingleTypeHandler(OffsetDateTimeForSqlTypeHandler.class));
        this.register(JDBCType.SQLXML.getVendorTypeNumber(), createSingleTypeHandler(SqlXmlTypeHandler.class));
        this.register(JDBCType.BINARY.getVendorTypeNumber(), createSingleTypeHandler(BytesTypeHandler.class));
        this.register(JDBCType.VARBINARY.getVendorTypeNumber(), createSingleTypeHandler(BytesTypeHandler.class));
        this.register(JDBCType.BLOB.getVendorTypeNumber(), createSingleTypeHandler(BlobBytesTypeHandler.class));
        this.register(JDBCType.LONGVARBINARY.getVendorTypeNumber(), createSingleTypeHandler(BytesTypeHandler.class));
        this.register(JDBCType.JAVA_OBJECT.getVendorTypeNumber(), createSingleTypeHandler(ObjectTypeHandler.class));
        this.register(JDBCType.ARRAY.getVendorTypeNumber(), createSingleTypeHandler(ArrayTypeHandler.class));
        // DATALINK(Types.DATALINK)
        // DISTINCT(Types.DISTINCT),
        // STRUCT(Types.STRUCT),
        // REF(Types.REF),
        // ROWID(Types.ROWID),
        // REF_CURSOR(Types.REF_CURSOR),
        this.register(JDBCType.OTHER.getVendorTypeNumber(), createSingleTypeHandler(UnknownTypeHandler.class));

        this.registerCrossChars(MonthDay.class, createSingleTypeHandler(MonthDayOfStringTypeHandler.class));
        this.registerCrossNChars(MonthDay.class, createSingleTypeHandler(MonthDayOfStringTypeHandler.class));
        this.registerCrossNumber(MonthDay.class, createSingleTypeHandler(MonthDayOfNumberTypeHandler.class));
        this.registerCrossChars(YearMonth.class, createSingleTypeHandler(YearMonthOfStringTypeHandler.class));
        this.registerCrossNChars(YearMonth.class, createSingleTypeHandler(YearMonthOfStringTypeHandler.class));
        this.registerCrossNumber(YearMonth.class, createSingleTypeHandler(YearMonthOfNumberTypeHandler.class));
        this.registerCrossChars(Year.class, createSingleTypeHandler(YearOfStringTypeHandler.class));
        this.registerCrossNChars(Year.class, createSingleTypeHandler(YearOfStringTypeHandler.class));
        this.registerCrossNumber(Year.class, createSingleTypeHandler(YearOfNumberTypeHandler.class));
        this.registerCrossChars(Month.class, createSingleTypeHandler(MonthOfStringTypeHandler.class));
        this.registerCrossNChars(Month.class, createSingleTypeHandler(MonthOfStringTypeHandler.class));
        this.registerCrossNumber(Month.class, createSingleTypeHandler(MonthOfNumberTypeHandler.class));

        this.registerCrossChars(String.class, createSingleTypeHandler(StringTypeHandler.class));
        this.registerCrossNChars(String.class, createSingleTypeHandler(NStringTypeHandler.class));
        this.registerCross(JDBCType.CLOB.getVendorTypeNumber(), String.class, createSingleTypeHandler(ClobTypeHandler.class));
        this.registerCross(JDBCType.NCLOB.getVendorTypeNumber(), String.class, createSingleTypeHandler(NClobTypeHandler.class));
        this.registerCrossChars(Reader.class, createSingleTypeHandler(StringReaderTypeHandler.class));
        this.registerCrossNChars(Reader.class, createSingleTypeHandler(NStringReaderTypeHandler.class));
        this.registerCross(JDBCType.CLOB.getVendorTypeNumber(), Reader.class, createSingleTypeHandler(ClobReaderTypeHandler.class));
        this.registerCross(JDBCType.NCLOB.getVendorTypeNumber(), Reader.class, createSingleTypeHandler(NClobReaderTypeHandler.class));

        this.registerCross(JDBCType.SQLXML.getVendorTypeNumber(), String.class, createSingleTypeHandler(SqlXmlTypeHandler.class));
        this.registerCross(JDBCType.SQLXML.getVendorTypeNumber(), Reader.class, createSingleTypeHandler(SqlXmlForReaderTypeHandler.class));
        this.registerCross(JDBCType.SQLXML.getVendorTypeNumber(), InputStream.class, createSingleTypeHandler(SqlXmlForInputStreamTypeHandler.class));

        this.registerCross(JDBCType.BINARY.getVendorTypeNumber(), byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(JDBCType.BINARY.getVendorTypeNumber(), Byte[].class, createSingleTypeHandler(BytesForWrapTypeHandler.class));
        this.registerCross(JDBCType.VARBINARY.getVendorTypeNumber(), byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(JDBCType.VARBINARY.getVendorTypeNumber(), Byte[].class, createSingleTypeHandler(BytesForWrapTypeHandler.class));
        this.registerCross(JDBCType.BLOB.getVendorTypeNumber(), byte[].class, createSingleTypeHandler(BlobBytesTypeHandler.class));
        this.registerCross(JDBCType.BLOB.getVendorTypeNumber(), Byte[].class, createSingleTypeHandler(BlobBytesForWrapTypeHandler.class));
        this.registerCross(JDBCType.LONGVARBINARY.getVendorTypeNumber(), byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(JDBCType.LONGVARBINARY.getVendorTypeNumber(), Byte[].class, createSingleTypeHandler(BytesForWrapTypeHandler.class));

        this.registerCross(JDBCType.BINARY.getVendorTypeNumber(), InputStream.class, createSingleTypeHandler(BytesInputStreamTypeHandler.class));
        this.registerCross(JDBCType.VARBINARY.getVendorTypeNumber(), InputStream.class, createSingleTypeHandler(BytesInputStreamTypeHandler.class));
        this.registerCross(JDBCType.BLOB.getVendorTypeNumber(), InputStream.class, createSingleTypeHandler(BlobInputStreamTypeHandler.class));
        this.registerCross(JDBCType.LONGVARBINARY.getVendorTypeNumber(), InputStream.class, createSingleTypeHandler(BytesInputStreamTypeHandler.class));

        this.registerCross(JDBCType.ARRAY.getVendorTypeNumber(), Object.class, createSingleTypeHandler(ArrayTypeHandler.class));

        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleBlob", JDBCType.VARBINARY.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleClob", JDBCType.CLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleNClob", JDBCType.NCLOB.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.DATE", JDBCType.DATE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMP", JDBCType.TIMESTAMP.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPTZ", JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPLTZ", JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber());
    }

    private TypeHandler<?> createSingleTypeHandler(Class<? extends TypeHandler<?>> typeHandler) {
        cachedSingleHandlers.computeIfAbsent(typeHandler, type -> {
            try {
                if (typeHandler == UnknownTypeHandler.class) {
                    return defaultTypeHandler;
                } else {
                    return typeHandler.newInstance();
                }
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        });
        return cachedSingleHandlers.get(typeHandler);
    }

    /** 注册 TypeHandler */
    public void register(int jdbcType, TypeHandler<?> typeHandler) {
        this.jdbcTypeHandlerMap.put(jdbcType, typeHandler);
    }

    /** 注册 TypeHandler */
    public void register(Class<?> javaType, TypeHandler<?> typeHandler) {
        this.javaTypeHandlerMap.put(javaType.getName(), typeHandler);
    }

    public void registerCross(int jdbcType, Class<?> javaType, TypeHandler<?> typeHandler) {
        Map<Integer, TypeHandler<?>> typeClassMap = this.typeHandlerMap.computeIfAbsent(javaType.getName(), k -> {
            return new ConcurrentHashMap<>();
        });
        typeClassMap.put(jdbcType, typeHandler);
    }

    private void registerCrossChars(Class<?> jdbcType, TypeHandler<?> typeHandler) {
        registerCross(JDBCType.CHAR.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.VARCHAR.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.LONGVARCHAR.getVendorTypeNumber(), jdbcType, typeHandler);
    }

    private void registerCrossNChars(Class<?> jdbcType, TypeHandler<?> typeHandler) {
        registerCross(JDBCType.NCHAR.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.NVARCHAR.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.LONGNVARCHAR.getVendorTypeNumber(), jdbcType, typeHandler);
    }

    private void registerCrossNumber(Class<?> jdbcType, TypeHandler<?> typeHandler) {
        registerCross(JDBCType.TINYINT.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.SMALLINT.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.INTEGER.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.BIGINT.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.FLOAT.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.DOUBLE.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.REAL.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.NUMERIC.getVendorTypeNumber(), jdbcType, typeHandler);
        registerCross(JDBCType.DECIMAL.getVendorTypeNumber(), jdbcType, typeHandler);
    }

    /** 根据 @MappedJavaTypes @MappedJdbcTypes @MappedCross 注解注册 TypeHandler */
    public void registerHandler(Class<? extends TypeHandler<?>> handlerClass, TypeHandler<?> typeHandler) {
        MappedJavaTypes mappedTypes = handlerClass.getAnnotation(MappedJavaTypes.class);
        if (mappedTypes != null) {
            for (Class<?> handledType : mappedTypes.value()) {
                register(handledType, typeHandler);
            }
        }
        MappedJdbcTypes mappedJdbcTypes = handlerClass.getAnnotation(MappedJdbcTypes.class);
        if (mappedJdbcTypes != null) {
            for (int jdbcType : mappedJdbcTypes.value()) {
                if (typeHandler instanceof TypeReference) {
                    registerCross(jdbcType, ((TypeReference<?>) typeHandler).getRawType(), typeHandler);
                } else {
                    register(jdbcType, typeHandler);
                }
            }
        }
        MappedCross[] mappedCrosses = handlerClass.getAnnotationsByType(MappedCross.class);
        for (MappedCross cross : mappedCrosses) {
            MappedJdbcTypes jdbcTypes = cross.jdbcType();
            MappedJavaTypes javaTypes = cross.javaTypes();
            for (Class<?> javaType : javaTypes.value()) {
                for (int jdbcType : jdbcTypes.value()) {
                    registerCross(jdbcType, javaType, typeHandler);
                }
            }
        }
    }

    public Collection<TypeHandler<?>> getTypeHandlers() {
        return Collections.unmodifiableCollection(this.javaTypeHandlerMap.values());
    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type.*/
    public static int toSqlType(final String javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType);
        if (jdbcType != null) {
            return jdbcType;
        }
        return JDBCType.JAVA_OBJECT.getVendorTypeNumber();

    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type.*/
    public static int toSqlType(final Class<?> javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType.getName());
        if (jdbcType != null) {
            return jdbcType;
        }
        return JDBCType.JAVA_OBJECT.getVendorTypeNumber();
    }

    /** 根据 jdbcType 获取默认的 Java Type.*/
    public static Class<?> toJavaType(int jdbcType) {
        return jdbcTypeToJavaTypeMap.get(jdbcType);
    }

    public static boolean hasTypeHandlerType(Class<? extends TypeHandler<?>> handlerType) {
        Objects.requireNonNull(handlerType, "handlerType is null.");
        return cachedSingleHandlers.containsKey(handlerType);
    }

    public static TypeHandler<?> getTypeHandlerByType(Class<? extends TypeHandler<?>> handlerType) {
        return cachedSingleHandlers.get(handlerType);
    }

    public boolean hasTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        return this.javaTypeHandlerMap.containsKey(typeClass.getName());
    }

    public boolean hasTypeHandler(int jdbcType) {
        return this.jdbcTypeHandlerMap.containsKey(jdbcType);
    }

    public boolean hasTypeHandler(Class<?> typeClass, int jdbcType) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        Map<Integer, TypeHandler<?>> jdbcHandlerMap = this.typeHandlerMap.get(typeClass.getName());
        if (jdbcHandlerMap != null) {
            return jdbcHandlerMap.containsKey(jdbcType);
        }
        return false;
    }

    public TypeHandler<?> getTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        TypeHandler<?> typeHandler = this.javaTypeHandlerMap.get(typeClass.getName());
        return (typeHandler != null) ? typeHandler : this.defaultTypeHandler;
    }

    public TypeHandler<?> getTypeHandler(int jdbcType) {
        TypeHandler<?> typeHandler = this.jdbcTypeHandlerMap.get(jdbcType);
        return (typeHandler != null) ? typeHandler : this.defaultTypeHandler;
    }

    /**
     * 根据 typeClass 和 jdbcType 的映射关系查找对应的 TypeHandler。
     *  - 如果不存在对应的 TypeHandler，那么通过 typeClass 单独查找。
     *  - 如果 typeClass 也没有注册那么返回 {@link #getDefaultTypeHandler()} */
    public TypeHandler<?> getTypeHandler(Class<?> typeClass, int jdbcType) {
        if (typeClass == null) {
            return this.defaultTypeHandler;
        }
        Map<Integer, TypeHandler<?>> handlerMap = this.typeHandlerMap.get(typeClass.getName());
        if (handlerMap != null) {
            TypeHandler<?> typeHandler = handlerMap.get(jdbcType);
            if (typeHandler != null) {
                return typeHandler;
            }
        }

        TypeHandler<?> typeHandler = this.javaTypeHandlerMap.get(typeClass.getName());
        if (typeHandler != null) {
            return typeHandler;
        }
        if (Enum.class.isAssignableFrom(typeClass)) {
            typeClass = typeClass.isAnonymousClass() ? typeClass.getSuperclass() : typeClass;
            typeHandler = this.javaTypeHandlerMap.get(typeClass.getName());
            if (typeHandler == null) {
                EnumTypeHandler enumOfStringTypeHandler = new EnumTypeHandler(typeClass);
                this.javaTypeHandlerMap.put(typeClass.getName(), enumOfStringTypeHandler);
                return enumOfStringTypeHandler;
            }
        }

        return this.defaultTypeHandler;
    }

    public UnknownTypeHandler getDefaultTypeHandler() {
        return this.defaultTypeHandler;
    }

    /** 一个工具方法，会根据 value Type 自动的选择对应的 TypeHandler */
    public void setParameterValue(final PreparedStatement ps, final int parameterPosition, final Object value) throws SQLException {
        if (value == null) {
            ps.setObject(parameterPosition, null);
        } else {
            Class<?> valueClass = value.getClass();
            TypeHandler<Object> typeHandler = (TypeHandler<Object>) getTypeHandler(valueClass);
            typeHandler.setParameter(ps, parameterPosition, value, toSqlType(valueClass));
        }
    }
}
