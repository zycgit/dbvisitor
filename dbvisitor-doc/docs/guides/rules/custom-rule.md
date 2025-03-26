---
id: custom-rule
sidebar_position: 6
hide_table_of_contents: true
title: 7.6 自定义规则
description: 当 dbVisitor 内置规则无法满足需要时。可以通过自定义方式拓展所需规则。
---

# 自定义规则

当 dbVisitor 内置规则无法满足需要时。可以通过自定义方式拓展所需规则。

```java title='1. 实现 SqlBuildRule 接口'
import net.hasor.dbvisitor.internal.OgnlUtils;
import net.hasor.dbvisitor.dynamic.rule.SqlBuildRule;
public class MyRule implements SqlBuildRule {
    @Override
    public boolean test(SqlArgSource data, DynamicContext context, String activeExpr) {
        return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(OgnlUtils.evalOgnl(activeExpr, data));
    }

    @Override
    public void executeRule(SqlArgSource data, DynamicContext context, 
                            SqlBuilder sqlBuilder, String activeExpr,
                            String ruleValue) {
        ...
        SqlArg arg = new SqlArg(expr, value, sqlMode, jdbcType, javaType, typeHandler);
        sqlBuilder.appendSql("?", arg);
    }
}
```

```java title='2. 注册规则'
RuleRegistry.DEFAULT.register("myrule", new MyRule());
```

```sql title='3. 使用规则'
select * from `test_user` @{myrule, true, xxxx}
```
