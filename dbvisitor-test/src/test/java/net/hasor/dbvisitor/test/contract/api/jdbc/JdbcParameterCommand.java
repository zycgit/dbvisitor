/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.api.jdbc;

/** Named fixture commands; the contract tests parameter handling, not SQL syntax. */
public enum JdbcParameterCommand {
    // @formatter:off
    SELECT_USER("SELECT id, name, age, email FROM user_info WHERE name = ? AND age > ?"),
    INSERT_POSITIONAL("INSERT INTO user_info (id, name, age, email, create_time) VALUES (?, ?, ?, ?, ?)"),
    SELECT_NULL_ROW("SELECT name, age FROM user_info WHERE id = ?"),
    SELECT_EMAIL_BY_ID("SELECT email FROM user_info WHERE id = ?"),
    SELECT_EMAIL_BY_NAME("SELECT email FROM user_info WHERE name = ?"),
    INSERT_COLON("INSERT INTO user_info (id, name, age, email, create_time) VALUES (:id, :name, :age, :email, :createTime)"),
    COUNT_BY_NAME_AGE("SELECT COUNT(*) FROM user_info WHERE name = :name AND age > :age"),
    INSERT_BRACE("INSERT INTO user_info (id, name, age, email, create_time) VALUES (#{id}, #{name}, #{age}, #{email}, #{createTime})"),
    COUNT_BY_ID_NAME("SELECT COUNT(*) FROM user_info WHERE id = #{id} AND name = #{name}"),
    COUNT_BY_NESTED("SELECT COUNT(*) FROM user_info WHERE id = :ids[0] AND name = :names[0] AND age = :user.info.age"),
    SELECT_TEXT_ORDER("SELECT ${column}, age FROM ${tableName} WHERE ${column} LIKE 'NXN-Param-Text-%' ORDER BY ${orderBy}"),
    COUNT_TEXT_COLUMN("SELECT COUNT(*) FROM user_info WHERE ${column} = #{name}"),
    COUNT_RULE_NAMES("SELECT COUNT(*) FROM user_info WHERE age > :minAge @{and, name IN @{in, :names}}"),
    UPDATE_RULE_FIELDS("UPDATE user_info SET @{set, age = :age} , @{set, email = :email} WHERE id = :id"),
    SELECT_AGE("SELECT age FROM user_info WHERE id = ?"),
    INSERT_ARRAY_SOURCE("INSERT INTO user_info (id, name, age, email, create_time) VALUES (:arg0, :arg1, :arg2, :arg3, :arg4)"),
    COUNT_SOURCE_NAMES("SELECT COUNT(*) FROM user_info WHERE name IN @{in, :names} AND age > :minAge"),
    SELECT_TEXT_VALUE("SELECT ${column}, age FROM ${tableName} WHERE ${column} = #{name}"),
    COUNT_TEXT_VALUE("SELECT COUNT(*) FROM ${tableName} WHERE ${column} = #{name}");
    // @formatter:on

    private final String sql;

    JdbcParameterCommand(String sql) {
        this.sql = sql;
    }

    public String sql() {
        return this.sql;
    }
}
