---
id: buildtools
sidebar_position: 1
hide_table_of_contents: true
title: 3.1 引入依赖
description: 在熟悉的构建工具中引入 dbVisitor。
---
import Vars from '@site/plugins/projectVars';

export const Highlight = ({children, color}) => (
  <span style={{ backgroundColor: color, borderRadius: '2px', color: '#fff', padding: '0.2rem', }}>{children}</span>
);

# 3.1 引入依赖

dbVisitor 正式发布的模块可以通过 [Maven Central](https://central.sonatype.com/search?q=dbvisitor) 获得。
因此任何使用依赖管理工具（Apache Maven、Gradle、sbt、leiningen、Apache Ivy 等）的项目都可以访问这些模块。

dbVisitor 最新正式版本为：**<Highlight color="rgb(227 17 108)">{Vars.lastReleaseVer}</Highlight>**

当前使用指南对应 **{Vars.docsVersion}**。SNAPSHOT 不在 Maven Central 正式版列表中，使用前需从源码构建并安装到本地 Maven 仓库；不要把开发版能力直接套用到旧版。

## 选择接入方式

| 项目类型 | 推荐模块 | 入口 |
| --- | --- | --- |
| 普通 Java 项目 | `dbvisitor` | [普通项目](./with_java) |
| Spring / Spring Boot 项目 | `dbvisitor-spring` / `dbvisitor-spring-starter` | [Spring 整合](./with_spring) |
| Solon 项目 | `dbvisitor-solon-plugin` | [Solon 整合](./with_solon) |
| Hasor 项目 | `dbvisitor-hasor` | [Hasor 整合](./with_hasor) |
| Guice 项目 | `dbvisitor-guice` | [Guice 整合](./with_guice) |

## Apache Maven

在使用 Apache Maven 的项目中引入 dbVisitor

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>@project.docsVersion@</version>
</dependency>
```

## Gradle

在使用 Gradle 的项目中引入 dbVisitor

```text
// Gradle
implementation 'net.hasor:dbvisitor:@project.docsVersion@'
```

## 混合 dbVisitor 不同版本

dbVisitor 无法保证不同版本之间能够相互协同工作（例如：dbVisitor 5.3.1 和 dbVisitor 6.0.0）
在项目或服务中使用的所有 dbVisitor 组件都应使用相同的版本。

## 框架整合 {#integration}

- 在 Java 程序中通过 [普通项目](./with_java) 方式使用 dbVisitor。
- 利用 [dbvisitor-guice](./with_guice) 在 Google Guice 中使用 dbVisitor。
- 利用 [dbvisitor-spring](./with_spring) 在 Spring、SpringBoot 中使用 dbVisitor。
- 利用 [dbvisitor-solon](./with_solon) 在 Solon 中使用 dbVisitor。
- 利用 [dbvisitor-hasor](./with_hasor) 在 Hasor 中使用 dbVisitor。
