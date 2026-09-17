/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import net.hasor.dbvisitor.mapper.BaseMapper;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.RefMapper;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;

public class ApiStyles {
    @Table("blog_orders")
    public static class Order {
        @Column(primary = true)
        private Integer id;
        private String  status;
        @Column("amount_cents")
        private Long    amountCents;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getAmountCents() {
            return amountCents;
        }

        public void setAmountCents(Long amountCents) {
            this.amountCents = amountCents;
        }
    }

    @RefMapper("/mapper/orders.xml")
    public interface OrderMapper extends BaseMapper<Order> {
        default List<Order> findPaid(long minimumCents) throws SQLException {
            return query().eq(Order::getStatus, "PAID").ge(Order::getAmountCents, minimumCents).orderBy(Order::getId).queryForList();
        }

        @Query("SELECT * FROM blog_orders WHERE id = #{id}")
        Order detail(@Param("id") int id);

        List<Map<String, Object>> totals(@Param("status") String status);
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:blog_styles"); Statement ddl = conn.createStatement(); Session session = new Configuration().newSession(conn)) {
            ddl.executeUpdate("CREATE TABLE blog_orders(id INT PRIMARY KEY, status VARCHAR(20), amount_cents BIGINT)");
            ddl.executeUpdate("INSERT INTO blog_orders VALUES (1,'PAID',1000),(2,'PAID',2000),(3,'NEW',5000)");
            OrderMapper mapper = session.createMapper(OrderMapper.class);
            System.out.println("builder=" + mapper.findPaid(1500).get(0).getId());
            System.out.println("annotation=" + mapper.detail(1).getAmountCents());
            Map<String, Object> totals = mapper.totals("PAID").get(0);
            System.out.println("xml=" + totals.get("status") + "; total=" + totals.get("total_cents"));
        }
    }
}
