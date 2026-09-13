---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: Business Scenarios
---

These scenarios use annotation Mappers to execute Redis commands. Choose a data structure for the business need, then read a scalar, a JSON object or multiple result rows.

| Scenario | Redis structure | Java result |
| --- | --- | --- |
| [Product Cache](product-cache.md) | String / JSON | ProductCache |
| [Login State](login-state.md) | String / JSON | LoginState |
| [Shopping Cart](shopping-cart.md) | Hash | List&lt;CartItem&gt; |
| [View Counter](view-counter.md) | String / integer | Long |
| [User Likes](article-likes.md) | Set | List&lt;String&gt; |
| [Points Ranking](points-ranking.md) | Sorted Set | List&lt;RankEntry&gt; |

## Create a Session

Configure dataSource using [JDBC Redis](../../../drivers/redis/connection.mdx). Run each page's calling code inside a Session:

```java
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

try (Session session = new Configuration().newSession(dataSource)) {
    // Run the scenario's calling code here.
}
```

Place the example models and Mappers in the `com.example.redis` package. Examples use `demo:` keys and assume empty test keys. Repeating them may overwrite data or increase counts; do not use business keys.
