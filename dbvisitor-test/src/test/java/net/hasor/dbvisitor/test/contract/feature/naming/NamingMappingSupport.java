/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.test.contract.feature.naming;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.test.contract.material.model.naming.AllNamingOptionsUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CamelCaseEnabledUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.CaseTestUpperCI;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordColumnNoDelimitedEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.KeywordTableNoDelimitedEntity;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnStrictUser;
import net.hasor.dbvisitor.test.contract.material.model.naming.UpperCaseColumnUser;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

public abstract class NamingMappingSupport extends AbstractNxnContractTest {
    protected int baseId() {
        return 930000;
    }

    protected SqlDialect detectDialect() throws SQLException {
        return jdbcTemplate.execute((ConnectionCallback<SqlDialect>) conn -> SqlDialectRegister.findDialect(null, conn));
    }

    protected LambdaTemplate optionsLambda(Options options) throws SQLException {
        Connection connection = jdbcTemplate.getConnection();
        if (connection != null) {
            return new LambdaTemplate(connection, options);
        }
        return new LambdaTemplate(jdbcTemplate.getDataSource(), options);
    }

    protected UpperCaseColumnUser newUpperCaseUser(int id, String name) {
        UpperCaseColumnUser user = new UpperCaseColumnUser();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    protected CamelCaseEnabledUser camelCaseUser(int id, String name, int age) {
        CamelCaseEnabledUser user = new CamelCaseEnabledUser();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    protected void ensurePlainUserTable() throws SQLException {
        dropTableIfExists("plain_user");
        jdbcTemplate.executeUpdate("CREATE TABLE plain_user (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, email VARCHAR(100), create_time " + profile().datetimeColumnType() + ")");
    }

    protected UpperCaseColumnStrictUser newUpperCaseStrictUser(int id, String name) {
        UpperCaseColumnStrictUser user = new UpperCaseColumnStrictUser();
        user.setId(id);
        user.setName(name);
        user.setAge(30);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    protected AllNamingOptionsUser allNamingUser(int id, String name) {
        AllNamingOptionsUser user = new AllNamingOptionsUser();
        user.setId(id);
        user.setName(name);
        user.setAge(36);
        user.setEmail(name.toLowerCase() + "@nxn.test");
        user.setCreateTime(new Date());
        return user;
    }

    protected void ensureCaseSensitivityTables() throws SQLException {
        dropTableIfExists("case_test_lower");
        dropTableIfExists(qualified("Case_Test_Upper"));
        jdbcTemplate.executeUpdate("CREATE TABLE case_test_lower (" + primaryKeyColumn("id", "INT") + ", name VARCHAR(100), age INT, memo VARCHAR(200))");
        jdbcTemplate.executeUpdate("CREATE TABLE " + qualified("Case_Test_Upper") + " (" //
                + primaryKeyColumn(qualified("Id"), "INT") + ", " //
                + qualified("Name") + " VARCHAR(100), " //
                + qualified("Age") + " INT, " //
                + qualified("Memo") + " VARCHAR(200))");
    }

    protected CaseTestUpperCI caseTestUpper(int id, String name, String memo) {
        CaseTestUpperCI entity = new CaseTestUpperCI();
        entity.setId(id);
        entity.setName(name);
        entity.setAge(25);
        entity.setMemo(memo);
        return entity;
    }

    protected String qualified(String identifier) {
        return profile().leftQualifier() + identifier + profile().rightQualifier();
    }

    protected void ensureKeywordColumnTable() throws SQLException {
        String left = profile().leftQualifier();
        String right = profile().rightQualifier();
        dropTableIfExists(left + "naming_keyword_test" + right);
        jdbcTemplate.executeUpdate("CREATE TABLE " + left + "naming_keyword_test" + right + " (" //
                + primaryKeyColumn(left + "id" + right, "INT") + ", " //
                + left + "order" + right + " VARCHAR(100), " //
                + left + "select" + right + " VARCHAR(100), " //
                + left + "name" + right + " VARCHAR(100))");
    }

    protected void ensureKeywordTable() throws SQLException {
        String left = profile().leftQualifier();
        String right = profile().rightQualifier();
        dropTableIfExists(left + "order" + right);
        jdbcTemplate.executeUpdate("CREATE TABLE " + left + "order" + right + " (" //
                + primaryKeyColumn(left + "id" + right, "INT") + ", " //
                + left + "name" + right + " VARCHAR(100), " //
                + left + "description" + right + " VARCHAR(200))");
    }

    protected KeywordColumnEntity keywordColumn(int id, String orderValue, String selectValue, String name) {
        KeywordColumnEntity entity = new KeywordColumnEntity();
        entity.setId(id);
        entity.setOrderValue(orderValue);
        entity.setSelectValue(selectValue);
        entity.setName(name);
        return entity;
    }

    protected KeywordColumnNoDelimitedEntity keywordColumnNoDelimited(int id, String orderValue, String selectValue, String name) {
        KeywordColumnNoDelimitedEntity entity = new KeywordColumnNoDelimitedEntity();
        entity.setId(id);
        entity.setOrderValue(orderValue);
        entity.setSelectValue(selectValue);
        entity.setName(name);
        return entity;
    }

    protected KeywordTableEntity keywordTable(int id, String name, String description) {
        KeywordTableEntity entity = new KeywordTableEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        return entity;
    }

    protected KeywordTableNoDelimitedEntity keywordTableNoDelimited(int id, String name, String description) {
        KeywordTableNoDelimitedEntity entity = new KeywordTableNoDelimitedEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setDescription(description);
        return entity;
    }
}
