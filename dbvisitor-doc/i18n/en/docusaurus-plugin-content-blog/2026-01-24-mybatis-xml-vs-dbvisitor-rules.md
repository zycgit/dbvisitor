---
slug: mybatis-xml-vs-dbvisitor-rules
title: Burn the Mummy Wrappings of MyBatis
authors: [ZhaoYongChun]
tags: [dbVisitor, MyBatis, ORM]
---
 
Hundreds of lines of `<if>`, `<where>`, `<choose>` tags in MyBatis XML — clean SQL buried under **mummy wrappings**. If you're fed up with three lines of XML for a simple null check, dbVisitor's dynamic SQL rules might be the scissors you need.

<!--truncate-->

## MyBatis XML Hell

Let's review what a standard MyBatis SQL with a few query conditions looks like:

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

The problem with this code is self-evident! This kind of bloated development experience is painful just to look at, let alone maintain.

## Revolutionary Solution

As a next-generation database access tool, one of dbVisitor's core design philosophies is: **Let SQL return to SQL**.

Using dbVisitor's original **rule mechanism**, the XML code above can be simplified to:

```sql
SELECT * FROM tb_user
   @{and, name = :name}
   @{and, age = :age}
   @{and, status = :status}
   @{and, create_time >= :createTime}
```

Assuming only the parameter `name="Tom"` is passed, the generated SQL is as follows:

```sql
SELECT * FROM tb_user WHERE name = ?
```

Isn't it instantly refreshing? No visual interference from angle brackets, no redundant XML closing tags, just pure SQL logic.

You might ask: What exactly does `@{and, ...}` do? Is it simple string concatenation?

Of course not! The dbVisitor rule engine is very intelligent. Taking `@{and, name = :name}` as an example, it has the following logic built-in:

