---
id: builder
sidebar_position: 3
title: 3. 构造器 API
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/builder.json';

通用用法见核心 API 的[5.3 构造器 API](../../guides/core/lambda/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 写入操作：新增、更新、删除，包括多条操作、空条件保护、影响条数及写入后回读。
- 写入冲突：默认插入、Ignore 和 Update 策略。
- 查询操作：选择字段、实体与 Map 结果、标量、计算列、计数及去重。
- Map 查询模式：映射 Map 使用 Java 属性名，自由 Map 使用数据库列名，均包含读写操作。
- 分页查询：按页查询、总数、翻页及分页迭代。
- 条件构造器：比较、区间、集合、LIKE、NULL、空字符串及条件组。
- 条件参数：条件方法和 apply 的值绑定，包括特殊字符与边界值。
- 分组：分组查询及聚合结果。
- 排序：升降序、多字段优先级及空值顺序。
