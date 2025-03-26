---
id: number-handler
sidebar_position: 3
hide_table_of_contents: true
title: 数字类型
description: dbVisitor 处理数字类型的类型处理器。
---

# 数字类型处理器

数字类型处理器位于 `net.hasor.dbvisitor.types.handler.number` 包中。

## 基础类型

| 类型处理器              | Java 类型                  | 作用                |
|--------------------|--------------------------|-------------------|
| ByteTypeHandler    | java.lang.Byte, byte     | 处理 byte 类型数据的读写   |
| ShortTypeHandler   | java.lang.Short, short   | 处理 short 类型数据的读写  |
| IntegerTypeHandler | java.lang.Integer, int   | 处理 int 类型数据的读写    |
| LongTypeHandler    | java.lang.Long, long     | 处理 long 类型数据的读写   |
| FloatTypeHandler   | java.lang.Float, float   | 处理 float 类型数据的读写  |
| DoubleTypeHandler  | java.lang.Double, double | 处理 double 类型数据的读写 |

## 对象类型

| 类型处理器                 | Java 类型              | 作用                    |
|-----------------------|----------------------|-----------------------|
| NumberTypeHandler     | java.lang.Number     | **只支持数据读取**           |
| BigDecimalTypeHandler | java.math.BigDecimal | 处理 BigDecimal 类型数据的读写 |
| BigIntegerTypeHandler | java.math.BigInteger | 处理 BigInteger 类型数据的读写 |

## 类型转换

| 类型处理器                         | Java 类型                    | 作用                                    |
|-------------------------------|----------------------------|---------------------------------------|
| IntegerAsBooleanTypeHandler   | java.lang.Boolean, boolean | 用于数值类型和布尔类型的映射，任何一个非零的整数都会被解析为 true。  |
| StringAsBigDecimalTypeHandler | java.math.BigDecimal       | 超大数读写，使用 BigDecimal 类型读写数据库 string 数据 |
| StringAsBigIntegerTypeHandler | java.math.BigInteger       | 超大数读写，使用 BigInteger 类型读写数据库 string 数据 |

## 特殊支持

| 类型处理器                          | Java 类型              | 作用                     |
|--------------------------------|----------------------|------------------------|
| PgMoneyAsBigDecimalTypeHandler | java.math.BigDecimal | 支持 PostgreSQL，Money 类型 |
