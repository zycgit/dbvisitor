---
id: gis-handler
sidebar_position: 6
title: 8.6 地理信息类型处理器
description: dbVisitor 基于 JTS 处理 OpenGIS 地理信息类型（WKT/WKB）的类型处理器。
---

# 地理信息类型处理器

地理信息类型处理器位于 `net.hasor.dbvisitor.types.handler.geo` 包中，使用前需引入 JTS 依赖：

```xml
<dependency>
   <groupId>org.locationtech.jts</groupId>
   <artifactId>jts-core</artifactId>
   <version>1.19.0</version>
</dependency>
```

## OpenGIS 标准格式

OpenGIS 规范定义了两种地理信息表示格式：
- **WKT**（Well-Known Text）— 字符串形式，如 `POINT (30 10)`
- **WKB**（Well-Known Binary）— 字节数组 `byte[]` 形式

## 内置处理器

根据数据库存储格式和应用层使用类型的不同，选择对应的处理器：

| 类型处理器 | 数据库格式 | Java 类型 | 说明 |
|---|---|---|---|
| `JtsGeometryWktAsWkbTypeHandler` | WKT (String) | `byte[]` | 读时 WKT→WKB，写时 WKB→WKT |
| `JtsGeometryWkbAsWktTypeHandler` | WKB (byte[]) | `String` | 读时 WKB→WKT，写时 WKT→WKB |
| `JtsGeometryWkbHexAsWktTypeHandler` | WKB HEX (String) | `String` | 读时 HEX→WKB→WKT，写时 WKT→WKB→HEX |

### 存储用 WKT {#wkt}

数据库以 WKT(String) 格式存储，应用层使用 WKB(byte[])：

```text
数据库           应用
WKT (String) → WKB (byte[])
WKT (String) ← WKB (byte[])
```

使用 `JtsGeometryWktAsWkbTypeHandler`

### 存储用 WKB {#wkb}

数据库以 WKB(byte[]) 格式存储，应用层使用 WKT(String)：

```text
数据库          应用
WKB (byte[]) → WKT (String)
WKB (byte[]) ← WKT (String)
```

使用 `JtsGeometryWkbAsWktTypeHandler`

### 存储用 WKB HEX {#hex}

数据库以 WKB 的 HEX 编码(String) 格式存储，应用层使用 WKT(String)：

```text
数据库              应用
WKB HEX (String) → WKT (String)
WKB HEX (String) ← WKT (String)
```

使用 `JtsGeometryWkbHexAsWktTypeHandler`
