---
id: stream-handler
sidebar_position: 7
title: 8.7 流类型处理器
description: dbVisitor 处理流类型的类型处理器。
---

# 流类型处理器

流类型处理器位于 `net.hasor.dbvisitor.types.handler.io` 包中。

## InputStream 处理器

| 类型处理器 | 作用 |
|---|---|
| `BlobAsInputStreamTypeHandler` | 以 getBlob/setBlob 方式，以流形式直接读写 BLOB 数据 |
| `BytesAsInputStreamTypeHandler` | 以 getBytes/setBytes 方式，将流数据拷贝到内存后进行读写 |
| `SqlXmlAsInputStreamTypeHandler` | 以 getSQLXML/setSQLXML 方式，借助 binaryStream 进行数据读写 |

## Reader 处理器

| 类型处理器 | 作用 |
|---|---|
| `ClobAsReaderTypeHandler` | 以 getClob/setClob 方式，以流形式直接读写 CLOB 数据 |
| `NClobAsReaderTypeHandler` | 以 getNClob/setNClob 方式，以流形式直接读写 NCLOB 数据 |
| `NStringAsReaderTypeHandler` | 以 getNString/setNString 方式，将流数据拷贝到内存后进行读写 |
| `SqlXmlAsReaderTypeHandler` | 以 getSQLXML/setSQLXML 方式，借助 characterStream 进行数据读写 |
| `StringAsReaderTypeHandler` | 以 getString/setString 方式，将流数据拷贝到内存后进行读写 |
