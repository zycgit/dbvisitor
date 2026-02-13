---
id: automapping
sidebar_position: 7
hide_table_of_contents: true
title: 自动映射
description: 了解 dbVisitor 中 Mapper 文件里自动映射的功能。
---

# 自动映射

Mapper 文件中 &lt;resultMap&gt; 或 &lt;entity&gt; 标签通过 `autoMapping` 属性默认启用自动映射。
在自动映射模式下无需为每个列进行映射，dbVisitor 会根据预定义的规则自动发现类型的属性并将其映射到列上。

```xml title='以 &lt;resultMap&gt; 标签为例'
<resultMap id="userResultMap" 
           type="com.example.dto.UserBean" 
           autoMapping="true"/> <!-- 默认 autoMapping 属性为 true 可不配置 -->
```

:::caution[注意两点]
- 一旦 &lt;resultMap&gt; 或 &lt;entity&gt; 标签中包含了 `id`、`result`、`mapping` 中的任意子标签，自动映射将不再生效，仅使用手动声明的列映射。
- 当属性标有 @Column 注解，无论 `autoMapping` 属性是任何值都会将其映射为列。
:::

## 驼峰命名法

通常数据库列的命名使用大写字母和下划线，这与 Java 通常遵循驼峰命名约定有一定的差异。通过 `mapUnderscoreToCamelCase` 属性可以修正这种差异。

```xml title='以 &lt;resultMap&gt; 标签为例'
<resultMap id="userResultMap"
           type="com.example.dto.UserBean"
           mapUnderscoreToCamelCase="true"/>
```
