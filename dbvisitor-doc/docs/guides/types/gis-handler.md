---
sidebar_position: 3
title: 地理信息类型处理器
description: dbVisitor ORM 工具处理枚举类型映射。
---

# 地理信息类型处理器

读写数据库中的地理信息类型数据需要引入 JTS 依赖

```xml
<dependency>
   <groupId>org.locationtech.jts</groupId>
   <artifactId>jts-core</artifactId>
   <version>1.19.0</version>
</dependency>
```

## 标准

OpenGIS 规范定义了两种具体格式
- Well-Known Text (WKT) format，字符串形式
- Well-Known Binary (WKB) format，字节数组 btye[]形式

## 存储用WKT

以 WKT 格式读出并转换为 WKB；或以 WKB 为入参，最终以 WKT 写入数据库。
- 这种场景下使用 `JtsGeometryWktAsWkbTypeHandler`

```text
数据库     应用
 WKT  ->  WKB
 WKT  <-  WKB
```

## 存储用WKB

以 WKB 格式读出并转换为 WKT；或以 WKT 为入参，最终以 WKB 写入数据库。
- 这种场景下使用 `JtsGeometryWkbAsWktTypeHandler`

```text
数据库     应用
 WKB  ->  WKT
 WKB  <-  WKT
```

## 存储用WKB(HEX)

以 WKB(HEX) 格式读出并转换为 WKT；或以 WKT 为入参，最终以 WKB(HEX) 写入数据库。
- 这种场景下使用 `JtsGeometryWkbHexAsWktTypeHandler`

```text
  数据库       应用
WKB(HEX)  ->  WKT
WKB(HEX)  <-  WKT
```
