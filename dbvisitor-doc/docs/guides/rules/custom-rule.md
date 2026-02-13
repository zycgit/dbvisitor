---
id: custom-rule
sidebar_position: 5
hide_table_of_contents: true
title: 7.4 自定义规则
description: 当 dbVisitor 内置规则无法满足需要时。可以通过自定义方式拓展所需规则。
---

# 自定义规则

当 dbVisitor 内置规则无法满足需要时，可以通过实现 `SqlRule` 接口来拓展自定义规则。

## 实现步骤

```java title='1. 实现 SqlRule 接口'
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.SqlRule;
import net.hasor.dbvisitor.internal.OgnlUtils;
import net.hasor.cobble.StringUtils;

public class MyRule implements SqlRule {
    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        // activeExpr 为 @{规则名, 激活条件, 规则内容} 中的"激活条件"
        // 返回 true 表示该规则应该执行
        return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(OgnlUtils.evalOgnl(activeExpr, data));
    }

    @Override
    public void executeRule(SqlArgSource data, QueryContext context,
                            SqlBuilder sqlBuilder, String activeExpr, String ruleValue) {
        // ruleValue 为 @{规则名, 激活条件, 规则内容} 中的"规则内容"
        // 通过 sqlBuilder 拼接生成的 SQL 片段
        sqlBuilder.appendSql("my_custom_value");
    }
}
```

```java title='2. 注册规则（二选一）'
// 方式 1：通过 Configuration 注册（推荐）
Configuration config = new Configuration();
config.addSqlRule("myrule", new MyRule());

// 方式 2：注册到全局默认注册表
RuleRegistry.DEFAULT.register("myrule", new MyRule());
```

```sql title='3. 使用规则'
select * from users where id = @{myrule, true, xxxx}
```

## 接口说明

`SqlRule` 接口定义了两个方法：

| 方法 | 说明 |
|------|------|
| `test(data, context, activeExpr)` | 判断规则是否应执行。返回 `false` 时整个 `@{...}` 会被跳过。 |
| `executeRule(data, context, sqlBuilder, activeExpr, ruleValue)` | 执行规则逻辑，通过 `sqlBuilder` 拼接 SQL 片段和参数。 |

**参数解析规则**：`@{ruleName, activeExpr, ruleValue}` 中第一个逗号前为规则名，第二个逗号前为 `activeExpr`，之后的所有内容为 `ruleValue`。

:::tip[提示]
规则名不区分大小写，注册为 `"myrule"` 的规则可以通过 `@{MyRule}`、`@{MYRULE}` 等方式调用。
:::
