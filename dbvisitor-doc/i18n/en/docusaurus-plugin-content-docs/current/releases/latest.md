---
id: latest
sidebar_position: 0
title: 最新版本
---

# 版本说明

## 版本定义

**版本号定义**：主版本号.次版本号.修订版本

- **主版本**
    - 架构发生重大变化
- **次版本**
    - 不兼容更新
    - 新功能新特性
- **修订版本**
    - Bug 修复

## 最新版本

v5.4.0 (2023-08-24)

## 版本事件
- `5.x` 及以后，dbVisitor 完全独立后的正式迭代版本
- `4.x` 及以前，dbVisitor 尚归属于 `hasor` 时期的版本

## 单独使用
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>5.4.0</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor:jar
 +- net.hasor:cobble-all:jar
```

## 整合 Spring Boot
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-spring-starter</artifactId>
    <version>5.4.0</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-spring-starter:jar
+- net.hasor:dbvisitor-spring:jar
   +- net.hasor:dbvisitor:jar
      +- net.hasor:cobble-all:jar
+- org.springframework.boot:spring-boot-autoconfigure:jar
+- org.springframework.boot:spring-boot-starter-jdbc:jar
```

## 整合 Spring
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-spring</artifactId>
    <version>5.4.0</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-spring:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar
+- org.springframework:spring-context:jar
+- org.springframework:spring-jdbc:jar
```

## 整合 Guice
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-guice</artifactId>
    <version>5.4.0</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-guice:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar
+- com.google.inject:guice:jar
```

## 整合 Hasor
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-hasor</artifactId>
    <version>5.4.0</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-hasor:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar
+- net.hasor:hasor-core:jar
```
