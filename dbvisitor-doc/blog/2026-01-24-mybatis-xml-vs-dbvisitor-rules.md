---
slug: mybatis-xml-vs-dbvisitor-rules
title: MyBatis 史诗级裹脚布可以烧了
authors: [ZhaoYongChun]
tags: [dbVisitor, MyBatis, ORM]
---
 
作为一名 Java 开发者，你一定经历过被 MyBatis XML 支配的恐惧。

当你打开一个 `UserMapper.xml`，迎面而来的是几百行甚至上千行的 `<if>`, `<where>`, `<choose>`, `<foreach>` 标签。原本清爽的 SQL 语句被这些 XML 标签切割得支离破碎，仿佛老太太的裹脚布——又臭又长。

如果你也受够了在 XML 标签里写逻辑，受够了为了一个简单的非空判断就要写三行 XML，那么请继续往下看。dbVisitor 的动态 SQL 规则机制，也许就是你一直在寻找的"剪刀"。

<!--truncate-->

## MyBatis 的 XML 地狱

让我们先回顾一下，一个标准的、带有几个查询条件的 MyBatis SQL 是什么样子的：

```xml
<select id="queryUsers" resultType="User">
    SELECT * FROM tb_user
    <where>
        <if test="name != null">
            AND name = #{name}
        </if>
        <if test="age != null">
            AND age = #{age}
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="createTime != null">
            AND create_time >= #{createTime}
        </if>
    </where>
</select>
```

这段代码的问题在哪里，已经不用多言！这种一坨一坨的开发体验看到就难受至极，更别提还要去维护它。

## 革命性的解法

dbVisitor 作为一个新一代的数据库访问工具，最核心的设计理念之一就是：**让 SQL 回归 SQL**。

利用 dbVisitor 独创的 **规则机制**，上面的 XML 代码可以精简为：

```sql
SELECT * FROM tb_user
   @{and, name = :name}
   @{and, age = :age}
   @{and, status = :status}
   @{and, create_time >= :createTime}
```

假设只传入参数 `name="Tom"`，生成的 SQL 如下：

```sql
SELECT * FROM tb_user WHERE name = ?
```

是不是瞬间清爽了？没有了尖括号的视觉干扰，没有了冗余的 XML 闭合标签，只有纯粹的 SQL 逻辑。

你可能会问：`@{and, ...}` 到底做了什么？它是简单的字符串拼接吗？

当然不是！dbVisitor 的规则引擎是非常智能的。以 `@{and, name = :name}` 为例，它内置了以下逻辑：

1.  **智能判空**：引擎会自动检查 `key = :key` 表达式中的参数。如果参数 `:key` 为 `null`，整个 `@{and}` 规则块会被自动忽略，不会生成任何 SQL（注意：空字符串 `""` 被视为有效值，不会忽略）。
2.  **WHERE 处理**：如果内容不为空，`@{and}` 会识别它是否是 WHERE 子句的开头。如果是开头（例如前面没有 `1=1`），它会自动抹去 `AND`，直接生成 `WHERE name = ?`（这一点类似 MyBatis 的 `<where>` 标签，但更加隐形）。

这一切都是自动发生的，你只需要声明规则，剩下的交给 dbVisitor。

## 条件判断本该如此性感

MyBatis 中 90% 的 `<if>` 标签都是为了做两件事：
1.  **参数不为空时**，追加查询条件。
2.  **开关开启时**，追加查询条件。

dbVisitor 将这两类高频场景直接内化为最基础的规则，无需任何复杂的标签嵌套。

**1. 智能补全**

自动检查条件状态选择是否需要自动补全 `WHERE` / `AND` / `OR` 关键字。

```sql
-- 唯一条件下，自动补全 WHERE
SELECT * FROM tb_user 
    @{and, name = :name}

-- 多组条件中，自动追加 AND
SELECT * FROM tb_user where type = 'employee'
    @{and, name = :name} -- 生成 and name = ?
```

**2. 智能判空：`@{and}` / `@{or}`**

这是你最常用的规则。它们会自动检查参数是否为 `null`。

```sql
SELECT * FROM tb_user
    @{and, name = :name}    -- 仅当 name 不为空时生成 and name = ?
    @{or,  age > :age}      -- 仅当 age 不为空时生成  or  age > ?
```

