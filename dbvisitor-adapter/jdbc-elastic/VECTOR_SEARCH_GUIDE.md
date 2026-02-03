# Elasticsearch 向量查询支持

## 概述

dbVisitor 已支持 Elasticsearch 的向量相似度搜索功能，可以通过 Lambda API 或原生 DSL 进行向量查询。

## 功能特性

### 1. 向量排序（KNN Search）
支持基于向量相似度的 Top-K 查询：
- **近似 KNN**：使用 ES 原生 `knn` 查询（高性能）
- **精确评分**：使用 `script_score` + Painless 脚本计算相似度

### 2. 向量范围过滤（Radius Search）
支持过滤出相似度满足阈值的文档。

### 3. 混合查询
支持向量查询与标量过滤条件的组合。

## API 使用

### Lambda Template API

```java
// 创建模板
LambdaTemplate lambda = new LambdaTemplate(esConnection);

// 定义实体
@Table("product_index")
public class Product {
    @Column("id")
    private String id;
    
    @Column("name")
    private String name;
    
    @Column("embedding")
    private List<Float> embedding;
    
    // getters and setters
}

// 1. 向量 KNN 查询（查找最相似的 10 个商品）
List<Float> queryVector = Arrays.asList(0.1f, 0.2f, 0.3f);
List<Product> topK = lambda.lambdaQuery(Product.class)
    .orderByL2(Product::getEmbedding, queryVector)
    .limit(10)
    .queryForList();

// 2. 向量范围查询（查找相似度 > 阈值的商品）
List<Product> similar = lambda.lambdaQuery(Product.class)
    .vectorRange(Product::getEmbedding, queryVector, 0.8)
    .queryForList();

// 3. 混合查询（类别过滤 + 向量排序）
List<Product> filtered = lambda.lambdaQuery(Product.class)
    .eq(Product::getCategory, "electronics")
    .orderByL2(Product::getEmbedding, queryVector)
    .limit(5)
    .queryForList();
```

### 原生 DSL API

```java
try (Connection conn = DriverManager.getConnection(esUrl)) {
    try (Statement stmt = conn.createStatement()) {
        // 1. KNN 查询
        String knnQuery = "POST /product_index/_search {" +
            "  \"knn\": {" +
            "    \"field\": \"embedding\"," +
            "    \"query_vector\": [0.1, 0.2, 0.3]," +
            "    \"k\": 10," +
            "    \"num_candidates\": 100" +
            "  }" +
            "}";
        
        // 2. Script Score 查询（余弦相似度）
        String scriptQuery = "POST /product_index/_search {" +
            "  \"query\": {" +
            "    \"script_score\": {" +
            "      \"query\": { \"match_all\": {} }," +
            "      \"script\": {" +
            "        \"source\": \"cosineSimilarity(params.query_vector, 'embedding') + 1.0\"," +
            "        \"params\": { \"query_vector\": [0.1, 0.2, 0.3] }" +
            "      }" +
            "    }" +
            "  }" +
            "}";
        
        // 3. 混合查询（过滤 + 向量评分）
        String hybridQuery = "POST /product_index/_search {" +
            "  \"query\": {" +
            "    \"script_score\": {" +
            "      \"query\": {" +
            "        \"bool\": {" +
            "          \"must\": [{ \"term\": { \"category\": \"electronics\" } }]" +
            "        }" +
            "      }," +
            "      \"script\": {" +
            "        \"source\": \"cosineSimilarity(params.query_vector, 'embedding') + 1.0\"," +
            "        \"params\": { \"query_vector\": [0.1, 0.2, 0.3] }" +
            "      }" +
            "    }" +
            "  }" +
            "}";
        
        try (ResultSet rs = stmt.executeQuery(knnQuery)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                System.out.println("Product: " + name);
            }
        }
    }
}
```

### 4. 参数化查询与结果映射
支持通过 `PreparedStatement` 安全传递向量参数，以及从结果集中直接读取向量数据。

