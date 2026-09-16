---
id: parameters
sidebar_position: 7
title: 7. 参数传递与规则
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/parameters.json';

通用用法见核心 API 的[参数传递](../../guides/args/about)和[SQL 规则](../../guides/rules/about)。这里集中比较各 API 共用的参数能力，状态链接说明数据源差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 位置参数：编程式调用和方法注解按位置绑定值，包含 NULL。
- 名称参数：编程式调用、方法注解和 Mapper 文件通过名称读取 Map、Bean 及嵌套属性。
- 文本替换：仅用于可信标识符或命令片段，用户输入的值使用绑定参数。
- 混合参数源：混合使用 Array、Bean、Map 提供参数。
- 显式类型：通过 SqlArg 指定 JDBC 类型和 TypeHandler。
- 绑定与复用：PreparedStatement 的值绑定、参数复用、NULL 与空串处理。
- 通用规则：按条件选择原生命令，只绑定选中分支的参数。
- SQL 片段规则：生成 SQL 的 AND、OR、IN、SET 片段，不负责转换成非 SQL 命令。
