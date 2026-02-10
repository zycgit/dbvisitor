package net.hasor.dbvisitor.test.oneapi.model;

import java.util.List;
import net.hasor.dbvisitor.mapping.Column;
import net.hasor.dbvisitor.mapping.Table;

/**
 * Level 4: Vector Storage Model
 * For testing vector/embedding support
 */
@Table("product_vector")
public class ProductVectorForPg {
    @Column(primary = true)
    private Integer id;

    private String name;

    // Vector data - stored as vector(128) in PG (pgvector), FLOAT_VECTOR in Milvus
    // Custom PgVectorTypeHandler handles List<Float> <-> pgvector text format conversion
    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
}
