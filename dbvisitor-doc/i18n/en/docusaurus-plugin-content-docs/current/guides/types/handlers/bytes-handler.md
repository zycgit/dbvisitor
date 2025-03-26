---
id: bytes-handler
sidebar_position: 6
hide_table_of_contents: true
title: 字节数组类型
description: dbVisitor 处理字节数组类型的类型处理器。
---

# 字节数组类型处理器

字节数组类型处理器位于 `net.hasor.dbvisitor.types.handler.bytes` 包中。

| 类型处理器                       | Java 类型          | 作用                                           |
|-----------------------------|------------------|----------------------------------------------|
| BytesTypeHandler            | byte[]           | 以 getBytes/setBytes 方式处理 byte[] 数据           |
| BytesAsBytesWrapTypeHandler | java.lang.Byte[] | 以 getBytes/setBytes 方式处理 java.lang.Byte[] 数据 |
| BlobAsBytesTypeHandler      | byte[]           | 以 getBlob/setBlob 方式处理 byte[] 数据             |
| BlobAsBytesWrapTypeHandler  | java.lang.Byte[] | 以 getBlob/setBlob 方式处理 java.lang.Byte[] 数据   |
