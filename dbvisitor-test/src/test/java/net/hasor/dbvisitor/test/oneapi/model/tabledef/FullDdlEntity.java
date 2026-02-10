package net.hasor.dbvisitor.test.oneapi.model.tabledef;

import java.math.BigDecimal;
import net.hasor.dbvisitor.mapping.*;

@Table(value = "ddl_test_table", ddlAuto = DdlAuto.Create)
@TableDescribe(characterSet = "utf8mb4", collation = "utf8mb4_general_ci", comment = "Test table for DDL", other = "ENGINE=InnoDB")
@IndexDescribe(name = "idx_name", columns = { "name" }, comment = "Name index")
@IndexDescribe(name = "idx_name_age", columns = { "name", "age" }, unique = true, comment = "Composite unique index", other = "USING BTREE")
public class FullDdlEntity {
    @Column(primary = true)
    @ColumnDescribe(sqlType = "BIGINT", comment = "Primary key", nullable = false)
    private Long id;

    @Column("name")
    @ColumnDescribe(sqlType = "VARCHAR(100)", nullable = false, defaultValue = "'unknown'", comment = "User name")
    private String name;

    @Column("age")
    @ColumnDescribe(sqlType = "INT", nullable = true, defaultValue = "0", comment = "User age")
    private Integer age;

    @Column("email")
    @ColumnDescribe(sqlType = "VARCHAR", length = "255", characterSet = "utf8", collation = "utf8_bin", comment = "Email address", other = "UNIQUE")
    private String email;

    @Column("balance")
    @ColumnDescribe(sqlType = "DECIMAL", precision = "10", scale = "2", comment = "Account balance")
    private BigDecimal balance;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
