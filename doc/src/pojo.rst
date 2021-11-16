对象映射
------------------------------------
有如下表

.. code-block:: sql
    :linenos:

    create table `test_user` (
        `id`          int(11),
        `name`        varchar(255),
        `age`         int,
        `create_time` datetime,
        primary key (`id`)
    )


类名即表名
==============

默认情况下 HasorDB 采用名称映射策略，即：``test_user`` 类映射 ``test_user`` 表，``create_time`` 属性映射 ``create_time`` 列。由此 pojo 如下：

.. code-block:: java
    :linenos:

    public class test_user {
        private BigInteger id;
        private String     name;
        private Integer    age;
        private Date       create_time;

        // getters and setters omitted
    )


驼峰转下划线
==============

上述 pojo 的命名不太符合常规 Java 驼峰的命名规范，可以通过 ``mapUnderscoreToCamelCase`` 配置将驼峰命名转换为下划线命名。

.. code-block:: java
    :linenos:

    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        private Integer id;
        private String  name;
        private Integer age;
        private Date    createTime;

        // getters and setters omitted
    }


- 类名 `TestUser` 会被转换为 `test_user`
- 列 `createTime` 会被转换为 `create_time`


明确映射关系
==============

在明确映射关系中可以精确的指定表名和列名。将 ``autoMapping`` 设置为关闭然后为每表和每一个列配置映射的名字。

.. code-block:: java
    :linenos:

    // name        = "test_user" 表名
    // autoMapping = false       关闭属性字段自动映射(默认true)，全部通过 @Column 配置
    @Table(name = "test_user", autoMapping = false)
    public class TestUser {
        @Column(name = "id"， primary = true)
        private Integer id;
        @Column("name")
        private String  name;
        @Column("age")
        private Integer age;
        @Column("create_time")
        private Date    createTime;

        // getters and setters omitted
    }


忽略某些列
==============

在默认情况下(``autoMapping = true``)，可以通过 ``@Ignore`` 注解忽略某个属性到列的映射。

.. code-block:: java
    :linenos:

    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        private Integer id;
        private String  name;
        private Integer age;
        private Date    createTime;
        @Ignore
        private Date    modifyTime; // 忽略到列的映射

        // getters and setters omitted
    }


名称大小写敏感
==============

处理大小写敏感需要设置 @Table 注解的 ``useDelimited = true``，让其在生成 SQL 的时候对每一个名称都使用限定符包裹起来；
然后设置 ``autoMapping = false`` 关闭属性的自动发现，改为通过 @Column 明确配置。
最后设置 ``caseInsensitive = false`` 将结果集列名大小写不敏感设置为敏感，默认是：true不敏感

.. HINT::
    和大小写相关的属性有两个分别是 ``useDelimited``、``caseInsensitive``

    - 属性 ``useDelimited``，决定在生成 SQL 语句时是否用限定符。
      例如表名：``test_user`` 和 ```test_user``` 后者使用了限定符。
    - 属性 ``caseInsensitive``，决定在接收和处理查询结果集时候，是否对结果集上的列名保持大小写敏感性。

.. code-block:: java
    :linenos:

    @Table(name = "test_user", useDelimited = true, autoMapping = false, caseInsensitive = false)
    public class TestUser {
        @Column(name = "id"， primary = true)
        private Integer id;
        @Column("age")
        private Integer age1;
        @Column("AGE")
        private String  age2;

        // getters and setters omitted
    }


名称含有关键字
==============

比如有如下这样一张表，包含了一个叫 index 的列。

.. code-block:: sql
    :linenos:

    create table `param_index` (
        `id`    int(11),
        `name`  varchar(255),
        `index` int,
        primary key (`id`)
    )

此时只需要设置 ``@Table`` 注解的 ``useDelimited = true`` 属性，让其在生成 SQL 的时候对每一个名称都使用限定符号包裹起来。即可正常处理，

