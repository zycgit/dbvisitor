---
id: bytes-handler
sidebar_position: 6
title: Byte Array Types
description: Type handlers for byte arrays in dbVisitor.
---

# Byte Array Type Handlers

Byte array type handlers are located in the `net.hasor.dbvisitor.types.handler.bytes` package.

## Handler List

| Handler | Java Type | Purpose |
|---|---|---|
| `BytesTypeHandler` | `byte[]` | Reads/writes byte[] data via getBytes/setBytes |
| `BytesAsBytesWrapTypeHandler` | `java.lang.Byte[]` | Reads/writes Byte[] wrapper type data via getBytes/setBytes |
| `BlobAsBytesTypeHandler` | `byte[]` | Reads/writes byte[] data via getBlob/setBlob |
| `BlobAsBytesWrapTypeHandler` | `java.lang.Byte[]` | Reads/writes Byte[] wrapper type data via getBlob/setBlob |