#### 参数化查询 (PreparedStatement)
```java
// 使用占位符 '?' 传递向量参数
String dsl = "POST /idx/_search { \"query\": { \"script_score\": { \"script\": { \"source\": \"cosineSimilarity(params.vec, 'embedding')\", \"params\": { \"vec\": ? } } } } }";

try (PreparedStatement ps = conn.prepareStatement(dsl)) {
    List<Double> vector = Arrays.asList(0.1, 0.2, 0.3); // 或 Object[]
    ps.setObject(1, vector); 
    
    try (ResultSet rs = ps.executeQuery()) {
        // ...
    }
}
```

#### 读取向量数据
从结果集中获取 `dense_vector` 字段的数据：
```java
// 确保查询中包含向量字段 (_source)
ResultSet rs = stmt.executeQuery("POST /idx/_search { \"_source\": [\"embedding\", \"name\"] }");
if (rs.next()) {
    // 根据驱动实现，可能返回 List<?> 或 数组
    Object vectorObj = rs.getObject("embedding"); 
    
    if (vectorObj instanceof List) {
        List<Double> vector = (List<Double>) vectorObj;
    }
}
```

## 索引 Mapping 配置

向量字段需要使用 `dense_vector` 类型：

```json
PUT /product_index
{
  "mappings": {
    "properties": {
      "name": { "type": "text" },
      "category": { "type": "keyword" },
      "embedding": {
        "type": "dense_vector",
        "dims": 128,
        "index": true,
        "similarity": "cosine"
      }
    }
  }
}
```

### 相似度算法

- **cosine**：余弦相似度（推荐用于归一化向量）
- **l2_norm**：欧氏距离
- **dot_product**：点积（适合某些特定场景）

## 底层实现

### Lambda API 生成的 DSL

当调用 `orderByL2()` 时，dbVisitor 会生成如下 DSL：

```json
{
  "query": { "match_all": {} },
  "sort": [
    {
      "_script": {
        "type": "number",
        "script": {
          "lang": "painless",
          "source": "l2norm(params.vector, 'embedding')",
          "params": { "vector": [0.1, 0.2, 0.3] }
        },
        "order": "asc"
      }
    }
  ]
}
```

当调用 `vectorRange()` 时，生成：

```json
{
  "query": {
    "bool": {
      "must": [
        {
          "script": {
            "script": {
              "source": "l2norm(params.vector, 'embedding') < 0.5",
              "params": { "vector": [0.1, 0.2, 0.3] }
            }
          }
        }
      ]
    }
  }
}
```

## 测试

完整的测试用例参见：[Elastic7VectorSearchTest.java](../dbvisitor-adapter/jdbc-elastic/src/test/java/net/hasor/dbvisitor/adapter/elastic/realdb/es7/Elastic7VectorSearchTest.java)

运行测试（需要本地 ES7 实例）：
```bash
mvn test -pl dbvisitor-adapter/jdbc-elastic -Dtest=Elastic7VectorSearchTest
```

## 版本要求

- **Elasticsearch**: 7.6+ (需要支持 `dense_vector` 类型和 Painless 脚本中的向量函数)
- **Java**: 8+
- **dbVisitor**: 6.6.1+

## 性能建议

1. **使用 KNN 查询**：对于大规模数据，使用原生 `knn` 查询性能最佳
2. **适当的 num_candidates**：一般设置为 `k * 10`，平衡召回率和性能
3. **索引优化**：为 `dense_vector` 字段启用 `index: true`
4. **向量维度**：根据实际需求选择，常见维度有 128、256、512、768、1536

## 参考

- [Elasticsearch Dense Vector 文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/dense-vector.html)
- [KNN Search 文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/knn-search.html)
- [solon-ai-repo-elasticsearch 实现参考](https://gitee.com/noear/solon-ai/tree/main/solon-ai-rag-repositorys/solon-ai-repo-elasticsearch)