.. HINT::
    HasorDB 已经可以自动识别并处理 达梦、MySql、Oracle、PostgreSql 四个数据库的关键字，因此无需 ``useDelimited`` 也可以处理名称关键字问题。
    具体支持的关键字需要到 jar 包中 ``META-INF/db-keywords/*.keywords`` 相关文件中查看，HasorDB 当匹配到关键字会自动为它加上 useDelimited。


跨Schema映射
==============

通常一个应用的表都会属于同一个 Schema，因此查询数据库表只需要指定表名、无需特别指出其所在 Schema 即可查询表数据。

某些场景下一个应用要访问多个不同的 Schema，这时候可以使用下面方式将表映射到不同的 Schema 上。

.. code-block:: java
    :linenos:

    @Table(schema = "mydb", name = "test_user")
    public class TestUserForMy {
        // fields and getters and setters omitted
    }

    @Table(schema = "youdb", name = "test_user")
    public class TestUserForYou {
        // fields and getters and setters omitted
    }


列不允许更新
==============

如若 ``test_user`` 表的 ``create_time`` 列不允许被更新，那么需要配置 @Column 注解 ``update = false`` 表示该列不参与更新。

.. code-block:: java
    :linenos:

    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        private Integer id;
        private String  name;
        private Integer age;
        @Column(update = false) // 列不参与更新
        private Date    createTime;

        // getters and setters omitted
    }

.. HINT::
    ``@Column`` 具有另外一个属性 ``insert`` 可以控制当新增数据时是否参与新增，默认为 true 表示参与。


列的 JdbcType
==============

每一个数据库类型都会有一个与其对应的 JdbcType、每个 Java 类型要想正确写入也要匹配正确的 JdbcType。
通过 ``@Column`` 注解的 ``jdbcType`` 属性可以设置这种映射关系。通常 HasorDB 都会自动处理好它们，使用过程中无需干预。

.. HINT::
    若想了解 HasorDB 对于 JavaType 和 JdbcType 映射关系，请看类型相关章节。


.. code-block:: java
    :linenos:

    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        @Column(jdbcType = java.sql.Types.TINYINT) // 将 Integer 应为为 TINYINT
        private Integer id;

        // getters and setters omitted
    }


枚举类型映射
==============

默认情况
    枚举类型的映射无需指定特殊的 ``TypeHandler`` 在 HasorDB 中它会自动被处理好。
    默认下枚举的映射要求数据库中字段类型必须为 ``字符串``，而字段值内容是映射到枚举的枚举的 ``name()`` 属性上的。

    .. code-block:: java
        :linenos:

        @Table(mapUnderscoreToCamelCase = true)
        public class TestUser {
            private UserType userType;

            // getters and setters omitted
        }

数值映射为枚举类型
    将数值类型映射成为枚举值，需要枚举类型实现 ``net.hasor.db.types.EnumOfValue`` 接口。

    .. code-block:: java
        :linenos:

        public enum LicenseEnum implements EnumOfValue<LicenseEnum> {
            Private(0),
            AGPLv3(1),
            GPLv3(2),;

            private final int type;

            LicenseEnum(int type) {
                this.type = type;
            }

            public int codeValue() {
                return this.type;
            }

            public LicenseEnum valueOfCode(int codeValue) {
                for (LicenseEnum item : LicenseEnum.values()) {
                    if (item.getType() == codeValue) {
                        return item;
                    }
                }
                return null;
            }
        }

Code映射为枚举类型
    将 Code 类型映射成为枚举值，需要枚举类型实现 ``net.hasor.db.types.EnumOfCode`` 接口。
    Code 值映射与默认情况不同，它允许通过 Code 码来映射具体枚举项，并非通过枚举类型的 name 属性。

    .. code-block:: java
        :linenos:

        public enum LicenseEnum implements EnumOfValue<LicenseEnum> {
            Private("Private"),
            AGPLv3("AGPLv3"),
            GPLv3("GPLv3"),

            private final String type;

            LicenseEnum(String type) {
                this.type = type;
            }

            public String codeName() {
                return this.type;
            }

            public LicenseEnum valueOfCode(String codeValue) {
                for (LicenseEnum item : LicenseEnum.values()) {
                    if (item.codeName().equalsIgnoreCase(codeValue)) {
                        return item;
                    }
                }
                return null;
            }
        }


列读写自定义
==============

某些数据类型的写入需要特殊处理，例如将字符串数据按照某种格式转换成为时间日期类型。这就需要用到 ``TypeHandler``。

例如，数据库中保存的是时间类型，需要将其读取成格式为 ``yyyy-MM-dd`` 的字符串。

.. code-block:: java
    :linenos:

    @Table(mapUnderscoreToCamelCase = true)
    public class TestUser {
        @Column(typeHandler = MyDateTypeHandler.class)
        private String myTime;

        // getters and setters omitted
    }


.. code-block:: java
    :linenos:

    public class MyDateTypeHandler extends AbstractTypeHandler<String> {
        public void setNonNullParameter(PreparedStatement ps, int i, String parameter, Integer jdbcType) throws SQLException {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parameter);
            ps.setTimestamp(i, new Timestamp(date.getTime()));
        }

        public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return fmtDate(rs.getTimestamp(columnIndex));
        }

        public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return fmtDate(rs.getTimestamp(columnIndex));
        }

        public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return fmtDate(cs.getTimestamp(columnIndex));
        }

        private String fmtDate(Timestamp sqlTimestamp){
            if (sqlTimestamp != null) {
                Date date = new Date(sqlTimestamp.getTime());
                return new SimpleDateFormat("yyyy-MM-dd").format(date);
            }
            return null;
        }
    }


.. HINT::
    HasorDB 内置了 60 多个 TypeHandler，基本涵盖了各种情况以及数据类型。详细的信息请看类型相关章节。
