---
id: buildtools
sidebar_position: 0
hide_table_of_contents: true
title: 引入依赖
description: 在熟悉的构建工具中引入 dbVisitor。
---
import Vars from '@site/plugins/projectVars';

export const Highlight = ({children, color}) => (
  <span style={{ backgroundColor: color, borderRadius: '2px', color: '#fff', padding: '0.2rem', }}>{children}</span>
);

# 引入依赖

所有 dbVisitor 模块以及依赖项都可以通过 [Maven Central](https://central.sonatype.com/search?q=dbvisitor) 获得。
因此任何使用依赖管理工具（Apache Maven、Gradle、sbt、leiningen、Apache Ivy 等）的项目都可以访问这些模块。

截止到目前为止 dbVisitor 的最新版本为：**<Highlight color="rgb(227 17 108)">{Vars.lastReleaseVer}</Highlight>**

## Apache Maven

在使用 Apache Maven 的项目中引入 dbVisitor

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>最新版本的版本号</version>
</dependency>
```

## Gradle

在使用 Gradle 的项目中引入 dbVisitor

```text
// Gradle
implementation 'net.hasor:dbvisitor:最新版本的版本号'
```

## 混合 dbVisitor 不同版本

dbVisitor 无法保证不同版本之间能够相互协同工作（例如：dbVisitor 5.3.1 和 dbVisitor 6.0.0）
在项目或服务中使用的所有 dbVisitor 组件都应使用相同的版本。

## 框架整合 {#integration}
- 在 Java 程序中通过 [原始的方式](./with_java) 使用 dbVisitor。
- 利用 [dbvisitor-guice](./with_guice) 在 Google Guice 中使用 dbVisitor。
- 利用 [dbvisitor-spring](./with_spring) 在 Spring、SpringBoot 中使用 dbVisitor。
- 利用 [dbvisitor-solon](./with_solon) 在 Solon 中使用 dbVisitor。
- 利用 [dbvisitor-hasor](./with_hasor) 在 Hasor 中使用 dbVisitor。