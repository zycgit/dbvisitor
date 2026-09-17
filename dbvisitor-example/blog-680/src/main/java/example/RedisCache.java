/*
 * Copyright 2015-2022 the original author or authors.
 * Licensed under the Apache License, Version 2.0.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package example;

import java.sql.Connection;
import java.util.UUID;
import net.hasor.dbvisitor.mapper.Delete;
import net.hasor.dbvisitor.mapper.Insert;
import net.hasor.dbvisitor.mapper.Param;
import net.hasor.dbvisitor.mapper.Query;
import net.hasor.dbvisitor.mapper.SimpleMapper;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.types.BindTypeHandler;
import net.hasor.dbvisitor.types.handler.json.JsonTypeHandler;

public class RedisCache {
    @BindTypeHandler(JsonTypeHandler.class)
    public static class Product {
        private String productId;
        private String name;
        private Long   priceCents;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getPriceCents() {
            return priceCents;
        }

        public void setPriceCents(Long priceCents) {
            this.priceCents = priceCents;
        }
    }

    @SimpleMapper
    public interface ProductCacheMapper {
        @Insert("SET #{key} #{product} EX #{seconds}")
        int save(@Param("key") String key, @Param("product") Product product, @Param("seconds") int seconds);

        @Query("GET #{key}")
        Product load(@Param("key") String key);

        @Query("TTL #{key}")
        long ttl(@Param("key") String key);

        @Delete("DEL #{key}")
        int evict(@Param("key") String key);
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = Connections.redis(); Session session = new Configuration().newSession(conn)) {
            ProductCacheMapper mapper = session.createMapper(ProductCacheMapper.class);
            String key = "blog:product:" + UUID.randomUUID() + ":p1001";
            try {
                System.out.println("missing=" + mapper.load(key));
                Product product = new Product();
                product.setProductId("p1001");
                product.setName("USB-C Hub");
                product.setPriceCents(12900L);
                mapper.save(key, product, 300);
                Product cached = mapper.load(key);
                System.out.println("cached=" + cached.getName() + "; priceCents=" + cached.getPriceCents());
                System.out.println("ttl=" + mapper.ttl(key));
                mapper.evict(key);
                System.out.println("afterEvict=" + mapper.load(key));
            } finally {
                mapper.evict(key);
            }
        }
    }
}