**3. 开关控制：`@{ifand}` / `@{ifor}`**

当你需要用布尔值来控制 SQL 时，使用 `@{ifand}` 或 `@{ifor}`。

```sql
-- 只有当 showAll 为 false 时，才拼接入 AND is_delete = 0
SELECT * FROM tb_user @{ifand, !showAll, is_delete = 0}
```

对比 MyBatis 的臃肿写法以及像老鼠屎一样无处不在的 `1=1`

```xml
SELECT * FROM tb_user WHERE 1=1
<if test="!showAll">
    AND is_delete = 0
</if>
```

仅仅是一个简单的条件，dbVisitor 让你少写了 3 行代码，减少无用的条件判断，这就是效率。

## 一行代码的 IN 查询

MyBatis 的 `<foreach>` 标签简直是反人类设计：collection, item, open, close... 配置项多达七八个。
而且为了防止集合为空导致 SQL 语法错误，通常还得在外面套一层 `<if>`，简直令人窒息：

```xml
SELECT * FROM tb_user WHERE 1=1
<!-- 先判断不为空，再循环 -->
<if test="idList != null and idList.size() > 0">
    AND id IN
    <foreach collection="idList" item="id" open="(" separator="," close=")">
      #{id}
    </foreach>
</if>
```

而在 dbVisitor 中，利用 `@{ifand}` 和 `@{in}` 两个规则的嵌套使用，你只需要一行：

```sql
-- ifand 接受 OGNL 表达式，!idList.isEmpty 确保集合有值时才生成 SQL
SELECT * FROM tb_user @{ifand, !idList.isEmpty, id IN @{in, :idList}}
```

对比一下，这代码量节省的可不是一点点，而是降维打击！

## 忘掉 &lt;set&gt;：忘掉逗号

在写 Update 语句时，处理末尾的逗号是最烦人的。dbVisitor 的 `@{set}` 规则完美解决，引擎会自动处理字段间的逗号：

```sql
UPDATE tb_user
SET
    @{set, name  = :name}
    @{set, age   = :age}
    @{set, email = :email}
WHERE id = #{id}
```

MyBatis 用了一个 `<set>` 标签来解决这个问题：

```xml
UPDATE tb_user
<set>
    <if test="name != null">name = #{name},</if>
    <if test="age != null">age = #{age},</if>
    <if test="email != null">email = #{email},</if>
</set>
WHERE id = #{id}
```

⚠️ 虽然 `@{set}` 规则很强大，但规则的判断是依赖已经生成的 SQL 进行推断。

```sql title='当规则没有匹配时，一些意外情况'
UPDATE tb_user SET
    @{set, name = :name},  -- ❌ 规则无法删除身后的逗号
    fixed_col = 123,      
    @{set, email = :email} -- ❌ 规则虽然不会添加新的逗号但也不会删除上一个条件中的逗号
WHERE id = :id

-- 正确的写法是
UPDATE tb_user SET
    fixed_col = 123        -- 先写固定列，再写动态列
    @{set, name = :name}   -- 规则会自动处理前置逗号
    @{set, email = :email}
WHERE id = :id
```

## 分支判断的救星

MyBatis 的 `<choose>-<when>-<otherwise>` 结构冗长得令人发指，写起来仿佛在填表。
最大的麻烦在于即便是使用 `@Select` 注解仍然逃脱不了 XML 的魔咒

```xml
SELECT * FROM t_blog WHERE 1 = 1
<choose>
    <when test="title != null">
        AND title = #{title}
    </when>
    <when test="content != null">
        AND content = #{content}
    </when>
    <otherwise>
        AND owner = "defaultOwner"
    </otherwise>
</choose>
```

```java
@Select({"<script>...无法逃避的 XML 魔咒...</script>"})
List<Blog> queryBlogs(@Param("title") String title,
                      @Param("content") String content);
```

dbVisitor 让你用 SQL 的思维写 SQL，更符合编程直觉。同样的场景，dbVisitor 只需要这样：

```sql title='CASE 规则 IF-ELSE 模式'
SELECT * FROM t_blog
@{and, @{case, , @{when, title != null,   title = #{title}},
                 @{when, content != null, content = #{content}},
                 @{else,                  owner = "defaultOwner"}
        }
}
```

