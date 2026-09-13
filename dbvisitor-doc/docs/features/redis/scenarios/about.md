---
id: about
sidebar_position: 0
hide_table_of_contents: true
title: 业务场景
---

这些场景使用方法注解 Mapper 执行 Redis 命令。按业务选择数据结构，再决定是读取一个值、一个 JSON 对象，还是多行结果。

| 场景 | Redis 数据结构 | Java 结果 |
| --- | --- | --- |
| [商品缓存](product-cache.md) | String / JSON | ProductCache |
| [登录状态](login-state.md) | String / JSON | LoginState |
| [购物车](shopping-cart.md) | Hash | List&lt;CartItem&gt; |
| [浏览计数](view-counter.md) | String / integer | Long |
| [用户点赞](article-likes.md) | Set | List&lt;String&gt; |
| [积分排行榜](points-ranking.md) | Sorted Set | List&lt;RankEntry&gt; |

## 创建 Session

先按 [JDBC Redis](../../../drivers/redis/connection.mdx) 配置 dataSource。各页的调用代码放在同一个 Session 中执行：

```java
import java.util.List;
import java.util.UUID;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

try (Session session = new Configuration().newSession(dataSource)) {
    // 在此执行场景中的调用代码。
}
```

示例模型和 Mapper 放在 `com.example.redis` 包中。示例使用 `demo:` 前缀，在空的测试键上运行；重复运行可能覆盖数据或累加计数，不要使用业务键。
