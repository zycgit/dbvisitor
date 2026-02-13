---
id: buildtools
sidebar_position: 0
hide_table_of_contents: true
title: Add Dependency
description: Introduce dbVisitor in your familiar build tools.
---
import Vars from '@site/plugins/projectVars';

export const Highlight = ({children, color}) => (
  <span style={{ backgroundColor: color, borderRadius: '2px', color: '#fff', padding: '0.2rem', }}>{children}</span>
);

# Add Dependency

All dbVisitor modules and dependencies are available via [Maven Central](https://central.sonatype.com/search?q=dbvisitor).
Therefore, any project using dependency management tools (Apache Maven, Gradle, sbt, leiningen, Apache Ivy, etc.) can access these modules.

As of now, the latest version of dbVisitor is: **<Highlight color="rgb(227 17 108)">{Vars.lastReleaseVer}</Highlight>**

## Apache Maven

Introduce dbVisitor in a project using Apache Maven

```xml
<dependency>
    <groupId>net.hasor</groupId>
    <artifactId>dbvisitor</artifactId>
    <version>latest version</version>
</dependency>
```

## Gradle

Introduce dbVisitor in a project using Gradle

```text
// Gradle
implementation 'net.hasor:dbvisitor:latest version'
```

## Mixing Different Versions of dbVisitor

dbVisitor cannot guarantee that different versions will work together (e.g., dbVisitor 5.3.1 and dbVisitor 6.0.0).
All dbVisitor components used in a project or service should use the same version.

## Framework Integration {#integration}
- Use dbVisitor in a Java program via [the plain way](./with_java).
- Use [dbvisitor-guice](./with_guice) to use dbVisitor in Google Guice.
- Use [dbvisitor-spring](./with_spring) to use dbVisitor in Spring, SpringBoot.
- Use [dbvisitor-solon](./with_solon) to use dbVisitor in Solon.
- Use [dbvisitor-hasor](./with_hasor) to use dbVisitor in Hasor.
