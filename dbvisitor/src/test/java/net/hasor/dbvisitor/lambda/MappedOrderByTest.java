/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import net.hasor.dbvisitor.lambda.core.OrderNullsStrategy;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class MappedOrderByTest {
    @Test
    public void repeatedMappedColumn_shouldPreserveEveryOrdering() throws SQLException {
        String sql = new LambdaTemplate().query(SortRow.class).asc("age").desc(SortRow::getAge).desc("name").getBoundSql().getSqlString();
        assertEquals("SELECT * FROM sort_row ORDER BY sort_age ASC, sort_age DESC, name DESC", sql);
    }

    @Test
    public void repeatedMappedColumn_shouldPreserveBothNullStrategies() throws SQLException {
        String first = new LambdaTemplate().query(SortRow.class).asc(OrderNullsStrategy.FIRST, SortRow::getAge).getBoundSql().getSqlString();
        String second = new LambdaTemplate().query(SortRow.class).desc(OrderNullsStrategy.LAST, SortRow::getAge).getBoundSql().getSqlString();
        var actual = new LambdaTemplate().query(SortRow.class).asc(OrderNullsStrategy.FIRST, SortRow::getAge)
                .desc(OrderNullsStrategy.LAST, SortRow::getAge).getBoundSql();
        assertEquals(first + ", " + second.substring(second.indexOf("ORDER BY ") + "ORDER BY ".length()), actual.getSqlString());
    }

    @Test
    public void reset_shouldAllowNewOrdering() throws SQLException {
        var query = new LambdaTemplate().query(SortRow.class).asc("age");
        query.getBoundSql();
        assertEquals("SELECT * FROM sort_row ORDER BY sort_age DESC", query.reset().desc("age").getBoundSql().getSqlString());
    }

    @Test
    public void reusedQuery_shouldKeepOrderingWithoutAffectingNewMapQuery() throws SQLException {
        var query = new LambdaTemplate().query(SortRow.class).asc("age");
        query.getBoundSql();
        String repeated = "SELECT * FROM sort_row ORDER BY sort_age ASC, sort_age DESC";
        assertEquals(repeated, query.desc("age").getBoundSql().getSqlString());
        assertEquals("SELECT * FROM sort_row ORDER BY sort_age DESC", query.asMap().desc("age").getBoundSql().getSqlString());
        assertEquals(repeated, query.getBoundSql().getSqlString());
    }

    @Test
    public void nestedConditions_shouldNotAffectOuterOrdering() throws SQLException {
        var query = new LambdaTemplate().query(SortRow.class).nested(part -> part.eq("age", 20)).asc("age").desc("age");
        var expected = new LambdaTemplate().query(SortRow.class).nested(part -> part.eq("age", 20)).asc("age");
        assertEquals(expected.getBoundSql().getSqlString() + ", sort_age DESC", query.getBoundSql().getSqlString());
    }

    @Test
    public void unmappedExpressions_shouldRemainRejected() throws SQLException {
        var query = new LambdaTemplate().query(SortRow.class);
        NoSuchElementException error = assertThrows(NoSuchElementException.class, () -> query.asc("random()"));
        assertEquals("tableMapping 'sort_row', property 'random()' is not exist.", error.getMessage());
    }

    @Test
    public void freedomExpressions_shouldRemainUntouched() throws SQLException {
        String sql = new LambdaTemplate().queryFreedom("sort_row").asc("random()").desc("random()").getBoundSql().getSqlString();
        assertEquals("SELECT * FROM sort_row ORDER BY random() ASC, random() DESC", sql);
    }

    @Test
    public void orderTemplates_shouldRemainUntouched() throws SQLException {
        String sql = new LambdaTemplate().query(SortRow.class).asc("randomOrder").desc("randomOrder").getBoundSql().getSqlString();
        assertEquals("SELECT * FROM sort_row ORDER BY random() ASC, random() DESC", sql);
    }

    @Test
    public void selectTemplates_shouldRemainUntouched() throws SQLException {
        String sql = new LambdaTemplate().query(SelectTemplateRow.class).selectAll().asc("randomSelect").desc("randomSelect").getBoundSql().getSqlString();
        assertEquals("SELECT random() random_select FROM sort_row ORDER BY random_select ASC, random_select DESC", sql);
    }

    @Table("sort_row")
    public static class SortRow {
        @Column("sort_age")
        private Integer age;
        @Column("name")
        private String name;
        @Column(value = "random_order", orderByColTemplate = "random()")
        private Double randomOrder;
        public Integer getAge() {
            return this.age;
        }

        public String getName() {
            return this.name;
        }

        public Double getRandomOrder() {
            return this.randomOrder;
        }
    }

    @Table("sort_row")
    public static class SelectTemplateRow {
        @Column(value = "random_select", selectTemplate = "random()")
        private Double randomSelect;

        public Double getRandomSelect() {
            return this.randomSelect;
        }
    }
}
