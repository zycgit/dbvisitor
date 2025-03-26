---
id: stream-handler
sidebar_position: 7
title: 8.7 流类型处理器
description: dbVisitor 处理流类型的类型处理器。
---

# 流类型处理器

流类型处理器位于 `net.hasor.dbvisitor.types.handler.io` 包中。

| 类型处理器                          | Java 类型             | 作用                                                |
|--------------------------------|---------------------|---------------------------------------------------|
| BlobAsInputStreamTypeHandler   | java.io.InputStream | 以 getBlob/setBlob 方法 流的方式读写数据                     |
| BytesAsInputStreamTypeHandler  | java.io.InputStream | 以 getBytes/setBytes 方法，将流中的数据拷贝到内存在进行读写           |
| SqlXmlAsInputStreamTypeHandler | java.io.InputStream | 以 getSQLXML/setSQLXML，并借助其 binaryStream 进行数据读写    |
| ClobAsReaderTypeHandler        | java.io.Reader      | 以 getClob/setClob 方法 流的方式读写数据                     |
| NClobAsReaderTypeHandler       | java.io.Reader      | 以 getNClob/setNClob 方法 流的方式读写数据                   |
| NStringAsReaderTypeHandler     | java.io.Reader      | 以 getNString/setNString 方法，将流中的数据拷贝到内存在进行读写       |
| SqlXmlAsReaderTypeHandler      | java.io.Reader      | 以 getSQLXML/setSQLXML，并借助其 characterStream 进行数据读写 |
| StringAsReaderTypeHandler      | java.io.Reader      | 以 getString/setString 方法，将流中的数据拷贝到内存在进行读写         |
