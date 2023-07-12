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

v5.3.3 (2023-07-12)

## 版本事件
- `5.x` 及以后，dbVisitor 完全独立后的正式迭代版本
- `4.3.x` 是中间版本，区别是包名仍然属于 `net.hasor.db` 只是 jar 包引用改为 `dbVisitor`
- `4.2.x` 及以前，是 dbVisitor 尚归属于 `hasor-db` 时期的版本

## 单独使用
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>5.3.3</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor:jar
 +- net.hasor:cobble-all:jar(4.5.3)
```

## 整合 Spring Boot
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-spring-starter</artifactId>
    <version>5.3.3</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-spring-starter:jar
+- net.hasor:dbvisitor-spring:jar
   +- net.hasor:dbvisitor:jar
      +- net.hasor:cobble-all:jar(4.5.3)
+- org.springframework.boot:spring-boot-autoconfigure:jar(2.7.2)
+- org.springframework.boot:spring-boot-starter-jdbc:jar(2.7.2)
```

## 整合 Spring
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-spring</artifactId>
    <version>5.3.3</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-spring:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar(4.5.3)
+- org.springframework:spring-context:jar(4.0.0.RELEASE)
+- org.springframework:spring-jdbc:jar(4.0.0.RELEASE)
```

## 整合 Guice
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-guice</artifactId>
    <version>5.3.3</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-guice:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar(4.5.3)
+- com.google.inject:guice:jar(5.1.0)
```

## 整合 Hasor
```xml title='Maven 依赖'
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor-hasor</artifactId>
    <version>5.3.3</version>
</dependency>
```
```text title='依赖关系'
net.hasor:dbvisitor-hasor:jar
+- net.hasor:dbvisitor:jar
   +- net.hasor:cobble-all:jar(4.5.3)
+- net.hasor:hasor-core:jar(4.2.5)
```
