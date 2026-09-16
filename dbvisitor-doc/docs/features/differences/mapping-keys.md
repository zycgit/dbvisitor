---
id: mapping-keys
sidebar_position: 6
title: 6. 对象映射
hide_table_of_contents: true
---

import CapabilityTable from '@site/src/components/CapabilityTable';
import matrix from '@site/src/data/capabilities/tables/mapping-keys.json';

通用用法见核心 API 的[5.7 对象映射](../../guides/core/mapping/about)，状态链接说明各数据源的具体差异。

<CapabilityTable matrix={matrix} showCounts={false} />

- 映射表：默认映射、显式映射及忽略属性。
- 名称敏感性：数据库中的表名、列名大小写规则，以及 `useDelimited` 名称引用。
- 结果列大小写：使用 `caseInsensitive` 控制返回列名与属性的匹配。
- 写入策略：控制字段参与 INSERT、UPDATE，以及 NULL 和部分字段写入。
- JSON 字段映射：实体属性映射到单个字段；直接转换 JSON 值见[类型处理器](./types#json)。
- 主键策略：手工赋值、自增、UUID、序列及自定义生成；状态链接说明各数据源的策略边界。

空字符串和数组的往返读取统一见[类型处理器](./types)。
