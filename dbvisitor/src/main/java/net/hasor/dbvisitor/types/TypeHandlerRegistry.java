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
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.types.handler.*;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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
import java.util.function.Supplier;

/**
 * JDBC 4.2 full  compatible
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public final class TypeHandlerRegistry {

    private static final Map<String, Integer>  javaTypeToJdbcTypeMap = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> typeHandlerTypeCache  = new ConcurrentHashMap<>();
    public static final  TypeHandlerRegistry   DEFAULT               = new TypeHandlerRegistry();

    private final UnknownTypeHandler                          defaultTypeHandler        = new UnknownTypeHandler(this);
    private final Map<String, TypeHandler<?>>                 cachedByHandlerType       = new ConcurrentHashMap<>();
    private final Map<String, TypeHandler<?>>                 cachedByJavaType          = new ConcurrentHashMap<>();
    private final Map<Integer, TypeHandler<?>>                cachedByJdbcType          = new ConcurrentHashMap<>();
    private final Map<String, Map<Integer, TypeHandler<?>>>   cachedByCrossType         = new ConcurrentHashMap<>();
    private final Map<Class<?>, TypeHandler<?>>               abstractCachedByJavaType  = new LinkedHashMap<>();
    private final Map<Class<?>, Map<Integer, TypeHandler<?>>> abstractCachedByCrossType = new LinkedHashMap<>();

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
        this.register(Boolean.class, createTypeHandler(BooleanTypeHandler.class));
        this.register(boolean.class, createTypeHandler(BooleanTypeHandler.class));
        this.register(Byte.class, createTypeHandler(ByteTypeHandler.class));
        this.register(byte.class, createTypeHandler(ByteTypeHandler.class));
        this.register(Short.class, createTypeHandler(ShortTypeHandler.class));
        this.register(short.class, createTypeHandler(ShortTypeHandler.class));
        this.register(Integer.class, createTypeHandler(IntegerTypeHandler.class));
        this.register(int.class, createTypeHandler(IntegerTypeHandler.class));
        this.register(Long.class, createTypeHandler(LongTypeHandler.class));
        this.register(long.class, createTypeHandler(LongTypeHandler.class));
        this.register(Float.class, createTypeHandler(FloatTypeHandler.class));
        this.register(float.class, createTypeHandler(FloatTypeHandler.class));
        this.register(Double.class, createTypeHandler(DoubleTypeHandler.class));
        this.register(double.class, createTypeHandler(DoubleTypeHandler.class));
        this.register(Character.class, createTypeHandler(StringAsCharTypeHandler.class));
        this.register(char.class, createTypeHandler(StringAsCharTypeHandler.class));
        // java time
        this.register(Date.class, createTypeHandler(SqlTimestampAsDateTypeHandler.class));
        this.register(java.sql.Date.class, createTypeHandler(SqlDateTypeHandler.class));
        this.register(java.sql.Timestamp.class, createTypeHandler(SqlTimestampTypeHandler.class));
        this.register(java.sql.Time.class, createTypeHandler(SqlTimeTypeHandler.class));
        this.register(Instant.class, createTypeHandler(SqlTimestampAsInstantTypeHandler.class));
        this.register(JapaneseDate.class, createTypeHandler(JapaneseDateAsSqlDateTypeHandler.class));
        this.register(Year.class, createTypeHandler(SqlTimestampAsYearTypeHandler.class));
        this.register(Month.class, createTypeHandler(SqlTimestampAsMonthTypeHandler.class));
        this.register(YearMonth.class, createTypeHandler(SqlTimestampAsYearMonthTypeHandler.class));
        this.register(MonthDay.class, createTypeHandler(SqlTimestampAsMonthDayTypeHandler.class));
        //
        this.register(LocalDate.class, createTypeHandler(LocalDateTimeAsLocalDateTypeHandler.class));
        this.register(LocalTime.class, createTypeHandler(LocalTimeTypeHandler.class));
        this.register(LocalDateTime.class, createTypeHandler(LocalDateTimeTypeHandler.class));
        this.register(ZonedDateTime.class, createTypeHandler(OffsetDateTimeAsZonedDateTimeTypeHandler.class));
        this.register(OffsetDateTime.class, createTypeHandler(OffsetDateTimeTypeHandler.class));
        this.register(OffsetTime.class, createTypeHandler(OffsetTimeTypeHandler.class));
        // java extensions Types
        this.register(String.class, createTypeHandler(StringTypeHandler.class));
        this.register(BigInteger.class, createTypeHandler(BigIntegerTypeHandler.class));
        this.register(BigDecimal.class, createTypeHandler(BigDecimalTypeHandler.class));
        this.register(Reader.class, createTypeHandler(StringAsReaderTypeHandler.class));
        this.register(InputStream.class, createTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.register(Byte[].class, createTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.register(byte[].class, createTypeHandler(BytesTypeHandler.class));
        this.register(Object[].class, createTypeHandler(ArrayTypeHandler.class));
        this.register(Object.class, createTypeHandler(UnknownTypeHandler.class));
        this.register(Number.class, createTypeHandler(NumberTypeHandler.class));
        this.register(NClob.class, createTypeHandler(NClobAsStringTypeHandler.class));
        this.register(Clob.class, createTypeHandler(ClobAsStringTypeHandler.class));
        this.register(Blob.class, createTypeHandler(BlobAsBytesTypeHandler.class));
        this.register(URL.class, createTypeHandler(URLTypeHandler.class));
        this.register(URI.class, createTypeHandler(URITypeHandler.class));

        this.register(Types.BIT, createTypeHandler(BooleanTypeHandler.class));
        this.register(Types.BOOLEAN, createTypeHandler(BooleanTypeHandler.class));
        this.register(Types.TINYINT, createTypeHandler(ByteTypeHandler.class));
        this.register(Types.SMALLINT, createTypeHandler(ShortTypeHandler.class));
        this.register(Types.INTEGER, createTypeHandler(IntegerTypeHandler.class));
        this.register(Types.BIGINT, createTypeHandler(LongTypeHandler.class));
        this.register(Types.FLOAT, createTypeHandler(FloatTypeHandler.class));
        this.register(Types.DOUBLE, createTypeHandler(DoubleTypeHandler.class));
        this.register(Types.REAL, createTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.NUMERIC, createTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.DECIMAL, createTypeHandler(BigDecimalTypeHandler.class));
        this.register(Types.CHAR, createTypeHandler(StringAsCharTypeHandler.class));
        this.register(Types.NCHAR, createTypeHandler(NStringAsCharTypeHandler.class));
        this.register(Types.CLOB, createTypeHandler(ClobAsStringTypeHandler.class));
        this.register(Types.VARCHAR, createTypeHandler(StringTypeHandler.class));
        this.register(Types.LONGVARCHAR, createTypeHandler(StringTypeHandler.class));
        this.register(Types.NCLOB, createTypeHandler(NClobAsStringTypeHandler.class));
        this.register(Types.NVARCHAR, createTypeHandler(NStringTypeHandler.class));
        this.register(Types.LONGNVARCHAR, createTypeHandler(NStringTypeHandler.class));
        this.register(Types.TIMESTAMP, createTypeHandler(SqlTimestampAsDateTypeHandler.class));
        this.register(Types.DATE, createTypeHandler(SqlDateAsDateHandler.class));
        this.register(Types.TIME, createTypeHandler(SqlTimeAsDateTypeHandler.class));
        this.register(Types.TIME_WITH_TIMEZONE, createTypeHandler(OffsetTimeTypeHandler.class));
        this.register(Types.TIMESTAMP_WITH_TIMEZONE, createTypeHandler(OffsetDateTimeTypeHandler.class));
        this.register(Types.SQLXML, createTypeHandler(SqlXmlTypeHandler.class));
        this.register(Types.BINARY, createTypeHandler(BytesTypeHandler.class));
        this.register(Types.VARBINARY, createTypeHandler(BytesTypeHandler.class));
        this.register(Types.LONGVARBINARY, createTypeHandler(BytesTypeHandler.class));
        this.register(Types.BLOB, createTypeHandler(BlobAsBytesTypeHandler.class));
        this.register(Types.JAVA_OBJECT, createTypeHandler(ObjectTypeHandler.class));
        this.register(Types.ARRAY, createTypeHandler(ArrayTypeHandler.class));
        this.register(Types.DATALINK, createTypeHandler(URLTypeHandler.class));
        this.register(Types.ROWID, createTypeHandler(StringTypeHandler.class));
        // DISTINCT(Types.DISTINCT),
        // STRUCT(Types.STRUCT),
        // REF(Types.REF),
        // REF_CURSOR(Types.REF_CURSOR),
        this.register(Types.OTHER, createTypeHandler(UnknownTypeHandler.class));

        this.registerCrossChars(MonthDay.class, createTypeHandler(StringAsMonthDayTypeHandler.class));
        this.registerCrossNChars(MonthDay.class, createTypeHandler(StringAsMonthDayTypeHandler.class));
        this.registerCrossNumber(MonthDay.class, createTypeHandler(IntegerAsMonthDayTypeHandler.class));
        this.registerCrossChars(YearMonth.class, createTypeHandler(StringAsYearMonthTypeHandler.class));
        this.registerCrossNChars(YearMonth.class, createTypeHandler(StringAsYearMonthTypeHandler.class));
        this.registerCrossNumber(YearMonth.class, createTypeHandler(IntegerAsYearMonthTypeHandler.class));
        this.registerCrossChars(Year.class, createTypeHandler(StringAsYearTypeHandler.class));
        this.registerCrossNChars(Year.class, createTypeHandler(StringAsYearTypeHandler.class));
        this.registerCrossNumber(Year.class, createTypeHandler(IntegerAsYearTypeHandler.class));
        this.registerCrossChars(Month.class, createTypeHandler(StringAsMonthTypeHandler.class));
        this.registerCrossNChars(Month.class, createTypeHandler(StringAsMonthTypeHandler.class));
        this.registerCrossNumber(Month.class, createTypeHandler(IntegerAsMonthTypeHandler.class));

        this.registerCrossChars(String.class, createTypeHandler(StringTypeHandler.class));
        this.registerCrossNChars(String.class, createTypeHandler(NStringTypeHandler.class));
        this.registerCross(Types.CLOB, String.class, createTypeHandler(ClobAsStringTypeHandler.class));
        this.registerCross(Types.NCLOB, String.class, createTypeHandler(NClobAsStringTypeHandler.class));
        this.registerCrossChars(Reader.class, createTypeHandler(StringAsReaderTypeHandler.class));
        this.registerCrossNChars(Reader.class, createTypeHandler(NStringAsReaderTypeHandler.class));
        this.registerCross(Types.CLOB, Reader.class, createTypeHandler(ClobAsReaderTypeHandler.class));
        this.registerCross(Types.NCLOB, Reader.class, createTypeHandler(NClobAsReaderTypeHandler.class));

        this.registerCross(Types.SQLXML, String.class, createTypeHandler(SqlXmlTypeHandler.class));
        this.registerCross(Types.SQLXML, Reader.class, createTypeHandler(SqlXmlForReaderTypeHandler.class));
        this.registerCross(Types.SQLXML, InputStream.class, createTypeHandler(SqlXmlForInputStreamTypeHandler.class));

        this.registerCross(Types.BINARY, byte[].class, createTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.BINARY, Byte[].class, createTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.VARBINARY, byte[].class, createTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.VARBINARY, Byte[].class, createTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.BLOB, byte[].class, createTypeHandler(BlobAsBytesTypeHandler.class));
        this.registerCross(Types.BLOB, Byte[].class, createTypeHandler(BlobAsBytesWrapTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, byte[].class, createTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, Byte[].class, createTypeHandler(BytesAsBytesWrapTypeHandler.class));

        this.registerCross(Types.BINARY, InputStream.class, createTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.VARBINARY, InputStream.class, createTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.LONGVARBINARY, InputStream.class, createTypeHandler(BytesAsInputStreamTypeHandler.class));
        this.registerCross(Types.BLOB, InputStream.class, createTypeHandler(BlobAsInputStreamTypeHandler.class));

        this.registerCross(Types.ARRAY, Object.class, createTypeHandler(ArrayTypeHandler.class));

        this.registerCross(Types.DATALINK, String.class, createTypeHandler(StringTypeHandler.class));
        this.registerCross(Types.DATALINK, URL.class, createTypeHandler(URLTypeHandler.class));
        this.registerCross(Types.DATALINK, URI.class, createTypeHandler(URITypeHandler.class));

        this.registerCross(Types.ROWID, byte[].class, createTypeHandler(BytesTypeHandler.class));
        this.registerCross(Types.ROWID, Byte[].class, createTypeHandler(BytesAsBytesWrapTypeHandler.class));
        this.registerCross(Types.ROWID, String.class, createTypeHandler(StringTypeHandler.class));
    }

    private static void registerTypeHandlerType(TypeHandler<?> typeHandler) {
        if (typeHandler != null) {
            registerTypeHandlerType(typeHandler.getClass());
        }
    }

    private static void registerTypeHandlerType(Class<?> typeHandler) {
        String name = typeHandler.getName();
        if (!typeHandlerTypeCache.containsKey(name) && !typeHandler.isAnnotationPresent(NoCache.class)) {
            typeHandlerTypeCache.put(name, typeHandler);
        }
    }

    public TypeHandler<?> getHandlerByHandlerType(String handlerType) {
        return this.cachedByHandlerType.getOrDefault(handlerType, null);
    }

    public TypeHandler<?> getHandlerByHandlerType(Class<?> handlerType) {
        return this.cachedByHandlerType.getOrDefault(handlerType.getName(), null);
    }

    public TypeHandler<?> createTypeHandler(Class<?> typeHandler) {
        return this.createTypeHandler(typeHandler, () -> {
            try {
                return (TypeHandler<?>) typeHandler.newInstance();
            } catch (Exception e) {
                throw ExceptionUtils.toRuntime(e);
            }
        });
    }

    public TypeHandler<?> createTypeHandler(Class<?> typeHandler, Supplier<TypeHandler<?>> supplier) {
        if (typeHandler.isAnnotationPresent(NoCache.class)) {
            if (typeHandler == UnknownTypeHandler.class) {
                return this.defaultTypeHandler;
            } else {
                TypeHandler<?> handler = supplier.get();
                if (handler == null) {
                    return this.defaultTypeHandler;
                } else {
                    return handler;
                }
            }
        } else {
            registerTypeHandlerType(typeHandler);
            return this.cachedByHandlerType.computeIfAbsent(typeHandler.getName(), type -> {
                if (typeHandler == UnknownTypeHandler.class) {
                    return this.defaultTypeHandler;
                } else {
                    TypeHandler<?> handler = supplier.get();
                    if (handler == null) {
                        return this.defaultTypeHandler;
                    } else {
                        return handler;
                    }
                }
            });
        }
    }

    /** 注册 TypeHandler */
    public void register(int jdbcType, TypeHandler<?> typeHandler) {
        this.cachedByJdbcType.put(jdbcType, typeHandler);
        registerTypeHandlerType(typeHandler);
    }

    /** 注册 TypeHandler */
    public void register(Class<?> javaType, TypeHandler<?> typeHandler) {
        if (isAbstract(javaType)) {
            this.abstractCachedByJavaType.put(javaType, typeHandler);
        } else {
            this.cachedByJavaType.put(javaType.getName(), typeHandler);
        }
        registerTypeHandlerType(typeHandler);
    }

    public void registerCross(int jdbcType, Class<?> javaType, TypeHandler<?> typeHandler) {
        if (isAbstract(javaType)) {
            this.abstractCachedByCrossType.computeIfAbsent(javaType, k -> {
                return new LinkedHashMap<>();
            }).put(jdbcType, typeHandler);
        } else {
            this.cachedByCrossType.computeIfAbsent(javaType.getName(), k -> {
                return new ConcurrentHashMap<>();
            }).put(jdbcType, typeHandler);
        }

        registerTypeHandlerType(typeHandler);
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
    public void registerHandler(Class<?> handlerClass, TypeHandler<?> typeHandler) {
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
        return Collections.unmodifiableCollection(this.cachedByJavaType.values());
    }

    public Collection<String> getHandlerJavaTypes() {
        return Collections.unmodifiableCollection(this.cachedByJavaType.keySet());
    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type. */
    public static int toSqlType(final String javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType);
        if (jdbcType != null) {
            return jdbcType;
        }
        return Types.OTHER;

    }

    /** 根据 Java 类型Derive a default SQL type from the given Java type. */
    public static int toSqlType(final Class<?> javaType) {
        Integer jdbcType = javaTypeToJdbcTypeMap.get(javaType.getName());
        if (jdbcType != null) {
            return jdbcType;
        }
        return Types.OTHER;
    }

    public boolean hasTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        if (typeClass.isEnum() || typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            return true;
        }

        if (this.cachedByJavaType.containsKey(typeClass.getName())) {
            return true;
        }

        for (Class<?> abstractType : this.abstractCachedByJavaType.keySet()) {
            if (abstractType.isAssignableFrom(typeClass) || abstractType == typeClass) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTypeHandler(String typeName) {
        Objects.requireNonNull(typeName, "typeName is null.");
        return this.cachedByJavaType.containsKey(typeName);
    }

    public boolean hasTypeHandler(int jdbcType) {
        return this.cachedByJdbcType.containsKey(jdbcType);
    }

    public boolean hasTypeHandler(Class<?> typeClass, int jdbcType) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        if (typeClass.isEnum()) {
            return true;
        }
        Map<Integer, TypeHandler<?>> jdbcHandlerMap = this.cachedByCrossType.get(typeClass.getName());
        if (jdbcHandlerMap != null) {
            return jdbcHandlerMap.containsKey(jdbcType);
        }

        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            return true;
        }

        for (Class<?> abstractType : this.abstractCachedByCrossType.keySet()) {
            if (abstractType.isAssignableFrom(typeClass) || abstractType == typeClass) {
                return true;
            }
        }
        return false;
    }

    public TypeHandler<?> getTypeHandler(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            throw new NullPointerException("typeName is null.");
        }
        TypeHandler<?> typeHandler = this.cachedByJavaType.get(typeName);
        return (typeHandler != null) ? typeHandler : this.defaultTypeHandler;
    }

    public TypeHandler<?> getTypeHandler(Class<?> typeClass) {
        Objects.requireNonNull(typeClass, "typeClass is null.");
        String typeClassName = typeClass.getName();
        TypeHandler<?> typeHandler = this.cachedByJavaType.get(typeClassName);
        if (typeHandler != null) {
            return typeHandler;
        }

        // maybe classType include BindTypeHandler
        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            synchronized (this) {
                if (this.cachedByJavaType.containsKey(typeClassName)) {
                    return this.cachedByJavaType.get(typeClassName);
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

                this.cachedByJavaType.put(typeClassName, typeHandler);
                return typeHandler;
            }
        }

        // maybe classType is enum
        if (Enum.class.isAssignableFrom(typeClass)) {
            return this.cachedByJavaType.computeIfAbsent(typeClass.getName(), s -> {
                Class<?> enumType = typeClass.isAnonymousClass() ? typeClass.getSuperclass() : typeClass;
                return new EnumTypeHandler(enumType);
            });
        }
        // maybe classType is abstract
        for (Class<?> abstractType : this.abstractCachedByJavaType.keySet()) {
            if (abstractType.isAssignableFrom(typeClass) || abstractType == typeClass) {
                typeHandler = this.abstractCachedByJavaType.get(abstractType);
                break;
            }
        }

        // register default
        if (typeHandler == null) {
            typeHandler = this.defaultTypeHandler;
        }
        this.cachedByJavaType.put(typeClassName, typeHandler);
        return typeHandler;
    }

    public TypeHandler<?> getTypeHandler(int jdbcType) {
        TypeHandler<?> typeHandler = this.cachedByJdbcType.get(jdbcType);
        return (typeHandler != null) ? typeHandler : this.defaultTypeHandler;
    }

    /**
     * 根据 typeClass 和 jdbcType 的映射关系查找对应的 TypeHandler。
     * - 如果不存在对应的 TypeHandler，那么通过 typeClass 单独查找。
     * - 如果 typeClass 也没有注册那么返回 {@link #getDefaultTypeHandler()}
     */
    public TypeHandler<?> getTypeHandler(Class<?> typeClass, int jdbcType) {
        if (typeClass == null) {
            return this.defaultTypeHandler;
        }

        // find by classType and jdbcType
        String typeClassName = typeClass.getName();
        Map<Integer, TypeHandler<?>> handlerMap = this.cachedByCrossType.get(typeClassName);
        if (handlerMap != null) {
            TypeHandler<?> typeHandler = handlerMap.get(jdbcType);
            if (typeHandler != null) {
                return typeHandler;
            }
        }

        // find by classType
        TypeHandler<?> typeHandler = this.cachedByJavaType.get(typeClassName);
        if (typeHandler != null) {
            return typeHandler;
        }

        // maybe classType include BindTypeHandler
        if (typeClass.isAnnotationPresent(BindTypeHandler.class)) {
            synchronized (this) {
                if (this.cachedByCrossType.containsKey(typeClassName)) {
                    handlerMap = this.cachedByCrossType.get(typeClassName);
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
            typeHandler = this.cachedByJavaType.get(typeClass.getName());
            if (typeHandler == null) {
                EnumTypeHandler handler = new EnumTypeHandler(typeClass);
                registerCross(jdbcType, typeClass, handler);
                return handler;
            }
        }
        // maybe classType is abstract
        for (Class<?> abstractType : this.abstractCachedByCrossType.keySet()) {
            if (abstractType.isAssignableFrom(typeClass) || abstractType == typeClass) {
                Map<Integer, TypeHandler<?>> typeHandlerMap = this.abstractCachedByCrossType.get(typeClass);
                typeHandler = typeHandlerMap.get(jdbcType);
                break;
            }
        }

        // register default
        if (typeHandler == null) {
            typeHandler = this.defaultTypeHandler;
        }
        registerCross(jdbcType, typeClass, typeHandler);
        return typeHandler;
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

        if (value instanceof SqlArg) {
            SqlArg arg = (SqlArg) value;
            Integer argType = arg.getJdbcType();
            TypeHandler argHandler = arg.getTypeHandler();
            Object argValue = arg.getValue();

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

    /** 一个工具方法，会根据 value Type 自动的选择对应的 TypeHandler */
    public void setParameterValue(final CallableStatement cs, final int parameterPosition, final Object value) throws SQLException {
        SqlMode sqlMode;
        Integer jdbcType;
        String typeName;
        Integer scale;
        Class<?> javaType;
        if (value instanceof SqlArg) {
            sqlMode = ((SqlArg) value).getSqlMode();
            sqlMode = sqlMode == null ? SqlMode.In : sqlMode;
            jdbcType = ((SqlArg) value).getJdbcType();
            typeName = ((SqlArg) value).getJdbcTypeName();
            javaType = ((SqlArg) value).getJavaType();
            scale = ((SqlArg) value).getScale();
        } else {
            sqlMode = SqlMode.In;
            jdbcType = null;
            typeName = null;
            scale = null;
            javaType = null;
        }

        if (sqlMode.isIn()) {
            this.setParameterValue((PreparedStatement) cs, parameterPosition, value);
        }

        if (sqlMode.isOut()) {
            if (sqlMode == SqlMode.Cursor) {
                if (StringUtils.equals(JdbcUtils.getDbType(cs), JdbcUtils.ORACLE)) {
                    jdbcType = -10;// oracle driver oracle.jdbc.OracleTypes.CURSOR = -10
                } else {
                    jdbcType = Types.REF_CURSOR;
                }
                cs.registerOutParameter(parameterPosition, jdbcType);
            } else {
                if (jdbcType == null && javaType != null) {
                    jdbcType = TypeHandlerRegistry.toSqlType(javaType);
                }
                if (jdbcType == null) {
                    throw new SQLException("jdbcType must not be null");
                }

                if (typeName != null) {
                    cs.registerOutParameter(parameterPosition, jdbcType, typeName);
                } else if (scale != null) {
                    cs.registerOutParameter(parameterPosition, jdbcType, scale);
                } else {
                    cs.registerOutParameter(parameterPosition, jdbcType);
                }
            }
        }
    }

    private static boolean isAbstract(Class<?> javaType) {
        if (javaType.isArray()) {
            javaType = javaType.getComponentType();
        }
        if (javaType.isPrimitive()) {
            return false;
        }

        int modifiers = javaType.getModifiers();
        if (javaType.isInterface() || Modifier.isAbstract(modifiers)) {
            return true;
        } else {
            return false;
        }
    }

    public Object getParameterValue(CallableStatement cs, int i, SqlArg arg) throws SQLException {
        TypeHandler<?> argHandler = arg.getTypeHandler();
        Class<?> argJavaType = arg.getJavaType();
        Integer argJdbcType = arg.getJdbcType();

        if (argHandler == null) {
            if (argJavaType != null && argJdbcType != null && this.hasTypeHandler(argJavaType, argJdbcType)) {
                argHandler = this.getTypeHandler(argJavaType, argJdbcType);
            } else if (argJavaType != null && this.hasTypeHandler(argJavaType)) {
                argHandler = this.getTypeHandler(argJavaType);
            } else if (argJdbcType != null && this.hasTypeHandler(argJdbcType)) {
                argHandler = this.getTypeHandler(argJdbcType);
            } else {
                argHandler = this.getDefaultTypeHandler();
            }
        }

        return argHandler.getResult(cs, i);
    }
}