换一个例子：根据 encryptMode 的值选择加密方式
```sql title='CASE 规则 Switch 模式'
SELECT * FROM tb_user
@{and, @{case, encryptMode, @{when, true, password = @{md5, :pwd}},
                            @{else,       password = :pwd}
        }
}
```

## 像乐高积木一样组合

规则引擎最强大的地方在于其 **可组合性**。所有规则都可以像乐高积木一样嵌套使用。这意味着你可以用 `@{case}` 的结果去驱动 `@{and}`，或者在 `@{else}` 里再写一组 `@{if}`。

比如一个常见的**权限控制**场景：
*   如果是管理员 (`ADMIN`)，查询所有数据；
*   如果是部门经理 (`MGR`)，查询本部门数据；
*   如果是普通员工，只能查自己的数据。

```sql
SELECT * FROM tb_report
@{and, @{case, role, @{when, 'ADMIN', /* 不加限制 */},
                     @{when, 'MGR',   dept_id = :deptId},
                     @{else,          user_id = :userId}
       }
}
```

注意看，外层的 `@{and}` 会自动处理连接词：
- 当 `role` 是 'ADMIN' 时，`@{case}` 输出空，整个 `@{and}` 消失。
  - 生成 `SELECT * FROM tb_report WHERE 1=1`。
- 当 `role` 是 'MGR' 时，内部输出 `dept_id = ?`，外层自动加上 `AND`。
  - 生成 `... WHERE 1=1 AND dept_id = ?`。

这种自然的嵌套组合，让你能用 SQL 结构直接表达复杂的业务逻辑，而不是在 Java 代码和 XML 之间反复横跳。

## 这就结束了？

规则的强大之处在于 **无处不在**。你不需要为了使用动态 SQL 而强迫自己切换开发模式。无论你身处哪个层级，dbVisitor 的动态规则都能无缝集成。

**1. 在 编程式 API (JdbcTemplate) 中使用**

不需要 StringBuilder，不需要拼接字符串。

```java
// 直接在 SQL 字符串中使用规则
jdbcTemplate.queryForList("SELECT * FROM users @{and, name = :name}", args);
```

**2. 在 声明式 API (注解) 中使用**

彻底摆脱 `@Script` 或者 `<script>` 标签的包裹，像写普通 SQL 一样写动态 SQL。

```java
// 在 Mapper 接口的注解中使用
@Insert("INSERT INTO users (account, password) VALUES (:account, @{md5, :password})")
int insertUser(User user);
```

**3. 在 传统 XML 文件 中使用**

如果你还是喜欢将 SQL 独立管理，dbVisitor 也支持在 XML 文件中使用这些规则，完全替代 `<if>` 等标签。

```xml
<select id="queryUser">
    SELECT * FROM users
    @{and, name = :name}
    @{and, age = :age}
</select>
```

这意味着可以将动态 SQL 的能力带入到任何你熟悉的地方，而不需要任何额外的适配成本。

## 结语

扔掉你的“裹脚布”,让我们来一场直观的对比：

| 特性 | MyBatis XML | dbVisitor 规则 | 评价 |
| :--- | :--- | :--- | :--- |
| **条件判断** | `<if test="...">...</if>` | `@{and, ...}` | dbVisitor 节省 70% 代码量 |
| **IN 查询** | `<foreach item="..." ...>` | `auto expand` | 直觉式编程 |
| **代码风格** | XML 与 SQL 混杂 | 脚本化 SQL | dbVisitor 阅读更流畅 |
| **维护成本** | 高，容易淹没在标签海中 | 低，逻辑一目了然 | 降本增效 |

技术在进步，工具在迭代。MyBatis 在过去的十几年里确实为 Java 生态做出了巨大贡献，但在动态 SQL 的处理上，它的 XML 方案确实显得过于陈旧和繁琐了。

dbVisitor 通过引入脚本化的规则引擎，在保留 SQL 灵活性的同时，彻底解决了动态 SQL 拼接的痛点。它向我们证明了：**强大的功能不需要复杂的配置**。

如果你厌倦了维护几千行的 XML Mapper，不妨试试 dbVisitor，体验一下"把裹脚布烧了"之后的奔跑快感。
