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
package net.hasor.dbvisitor.types;
import net.hasor.cobble.ExceptionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.reflect.ConstructorUtils;
import net.hasor.cobble.reflect.TypeReference;
import net.hasor.dbvisitor.types.handler.*;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
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

    public static final TypeHandlerRegistry                       DEFAULT            = new TypeHandlerRegistry();
    private final       UnknownTypeHandler                        defaultTypeHandler = new UnknownTypeHandler(this);
    // mappings
    private final       Map<String, TypeHandler<?>>               javaTypeHandlerMap = new ConcurrentHashMap<>();
    private final       Map<Integer, TypeHandler<?>>              jdbcTypeHandlerMap = new ConcurrentHashMap<>();
    private final       Map<String, Map<Integer, TypeHandler<?>>> typeHandlerMap     = new ConcurrentHashMap<>();

    static {
        // primitive and wrapper
        javaTypeToJdbcTypeMap.put(Boolean.class.getName(), Types.BIT);
        javaTypeToJdbcTypeMap.put(boolean.class.getName(), Types.BIT);
        javaTypeToJdbcTypeMap.put(Byte.class.getName(), Types.TINYINT);
        javaTypeToJdbcTypeMap.put(byte.class.getName(), Types.TINYINT);
        javaTypeToJdbcTypeMap.put(Short.class.getName(), Types.SMALLINT);
        javaTypeToJdbcTypeMap.put(short.class.getName(), Types.SMALLINT);
        javaTypeToJdbcTypeMap.put(Integer.class.getName(), Types.INTEGER);
        javaTypeToJdbcTypeMap.put(int.class.getName(), Types.INTEGER);
        javaTypeToJdbcTypeMap.put(Long.class.getName(), Types.BIGINT);
        javaTypeToJdbcTypeMap.put(long.class.getName(), Types.BIGINT);
        javaTypeToJdbcTypeMap.put(Float.class.getName(), Types.FLOAT);
        javaTypeToJdbcTypeMap.put(float.class.getName(), Types.FLOAT);
        javaTypeToJdbcTypeMap.put(Double.class.getName(), Types.DOUBLE);
        javaTypeToJdbcTypeMap.put(double.class.getName(), Types.DOUBLE);
        javaTypeToJdbcTypeMap.put(Character.class.getName(), Types.CHAR);
        javaTypeToJdbcTypeMap.put(char.class.getName(), Types.CHAR);
        // java time
        javaTypeToJdbcTypeMap.put(Date.class.getName(), Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put(java.sql.Date.class.getName(), Types.DATE);
        javaTypeToJdbcTypeMap.put(java.sql.Timestamp.class.getName(), Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put(java.sql.Time.class.getName(), Types.TIME);
        javaTypeToJdbcTypeMap.put(Instant.class.getName(), Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put(LocalDateTime.class.getName(), Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put(LocalDate.class.getName(), Types.DATE);
        javaTypeToJdbcTypeMap.put(LocalTime.class.getName(), Types.TIME);
        javaTypeToJdbcTypeMap.put(ZonedDateTime.class.getName(), Types.TIMESTAMP_WITH_TIMEZONE);
        javaTypeToJdbcTypeMap.put(JapaneseDate.class.getName(), Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put(YearMonth.class.getName(), Types.VARCHAR);
        javaTypeToJdbcTypeMap.put(Year.class.getName(), Types.SMALLINT);
        javaTypeToJdbcTypeMap.put(Month.class.getName(), Types.SMALLINT);
        javaTypeToJdbcTypeMap.put(OffsetDateTime.class.getName(), Types.TIMESTAMP_WITH_TIMEZONE);
        javaTypeToJdbcTypeMap.put(OffsetTime.class.getName(), Types.TIME_WITH_TIMEZONE);
        // java extensions Types
        javaTypeToJdbcTypeMap.put(String.class.getName(), Types.VARCHAR);
        javaTypeToJdbcTypeMap.put(BigInteger.class.getName(), Types.BIGINT);
        javaTypeToJdbcTypeMap.put(BigDecimal.class.getName(), Types.DECIMAL);
        javaTypeToJdbcTypeMap.put(Reader.class.getName(), Types.CLOB);
        javaTypeToJdbcTypeMap.put(InputStream.class.getName(), Types.BLOB);
        javaTypeToJdbcTypeMap.put(URL.class.getName(), Types.DATALINK);
        javaTypeToJdbcTypeMap.put(URI.class.getName(), Types.DATALINK);
        javaTypeToJdbcTypeMap.put(Byte[].class.getName(), Types.VARBINARY);
        javaTypeToJdbcTypeMap.put(byte[].class.getName(), Types.VARBINARY);
        javaTypeToJdbcTypeMap.put(Object[].class.getName(), Types.ARRAY);
        javaTypeToJdbcTypeMap.put(Object.class.getName(), Types.JAVA_OBJECT);
        // oracle types
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleBlob", Types.BLOB);
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleClob", Types.CLOB);
        javaTypeToJdbcTypeMap.put("oracle.jdbc.OracleNClob", Types.NCLOB);
        javaTypeToJdbcTypeMap.put("oracle.sql.DATE", Types.DATE);
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMP", Types.TIMESTAMP);
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPTZ", Types.TIMESTAMP_WITH_TIMEZONE);
        javaTypeToJdbcTypeMap.put("oracle.sql.TIMESTAMPLTZ", Types.TIMESTAMP_WITH_TIMEZONE);
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
        this.register(Character.class, createSingleTypeHandler(StringAsCharTypeHandler.class));
        this.register(char.class, createSingleTypeHandler(StringAsCharTypeHandler.class));
        // java time
        this.register(Date.class, createSingleTypeHandler(SqlTimestampAsDateTypeHandler.class));
        this.register(java.sql.Date.class, createSingleTypeHandler(SqlDateTypeHandler.class));
        this.register(java.sql.Timestamp.class, createSingleTypeHandler(SqlTimestampTypeHandler.class));
        this.register(java.sql.Time.class, createSingleTypeHandler(SqlTimeTypeHandler.class));
        this.register(Instant.class, createSingleTypeHandler(SqlTimestampAsInstantTypeHandler.class));
        this.register(JapaneseDate.class, createSingleTypeHandler(JapaneseDateAsSqlDateTypeHandler.class));
        this.register(Year.class, createSingleTypeHandler(SqlTimestampAsYearTypeHandler.class));
        this.register(Month.class, createSingleTypeHandler(SqlTimestampAsMonthTypeHandler.class));
        this.register(YearMonth.class, createSingleTypeHandler(SqlTimestampAsYearMonthTypeHandler.class));
        this.register(MonthDay.class, createSingleTypeHandler(SqlTimestampAsMonthDayTypeHandler.class));
        //
        this.register(LocalDate.class, createSingleTypeHandler(LocalDateTimeAsLocalDateTypeHandler.class));
        this.register(LocalTime.class, createSingleTypeHandler(LocalTimeTypeHandler.class));
        this.register(LocalDateTime.class, createSingleTypeHandler(LocalDateTimeTypeHandler.class));
        this.register(ZonedDateTime.class, createSingleTypeHandler(OffsetDateTimeAsZonedDateTimeTypeHandler.class));
        this.register(OffsetDateTime.class, createSingleTypeHandler(OffsetDateTimeTypeHandler.class));
        this.register(OffsetTime.class, createSingleTypeHandler(OffsetTimeTypeHandler.class));
        // java extensions Types
        this.register(String.class, createSingleTypeHandler(StringTypeHandler.class));
        this.register(BigInteger.class, createSingleTypeHandler(BigIntegerTypeHandler.class));
        this.register(BigDecimal.class, createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(Reader.class, createSingleTypeHandler(StringAsReaderTypeHandler.class));
        this.register(InputStream.class, createSingleTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.register(Byte[].class, createSingleTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.register(byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.register(Object[].class, createSingleTypeHandler(ArrayTypeHandler.class));
        this.register(Object.class, createSingleTypeHandler(UnknownTypeHandler.class));
        this.register(Number.class, createSingleTypeHandler(NumberTypeHandler.class));
        this.register(Clob.class, createSingleTypeHandler(ClobAsStringTypeHandler.class));
        this.register(NClob.class, createSingleTypeHandler(NClobAsStringTypeHandler.class));
        this.register(Blob.class, createSingleTypeHandler(BlobAsBytesTypeHandler.class));
        this.register(URL.class, createSingleTypeHandler(URLTypeHandler.class));
        this.register(URI.class, createSingleTypeHandler(URITypeHandler.class));

        this.register(Types.BIT, createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(Types.BOOLEAN, createSingleTypeHandler(BooleanTypeHandler.class));
        this.register(Types.TINYINT, createSingleTypeHandler(ByteTypeHandler.class));
        this.register(Types.SMALLINT, createSingleTypeHandler(ShortTypeHandler.class));
        this.register(Types.INTEGER, createSingleTypeHandler(IntegerTypeHandler.class));
        this.register(Types.BIGINT, createSingleTypeHandler(LongTypeHandler.class));
        this.register(Types.FLOAT, createSingleTypeHandler(FloatTypeHandler.class));
        this.register(Types.DOUBLE, createSingleTypeHandler(DoubleTypeHandler.class));
        this.register(Types.REAL, createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.NUMERIC, createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.DECIMAL, createSingleTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.CHAR, createSingleTypeHandler(StringAsCharTypeHandler.class));
        this.register(Types.NCHAR, createSingleTypeHandler(NStringAsCharTypeHandler.class));
        this.register(Types.CLOB, createSingleTypeHandler(ClobAsStringTypeHandler.class));
        this.register(Types.VARCHAR, createSingleTypeHandler(StringTypeHandler.class));
        this.register(Types.LONGVARCHAR, createSingleTypeHandler(StringTypeHandler.class));
        this.register(Types.NCLOB, createSingleTypeHandler(NClobAsStringTypeHandler.class));
        this.register(Types.NVARCHAR, createSingleTypeHandler(NStringTypeHandler.class));
        this.register(Types.LONGNVARCHAR, createSingleTypeHandler(NStringTypeHandler.class));
        this.register(Types.TIMESTAMP, createSingleTypeHandler(SqlTimestampAsDateTypeHandler.class));
        this.register(Types.DATE, createSingleTypeHandler(SqlDateAsDateHandler.class));
        this.register(Types.TIME, createSingleTypeHandler(SqlTimeAsDateTypeHandler.class));
        this.register(Types.TIME_WITH_TIMEZONE, createSingleTypeHandler(OffsetTimeTypeHandler.class));
        this.register(Types.TIMESTAMP_WITH_TIMEZONE, createSingleTypeHandler(OffsetDateTimeTypeHandler.class));
        this.register(Types.SQLXML, createSingleTypeHandler(SqlXmlTypeHandler.class));
        this.register(Types.BINARY, createSingleTypeHandler(BytesTypeHandler.class));
        this.register(Types.VARBINARY, createSingleTypeHandler(BytesTypeHandler.class));
        this.register(Types.LONGVARBINARY, createSingleTypeHandler(BytesTypeHandler.class));
        this.register(Types.BLOB, createSingleTypeHandler(BlobAsBytesTypeHandler.class));
        this.register(Types.JAVA_OBJECT, createSingleTypeHandler(ObjectTypeHandler.class));
        this.register(Types.ARRAY, createSingleTypeHandler(ArrayTypeHandler.class));
        this.register(Types.DATALINK, createSingleTypeHandler(URLTypeHandler.class));
        this.register(Types.ROWID, createSingleTypeHandler(StringTypeHandler.class));
        // DISTINCT(Types.DISTINCT),
        // STRUCT(Types.STRUCT),
        // REF(Types.REF),
        // REF_CURSOR(Types.REF_CURSOR),
        this.register(Types.OTHER, createSingleTypeHandler(UnknownTypeHandler.class));

        this.registerCrossChars(MonthDay.class, createSingleTypeHandler(StringAsMonthDayTypeHandler.class));
        this.registerCrossNChars(MonthDay.class, createSingleTypeHandler(StringAsMonthDayTypeHandler.class));
        this.registerCrossNumber(MonthDay.class, createSingleTypeHandler(IntegerAsMonthDayTypeHandler.class));
        this.registerCrossChars(YearMonth.class, createSingleTypeHandler(StringAsYearMonthTypeHandler.class));
        this.registerCrossNChars(YearMonth.class, createSingleTypeHandler(StringAsYearMonthTypeHandler.class));
        this.registerCrossNumber(YearMonth.class, createSingleTypeHandler(IntegerAsYearMonthTypeHandler.class));
        this.registerCrossChars(Year.class, createSingleTypeHandler(StringAsYearTypeHandler.class));
        this.registerCrossNChars(Year.class, createSingleTypeHandler(StringAsYearTypeHandler.class));
        this.registerCrossNumber(Year.class, createSingleTypeHandler(IntegerAsYearTypeHandler.class));
        this.registerCrossChars(Month.class, createSingleTypeHandler(StringAsMonthTypeHandler.class));
        this.registerCrossNChars(Month.class, createSingleTypeHandler(StringAsMonthTypeHandler.class));
        this.registerCrossNumber(Month.class, createSingleTypeHandler(IntegerAsMonthTypeHandler.class));

        this.registerCrossChars(String.class, createSingleTypeHandler(StringTypeHandler.class));
        this.registerCrossNChars(String.class, createSingleTypeHandler(NStringTypeHandler.class));
        this.registerCross(Types.CLOB, String.class, createSingleTypeHandler(ClobAsStringTypeHandler.class));
        this.registerCross(Types.NCLOB, String.class, createSingleTypeHandler(NClobAsStringTypeHandler.class));
        this.registerCrossChars(Reader.class, createSingleTypeHandler(StringAsReaderTypeHandler.class));
        this.registerCrossNChars(Reader.class, createSingleTypeHandler(NStringAsReaderTypeHandler.class));
        this.registerCross(Types.CLOB, Reader.class, createSingleTypeHandler(ClobAsReaderTypeHandler.class));
        this.registerCross(Types.NCLOB, Reader.class, createSingleTypeHandler(NClobAsReaderTypeHandler.class));

        this.registerCross(Types.SQLXML, String.class, createSingleTypeHandler(SqlXmlTypeHandler.class));
        this.registerCross(Types.SQLXML, Reader.class, createSingleTypeHandler(SqlXmlForReaderTypeHandler.class));
        this.registerCross(Types.SQLXML, InputStream.class, createSingleTypeHandler(SqlXmlForInputStreamTypeHandler.class));

        this.registerCross(Types.BINARY, byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.BINARY, Byte[].class, createSingleTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.VARBINARY, byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.VARBINARY, Byte[].class, createSingleTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.BLOB, byte[].class, createSingleTypeHandler(BlobAsBytesTypeHandler.class));
        this.registerCross(Types.BLOB, Byte[].class, createSingleTypeHandler(BlobAsBytesWrapTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, Byte[].class, createSingleTypeHandler(BytesAsBytesWrapTypeHandler.class));

        this.registerCross(Types.BINARY, InputStream.class, createSingleTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.VARBINARY, InputStream.class, createSingleTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, InputStream.class, createSingleTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.BLOB, InputStream.class, createSingleTypeHandler(BlobAsInputStreamTypeHandler.class));

        this.registerCross(Types.ARRAY, Object.class, createSingleTypeHandler(ArrayTypeHandler.class));

        this.registerCross(Types.DATALINK, String.class, createSingleTypeHandler(StringTypeHandler.class));
        this.registerCross(Types.DATALINK, URL.class, createSingleTypeHandler(URLTypeHandler.class));
        this.registerCross(Types.DATALINK, URI.class, createSingleTypeHandler(URITypeHandler.class));

        this.registerCross(Types.ROWID, byte[].class, createSingleTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.ROWID, Byte[].class, createSingleTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.ROWID, String.class, createSingleTypeHandler(StringTypeHandler.class));
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
        registerCross(Types.CHAR, jdbcType, typeHandler);
        registerCross(Types.VARCHAR, jdbcType, typeHandler);
        registerCross(Types.LONGVARCHAR, jdbcType, typeHandler);
    }

    private void registerCrossNChars(Class<?> jdbcType, TypeHandler<?> typeHandler) {
        registerCross(Types.NCHAR, jdbcType, typeHandler);
        registerCross(Types.NVARCHAR, jdbcType, typeHandler);
        registerCross(Types.LONGNVARCHAR, jdbcType, typeHandler);
    }

    private void registerCrossNumber(Class<?> jdbcType, TypeHandler<?> typeHandler) {
        registerCross(Types.TINYINT, jdbcType, typeHandler);
        registerCross(Types.SMALLINT, jdbcType, typeHandler);
        registerCross(Types.INTEGER, jdbcType, typeHandler);
        registerCross(Types.BIGINT, jdbcType, typeHandler);
        registerCross(Types.FLOAT, jdbcType, typeHandler);
        registerCross(Types.DOUBLE, jdbcType, typeHandler);
        registerCross(Types.REAL, jdbcType, typeHandler);
        registerCross(Types.NUMERIC, jdbcType, typeHandler);
        registerCross(Types.DECIMAL, jdbcType, typeHandler);
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

    public Collection<String> getHandlerJavaTypes() {
        return Collections.unmodifiableCollection(this.javaTypeHandlerMap.keySet());
    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type.*/
    public static int toSqlType(final String javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType);
        if (jdbcType != null) {
            return jdbcType;
        }
        return Types.JAVA_OBJECT;

    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type.*/
    public static int toSqlType(final Class<?> javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType.getName());
        if (jdbcType != null) {
            return jdbcType;
        }
        return Types.JAVA_OBJECT;
    }

    public boolean hasTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        if (typeClass.isEnum()) {
            return true;
        }
        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            return true;
        }

        return this.javaTypeHandlerMap.containsKey(typeClass.getName());
    }

    public boolean hasTypeHandler(String typeName) {
        Objects.requireNonNull(typeName, "typeName is null.");
        return this.javaTypeHandlerMap.containsKey(typeName);
    }

    public boolean hasTypeHandler(int jdbcType) {
        return this.jdbcTypeHandlerMap.containsKey(jdbcType);
    }

    public boolean hasTypeHandler(Class<?> typeClass, int jdbcType) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        if (typeClass.isEnum()) {
            return true;
        }
        Map<Integer, TypeHandler<?>> jdbcHandlerMap = this.typeHandlerMap.get(typeClass.getName());
        if (jdbcHandlerMap != null) {
            return jdbcHandlerMap.containsKey(jdbcType);
        }

        return typeClass.isAnnotationPresent(BindTypeHandler.class);
    }

    public TypeHandler<?> getTypeHandler(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            throw new NullPointerException("typeName is null.");
        }
        TypeHandler<?> typeHandler = this.javaTypeHandlerMap.get(typeName);
        return (typeHandler != null) ? typeHandler : this.defaultTypeHandler;
    }

    public TypeHandler<?> getTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        String typeClassName = typeClass.getName();
        TypeHandler<?> typeHandler = this.javaTypeHandlerMap.get(typeClassName);
        if (typeHandler != null) {
            return typeHandler;
        }

        // maybe classType include BindTypeHandler
        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            synchronized (this) {
                if (this.javaTypeHandlerMap.containsKey(typeClassName)) {
                    return this.javaTypeHandlerMap.get(typeClassName);
                }

                try {
                    BindTypeHandler handler = typeClass.getAnnotation(BindTypeHandler.class);
                    Constructor<?> constructor = ConstructorUtils.getAccessibleConstructor(handler.value(), Class.class);
                    if (constructor == null) {
                        typeHandler = (TypeHandler<?>) handler.value().newInstance();
                    } else {
                        typeHandler = (TypeHandler<?>) ConstructorUtils.invokeConstructor(handler.value(), typeClass);
                    }
                } catch (Exception e) {
                    throw ExceptionUtils.toRuntime(e);
                }

                this.javaTypeHandlerMap.put(typeClassName, typeHandler);
                return typeHandler;
            }
        }

        // maybe classType is enum
        if (Enum.class.isAssignableFrom(typeClass)) {
            return this.javaTypeHandlerMap.computeIfAbsent(typeClass.getName(), s -> {
                Class<?> enumType = typeClass.isAnonymousClass() ? typeClass.getSuperclass() : typeClass;
                return new EnumTypeHandler(enumType);
            });
        }

        // register default
        this.javaTypeHandlerMap.put(typeClassName, this.defaultTypeHandler);
        return this.defaultTypeHandler;
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

        // find by classType and jdbcType
        String typeClassName = typeClass.getName();
        Map<Integer, TypeHandler<?>> handlerMap = this.typeHandlerMap.get(typeClassName);
        if (handlerMap != null) {
            TypeHandler<?> typeHandler = handlerMap.get(jdbcType);
            if (typeHandler != null) {
                return typeHandler;
            }
        }

        // find by classType
        TypeHandler<?> typeHandler = this.javaTypeHandlerMap.get(typeClassName);
        if (typeHandler != null) {
            return typeHandler;
        }

        // maybe classType include BindTypeHandler
        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            synchronized (this) {
                if (this.typeHandlerMap.containsKey(typeClassName)) {
                    handlerMap = this.typeHandlerMap.get(typeClassName);
                    if (handlerMap.containsKey(jdbcType)) {
                        return handlerMap.get(jdbcType);
                    }
                }

                try {
                    BindTypeHandler handler = typeClass.getAnnotation(BindTypeHandler.class);
                    Constructor<?> constructor = ConstructorUtils.getAccessibleConstructor(handler.value(), Class.class);
                    if (constructor == null) {
                        typeHandler = (TypeHandler<?>) handler.value().newInstance();
                    } else {
                        typeHandler = (TypeHandler<?>) ConstructorUtils.invokeConstructor(handler.value(), typeClass);
                    }
                } catch (Exception e) {
                    throw ExceptionUtils.toRuntime(e);
                }

                registerCross(jdbcType, typeClass, typeHandler);
                return typeHandler;
            }
        }

        // maybe classType is enum
        if (Enum.class.isAssignableFrom(typeClass)) {
            typeClass = typeClass.isAnonymousClass() ? typeClass.getSuperclass() : typeClass;
            typeHandler = this.javaTypeHandlerMap.get(typeClass.getName());
            if (typeHandler == null) {
                EnumTypeHandler enumOfStringTypeHandler = new EnumTypeHandler(typeClass);
                registerCross(jdbcType, typeClass, enumOfStringTypeHandler);
                return enumOfStringTypeHandler;
            }
        }

        // register default
        registerCross(jdbcType, typeClass, this.defaultTypeHandler);
        return this.defaultTypeHandler;
    }

    public UnknownTypeHandler getDefaultTypeHandler() {
        return this.defaultTypeHandler;
    }

    /** 一个工具方法，会根据 value Type 自动的选择对应的 TypeHandler */
    public void setParameterValue(final PreparedStatement ps, final int parameterPosition, final Object value) throws SQLException {
        if (value == null) {
            ps.setObject(parameterPosition, null);
            return;
        }

        if (value instanceof MappedArg) {
            Integer argType = ((MappedArg) value).getJdbcType();
            TypeHandler argHandler = ((MappedArg) value).getTypeHandler();
            Object argValue = ((MappedArg) value).getValue();

            if (argType == null && argValue != null) {
                argType = TypeHandlerRegistry.toSqlType(argValue.getClass());
            }

            if (argHandler == null && argValue != null) {
                argHandler = this.getTypeHandler(argValue.getClass());
            }

            if (argHandler != null) {
                argHandler.setParameter(ps, parameterPosition, argValue, argType);
                return;
            } else if (argValue == null) {
                ps.setObject(parameterPosition, null);
                return;
            }
        }

        Class<?> valueClass = value.getClass();
        TypeHandler<Object> typeHandler = (TypeHandler<Object>) getTypeHandler(valueClass);
        typeHandler.setParameter(ps, parameterPosition, value, toSqlType(valueClass));
    }
}