1.  **Smart Null Check**: The engine automatically checks the parameter in the `key = :key` expression. If the parameter `:key` is `null`, the entire `@{and}` rule block is automatically ignored and no SQL is generated (Note: empty string `""` is treated as a valid value and will not be ignored).
2.  **WHERE Handling**: If the content is not empty, `@{and}` identifies if it is the beginning of a WHERE clause. If it is the beginning (e.g. no `1=1` before it), it automatically wipes out `AND` and generates `WHERE name = ?` directly (this is similar to MyBatis's `<where>` tag, but more invisible).

All this happens automatically; you just need to declare the rules and leave the rest to dbVisitor.

## Conditional Logic Should Be This Sexy

90% of `<if>` tags in MyBatis are for two things:
1.  **When parameter is not null**, append query condition.
2.  **When switch is on**, append query condition.

dbVisitor internalizes these two high-frequency scenarios directly into the most basic rules, without any complex tag nesting.

**1. Smart Completion**

Automatically checks condition status to choose whether to strictly complete `WHERE` / `AND` / `OR` keywords.

```sql
-- Single condition, auto complete WHERE
SELECT * FROM tb_user 
    @{and, name = :name}

-- Multiple conditions, auto append AND
SELECT * FROM tb_user where type = 'employee'
    @{and, name = :name} -- Generates and name = ?
```

**2. Smart Null Check: `@{and}` / `@{or}`**

These are your most used rules. They automatically check if parameters are `null`.

```sql
SELECT * FROM tb_user
    @{and, name = :name}    -- Generates and name = ? only when name is not null
    @{or,  age > :age}      -- Generates or age > ? only when age is not null
```

**3. Switch Control: `@{ifand}` / `@{ifor}`**

When you need to use a boolean value to control SQL, use `@{ifand}` or `@{ifor}`.

```sql
-- Append AND is_delete = 0 only when showAll is false
SELECT * FROM tb_user @{ifand, !showAll, is_delete = 0}
```

Compare with MyBatis's bloated syntax and the pervasive `1=1` which is like a rat dropping:

```xml
SELECT * FROM tb_user WHERE 1=1
<if test="!showAll">
    AND is_delete = 0
</if>
```

Just for a simple condition, dbVisitor lets you write 3 lines less code and reduces useless conditional checks. This is efficiency.

## One-Line IN Query

MyBatis's `<foreach>` tag is simply anti-human design: collection, item, open, close... configuration items are as many as seven or eight.
And to prevent SQL syntax errors caused by empty collections, you usually have to wrap it in an `<if>` layer, which is simply suffocating:

```xml
SELECT * FROM tb_user WHERE 1=1
<!-- Check not null first, then loop -->
<if test="idList != null and idList.size() > 0">
    AND id IN
    <foreach collection="idList" item="id" open="(" separator="," close=")">
      #{id}
    </foreach>
</if>
```

In dbVisitor, utilizing the nested use of `@{ifand}` and `@{in}` rules, you only need one line:

```sql
-- ifand accepts OGNL expression, !idList.isEmpty ensures SQL is generated only when list has values
SELECT * FROM tb_user @{ifand, !idList.isEmpty, id IN @{in, :idList}}
```

Compare this, the code saving is not just a little bit, it is a dimensionality reduction strike!

## Forget &lt;set&gt;: Forget Commas

When writing Update statements, handling the trailing comma is the most annoying thing. dbVisitor's `@{set}` rule solves this perfectly, the engine automatically handles commas between fields:

```sql
UPDATE tb_user
SET
    @{set, name  = :name}
    @{set, age   = :age}
    @{set, email = :email}
WHERE id = #{id}
```

MyBatis uses a `<set>` tag to solve this problem:

```xml
UPDATE tb_user
<set>
    <if test="name != null">name = #{name},</if>
    <if test="age != null">age = #{age},</if>
    <if test="email != null">email = #{email},</if>
</set>
WHERE id = #{id}
```

⚠️ Although the `@{set}` rule is powerful, the rule's judgment relies on inferring from the already generated SQL.

```sql title='Unexpected situations when rules do not match'
UPDATE tb_user SET
    @{set, name = :name},  -- ❌ Rule cannot remove the trailing comma
    fixed_col = 123,      
    @{set, email = :email} -- ❌ Rule won't add new comma but won't remove comma from previous condition
WHERE id = :id

-- The correct way is
UPDATE tb_user SET
    fixed_col = 123        -- Write fixed columns first, then dynamic columns
    @{set, name = :name}   -- Rule automatically handles preceding comma
    @{set, email = :email}
WHERE id = :id
```

## Savior of Branch Logic

MyBatis's `<choose>-<when>-<otherwise>` structure is excruciatingly verbose, writing it feels like filling out a form.
The biggest trouble is that even with `@Select` annotation, you cannot escape the XML curse.

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
@Select({"<script>...Inescapable XML curse...</script>"})
List<Blog> queryBlogs(@Param("title") String title,
                      @Param("content") String content);
```

dbVisitor lets you write SQL with SQL thinking, which is more intuitive for programming. In the same scenario, dbVisitor only needs this:

```sql title='CASE Rule IF-ELSE Mode'
SELECT * FROM t_blog
@{and, @{case, , @{when, title != null,   title = #{title}},
                 @{when, content != null, content = #{content}},
                 @{else,                  owner = "defaultOwner"}
        }
}
```

Another example: select encryption method based on encryptMode value
```sql title='CASE Rule Switch Mode'
SELECT * FROM tb_user
@{and, @{case, encryptMode, @{when, true, password = @{md5, :pwd}},
                            @{else,       password = :pwd}
        }
}
```

## Combine Like Lego Blocks

The most powerful thing about the rule engine is its **composability**. All rules can be nested like Lego blocks. This means you can use the result of `@{case}` to drive `@{and}`, or write another set of `@{if}` inside `@{else}`.

For example, a common **permission control** scenario:
*   If administrator (`ADMIN`), query all data;
*   If department manager (`MGR`), query department data;
*   If normal employee, only query own data.

```sql
SELECT * FROM tb_report
@{and, @{case, role, @{when, 'ADMIN', /* No restriction */},
                     @{when, 'MGR',   dept_id = :deptId},
                     @{else,          user_id = :userId}
       }
}
```

Notice that the outer `@{and}` automatically handles the connector:
- When `role` is 'ADMIN', `@{case}` outputs nothing, the entire `@{and}` disappears.
  - Generates `SELECT * FROM tb_report WHERE 1=1`.
- When `role` is 'MGR', internal output is `dept_id = ?`, outer layer automatically adds `AND`.
  - Generates `... WHERE 1=1 AND dept_id = ?`.

This natural nested combination allows you to express complex business logic directly with SQL structure, instead of jumping back and forth between Java code and XML.

## Is That All?

The power of rules lies in being **ubiquitous**. You don't need to force yourself to switch development modes to use dynamic SQL. No matter what layer you are in, dbVisitor's dynamic rules can be integrated seamlessly.

**1. Use in Programmatic API (JdbcTemplate)**

No StringBuilder, no string concatenation.

```java
// Use rules directly in SQL string
jdbcTemplate.queryForList("SELECT * FROM users @{and, name = :name}", args);
```

**2. Use in Declarative API (Annotations)**

Completely get rid of `@Script` or `<script>` wrapper, write dynamic SQL just like writing normal SQL.

```java
// Use in Mapper interface annotations
@Insert("INSERT INTO users (account, password) VALUES (:account, @{md5, :password})")
int insertUser(User user);
```

**3. Use in Traditional XML Files**

If you still prefer to manage SQL independently, dbVisitor also supports using these rules in XML files, completely replacing tags like `<if>`.

```xml
<select id="queryUser">
    SELECT * FROM users
    @{and, name = :name}
    @{and, age = :age}
</select>
```

This means you can bring dynamic SQL capabilities to any place you are familiar with, without any additional adaptation costs.

## Comparison Summary: Throw Away Your "Mummy Shroud"

Let's have a visual comparison:

| Feature | MyBatis XML | dbVisitor Rules | Review |
| :--- | :--- | :--- | :--- |
| **Conditional Logic** | `<if test="...">...</if>` | `@{and, ...}` | dbVisitor saves 70% code |
| **IN Query** | `<foreach item="..." ...>` | `auto expand` | Intuitive programming |
| **Code Style** | Mixed XML and SQL | Scripted SQL | dbVisitor reads smoother |
| **Maintenance Cost** | High, drowned in tag sea | Low, logic clear at a glance | Cost reduction & efficiency |

## Conclusion

Technology is improving, tools are iterating. MyBatis has indeed made huge contributions to the Java ecosystem in the past decade, but its XML solution for dynamic SQL processing indeed appears too archaic and cumbersome.

By introducing a scripted rule engine, dbVisitor completely solves the pain points of dynamic SQL splicing while retaining SQL flexibility. It proves to us: **Powerful features do not need complex configuration**.

If you are tired of maintaining thousands of lines of XML Mappers, try dbVisitor and experience the thrill of running after "burning the mummy's shroud".
