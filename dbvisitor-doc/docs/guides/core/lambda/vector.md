---
id: vector
sidebar_position: 8
hide_table_of_contents: true
title: 向量查询
description: 使用 LambdaTemplate 进行向量近邻查询（KNN）和范围过滤（ANN），支持 L2、Cosine、IP 等多种度量方式。
---

在 AI 和机器学习场景中，文本、图片、音频等非结构化数据通常会被模型编码为**高维向量**（也称 Embedding）。  
向量之间的**距离**可以衡量原始数据的**语义相似度** —— 距离越近，语义越相似。

<svg viewBox="0 0 720 200" xmlns="http://www.w3.org/2000/svg" style={{maxWidth:'70%',height:'auto'}}>
  <defs>
    <marker id="ah" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto"><path d="M0,0 L8,3 L0,6Z" fill="#555"/></marker>
  </defs>
  <rect x="20" y="30" width="160" height="140" rx="12" fill="#f0f4ff" stroke="#4a7cff" strokeWidth="1.5"/>
  <text x="100" y="22" textAnchor="middle" fontSize="13" fontWeight="bold" fill="#333">原始数据</text>
  <text x="100" y="70" textAnchor="middle" fontSize="12" fill="#555">🐱 猫的照片</text>
  <text x="100" y="100" textAnchor="middle" fontSize="12" fill="#555">🐶 狗的照片</text>
  <text x="100" y="130" textAnchor="middle" fontSize="12" fill="#555">🚗 汽车照片</text>
  <text x="100" y="160" textAnchor="middle" fontSize="12" fill="#555">📝 一段文字</text>
  <line x1="190" y1="100" x2="270" y2="100" stroke="#555" strokeWidth="1.5" markerEnd="url(#ah)"/>
  <text x="230" y="90" textAnchor="middle" fontSize="11" fill="#777">Embedding</text>
  <text x="230" y="115" textAnchor="middle" fontSize="11" fill="#777">模型编码</text>
  <rect x="280" y="30" width="160" height="140" rx="12" fill="#f0fff4" stroke="#2da44e" strokeWidth="1.5"/>
  <text x="360" y="22" textAnchor="middle" fontSize="13" fontWeight="bold" fill="#333">向量空间</text>
  <text x="360" y="65" textAnchor="middle" fontSize="11" fill="#555">[0.12, 0.85, ..., 0.33]</text>
  <text x="360" y="90" textAnchor="middle" fontSize="11" fill="#555">[0.15, 0.80, ..., 0.31]</text>
  <text x="360" y="115" textAnchor="middle" fontSize="11" fill="#555">[0.91, 0.02, ..., 0.77]</text>
  <text x="360" y="140" textAnchor="middle" fontSize="11" fill="#555">[0.45, 0.62, ..., 0.19]</text>
  <line x1="450" y1="100" x2="530" y2="100" stroke="#555" strokeWidth="1.5" markerEnd="url(#ah)"/>
  <text x="490" y="90" textAnchor="middle" fontSize="11" fill="#777">距离计算</text>
  <text x="490" y="115" textAnchor="middle" fontSize="11" fill="#777">相似度排序</text>
  <rect x="540" y="30" width="160" height="140" rx="12" fill="#fff8f0" stroke="#e5873a" strokeWidth="1.5"/>
  <text x="620" y="22" textAnchor="middle" fontSize="13" fontWeight="bold" fill="#333">查询结果</text>
  <text x="620" y="65" textAnchor="middle" fontSize="11" fill="#555">🥇 猫照 (dist=0.05)</text>
  <text x="620" y="90" textAnchor="middle" fontSize="11" fill="#555">🥈 狗照 (dist=0.12)</text>
  <text x="620" y="115" textAnchor="middle" fontSize="11" fill="#555">🥉 文字 (dist=0.58)</text>
  <text x="620" y="140" textAnchor="middle" fontSize="11" fill="#555">④ 汽车 (dist=0.91)</text>
</svg>

向量查询的核心问题是：**给定一个目标向量，在数据库中找到与它距离最近的记录**。

## 距离度量 {#metrics}

不同的度量方式适用于不同的场景，dbVisitor 支持 6 种度量：

<svg viewBox="0 0 720 320" xmlns="http://www.w3.org/2000/svg" style={{maxWidth:'70%',height:'auto'}}>
  <rect x="15" y="10" width="220" height="140" rx="10" fill="#f0f4ff" stroke="#4a7cff" strokeWidth="1.5"/>
  <text x="125" y="35" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#4a7cff">L2 欧氏距离</text>
  <text x="125" y="58" textAnchor="middle" fontSize="11" fill="#555">空间中两点的直线距离</text>
  <text x="125" y="78" textAnchor="middle" fontSize="12" fill="#333" fontFamily="serif,STIXGeneral">d = √Σ(aᵢ - bᵢ)²</text>
  <text x="125" y="100" textAnchor="middle" fontSize="11" fill="#777">值越小 → 越相似</text>
  <text x="125" y="120" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 通用场景首选</text>
  <text x="125" y="140" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;-&gt;</text>
  <rect x="250" y="10" width="220" height="140" rx="10" fill="#f0fff4" stroke="#2da44e" strokeWidth="1.5"/>
  <text x="360" y="35" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#2da44e">Cosine 余弦距离</text>
  <text x="360" y="58" textAnchor="middle" fontSize="11" fill="#555">两向量夹角的余弦补值</text>
  <text x="360" y="78" textAnchor="middle" fontSize="12" fill="#333" fontFamily="serif,STIXGeneral">d = 1 - cos(θ)</text>
  <text x="360" y="100" textAnchor="middle" fontSize="11" fill="#777">值越小 → 方向越接近</text>
  <text x="360" y="120" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 文本语义/NLP</text>
  <text x="360" y="140" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;=&gt;</text>
  <rect x="485" y="10" width="220" height="140" rx="10" fill="#fff8f0" stroke="#e5873a" strokeWidth="1.5"/>
  <text x="595" y="35" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#e5873a">IP 内积</text>
  <text x="595" y="58" textAnchor="middle" fontSize="11" fill="#555">向量的点积（负值排序）</text>
  <text x="595" y="78" textAnchor="middle" fontSize="12" fill="#333" fontFamily="serif,STIXGeneral">d = -Σ(aᵢ × bᵢ)</text>
  <text x="595" y="100" textAnchor="middle" fontSize="11" fill="#777">值越小 → 内积越大</text>
  <text x="595" y="120" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 推荐系统</text>
  <text x="595" y="140" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;#&gt;</text>
  <rect x="15" y="170" width="220" height="140" rx="10" fill="#fdf0ff" stroke="#a855f7" strokeWidth="1.5"/>
  <text x="125" y="195" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#a855f7">Hamming 距离</text>
  <text x="125" y="218" textAnchor="middle" fontSize="11" fill="#555">不同位的个数</text>
  <text x="125" y="238" textAnchor="middle" fontSize="12" fill="#333">适用于二值向量</text>
  <text x="125" y="260" textAnchor="middle" fontSize="11" fill="#777">值越小 → 越相似</text>
  <text x="125" y="280" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 哈希 / 指纹匹配</text>
  <text x="125" y="300" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;~&gt;</text>
  <rect x="250" y="170" width="220" height="140" rx="10" fill="#f0ffff" stroke="#0ea5e9" strokeWidth="1.5"/>
  <text x="360" y="195" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#0ea5e9">Jaccard 距离</text>
  <text x="360" y="218" textAnchor="middle" fontSize="11" fill="#555">集合交并比的补值</text>
  <text x="360" y="238" textAnchor="middle" fontSize="12" fill="#333">d = 1 - |A∩B| / |A∪B|</text>
  <text x="360" y="260" textAnchor="middle" fontSize="11" fill="#777">值越小 → 越相似</text>
  <text x="360" y="280" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 集合 / 标签相似</text>
  <text x="360" y="300" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;%&gt;</text>
  <rect x="485" y="170" width="220" height="140" rx="10" fill="#fffff0" stroke="#ca8a04" strokeWidth="1.5"/>
  <text x="595" y="195" textAnchor="middle" fontSize="14" fontWeight="bold" fill="#ca8a04">BM25</text>
  <text x="595" y="218" textAnchor="middle" fontSize="11" fill="#555">基于词频的文本相关度</text>
  <text x="595" y="238" textAnchor="middle" fontSize="12" fill="#333">经典信息检索评分</text>
  <text x="595" y="260" textAnchor="middle" fontSize="11" fill="#777">值越小 → 越相关</text>
  <text x="595" y="280" textAnchor="middle" fontSize="11" fill="#2da44e">✅ 全文检索</text>
  <text x="595" y="300" textAnchor="middle" fontSize="10" fill="#999">pgvector: &lt;?&gt;</text>
</svg>

:::tip[如何选择度量方式]
- **不确定时**选 L2（欧氏距离），它是最通用的度量方式。
- **文本语义搜索**选 Cosine，它只关注方向不关注向量长度，适合归一化后的 Embedding。
- **推荐/排序场景**选 IP（内积），当向量已归一化时，IP 结果等价于 Cosine 相似度。
:::

## 两种查询模式 {#modes}

dbVisitor 提供两种向量搜索模式，分别对应 SQL 中的 `ORDER BY` 和 `WHERE`：

<svg viewBox="0 0 720 280" xmlns="http://www.w3.org/2000/svg" style={{maxWidth:'70%',height:'auto'}}>
  <rect x="15" y="10" width="340" height="260" rx="12" fill="#f0f4ff" stroke="#4a7cff" strokeWidth="2"/>
  <text x="185" y="38" textAnchor="middle" fontSize="16" fontWeight="bold" fill="#4a7cff">KNN 排序模式 (orderBy*)</text>
  <text x="185" y="60" textAnchor="middle" fontSize="12" fill="#555">返回距离最近的 K 条记录</text>
  <circle cx="170" cy="150" r="80" fill="none" stroke="#ccc" strokeWidth="1" strokeDasharray="4,4"/>
  <circle cx="170" cy="150" r="50" fill="none" stroke="#ccc" strokeWidth="1" strokeDasharray="4,4"/>
  <circle cx="170" cy="150" r="20" fill="none" stroke="#ccc" strokeWidth="1" strokeDasharray="4,4"/>
  <text x="170" y="155" textAnchor="middle" fontSize="18" fill="#e53e3e">⊕</text>
  <text x="170" y="172" textAnchor="middle" fontSize="9" fill="#e53e3e">target</text>
  <circle cx="155" cy="135" r="5" fill="#4a7cff"/><text x="163" y="131" fontSize="9" fill="#4a7cff">①</text>
  <circle cx="190" cy="140" r="5" fill="#4a7cff"/><text x="198" y="136" fontSize="9" fill="#4a7cff">②</text>
  <circle cx="145" cy="165" r="5" fill="#4a7cff"/><text x="153" y="161" fontSize="9" fill="#4a7cff">③</text>
  <circle cx="120" cy="120" r="4" fill="#aaa"/>
  <circle cx="230" cy="180" r="4" fill="#aaa"/>
  <circle cx="100" cy="190" r="4" fill="#aaa"/>
  <circle cx="250" cy="110" r="4" fill="#aaa"/>
  <circle cx="210" cy="210" r="4" fill="#aaa"/>
  <text x="185" y="252" textAnchor="middle" fontSize="11" fill="#333">按距离排序，取前 K 个（蓝色）</text>
  <text x="185" y="268" textAnchor="middle" fontSize="11" fill="#777">SQL: ORDER BY embedding &lt;-&gt; ? LIMIT K</text>
  <rect x="370" y="10" width="340" height="260" rx="12" fill="#f0fff4" stroke="#2da44e" strokeWidth="2"/>
  <text x="540" y="38" textAnchor="middle" fontSize="16" fontWeight="bold" fill="#2da44e">Range 过滤模式 (vectorBy*)</text>
  <text x="540" y="60" textAnchor="middle" fontSize="12" fill="#555">返回距离小于阈值的所有记录</text>
  <circle cx="530" cy="150" r="75" fill="#2da44e" fillOpacity="0.08" stroke="#2da44e" strokeWidth="2" strokeDasharray="6,3"/>
  <text x="610" y="88" fontSize="10" fill="#2da44e">threshold</text>
  <line x1="530" y1="150" x2="605" y2="150" stroke="#2da44e" strokeWidth="1" strokeDasharray="3,3"/>
  <text x="530" y="155" textAnchor="middle" fontSize="18" fill="#e53e3e">⊕</text>
  <text x="530" y="172" textAnchor="middle" fontSize="9" fill="#e53e3e">target</text>
  <circle cx="510" cy="130" r="5" fill="#2da44e"/><text x="518" y="126" fontSize="9" fill="#2da44e">✓</text>
  <circle cx="555" cy="140" r="5" fill="#2da44e"/><text x="563" y="136" fontSize="9" fill="#2da44e">✓</text>
  <circle cx="520" cy="170" r="5" fill="#2da44e"/><text x="528" y="166" fontSize="9" fill="#2da44e">✓</text>
  <circle cx="560" cy="175" r="5" fill="#2da44e"/><text x="568" y="171" fontSize="9" fill="#2da44e">✓</text>
  <circle cx="470" cy="100" r="4" fill="#aaa"/><text x="478" y="97" fontSize="9" fill="#aaa">✗</text>
  <circle cx="620" cy="200" r="4" fill="#aaa"/><text x="628" y="197" fontSize="9" fill="#aaa">✗</text>
  <circle cx="450" cy="200" r="4" fill="#aaa"/><text x="458" y="197" fontSize="9" fill="#aaa">✗</text>
  <text x="540" y="252" textAnchor="middle" fontSize="11" fill="#333">圈内全部返回（绿色），圈外丢弃（灰色）</text>
  <text x="540" y="268" textAnchor="middle" fontSize="11" fill="#777">SQL: WHERE embedding &lt;-&gt; ? &lt; threshold</text>
</svg>

| 对比项     | KNN (`orderBy*`)                  | Range (`vectorBy*`)              |
|:----------|:----------------------------------|:---------------------------------|
| SQL 位置   | `ORDER BY`                        | `WHERE`                          |
| 返回数量   | 固定 K 条（需配合 `initPage`）       | 不固定，取决于阈值               |
| 适用场景   | "找最相似的 N 个"                   | "找所有距离在范围内的"           |
| 可组合性   | 可追加 WHERE 条件做预过滤            | 可与其他 WHERE 条件自由组合      |

## 准备工作 {#prepare}

### 1. 建表

以 PostgreSQL + pgvector 为例，需要安装 `vector` 扩展并创建向量列：

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE product_vector (
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(100),
    embedding vector(128)   -- 128 维向量
);
```

### 2. 实体映射

```java title='向量实体类'
@Table("product_vector")
public class ProductVector {
    @Column(primary = true)
    private Integer id;
    private String name;

    @Column(typeHandler = PgVectorTypeHandler.class)
    private List<Float> embedding;

    // getter / setter ...
}
```

- 向量字段使用 `List<Float>` 表示。
- 需要为向量字段指定 `typeHandler` 来处理 `List<Float>` 与数据库向量类型之间的转换。

:::info[提示]
`PgVectorTypeHandler` 是针对 PostgreSQL pgvector 的实现，利用 `PGobject` 进行 `List<Float>` 与 `vector` 类型的互转。
如果使用其他数据库（如 Milvus），需要使用对应的 TypeHandler。
:::

### 3. 向量参数格式

在 KNN 排序查询中（`orderBy*` 系列），向量参数需要传递**数据库能识别的类型**。  
以 pgvector 为例，不能直接传递 `List<Float>`，需要包装为 `PGobject`：

```java title='构造向量查询参数（pgvector）'
PGobject vectorParam = new PGobject();
vectorParam.setType("vector");
vectorParam.setValue("[0.1,0.2,0.3,...]"); // pgvector 文本格式
```

在 Range 过滤查询中（`vectorBy*` 系列），向量参数会经过实体映射的 `TypeHandler` 自动转换，因此可以直接传 `List<Float>`。

## KNN 排序查询 {#knn}

使用 `orderBy*` 方法按向量距离进行排序，返回距离目标最近的 K 条记录。

### L2 欧氏距离

```java title='按 L2 距离排序'
LambdaTemplate lambda = ...
Object target = ...; // 目标向量（PGobject 或数据库对应类型）

List<ProductVector> results = lambda.query(ProductVector.class)
        .orderByL2(ProductVector::getEmbedding, target)
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector ORDER BY embedding <-> ? ASC
```

### Cosine 余弦距离

```java title='按 Cosine 距离排序'
List<ProductVector> results = lambda.query(ProductVector.class)
        .orderByCosine(ProductVector::getEmbedding, target)
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector ORDER BY embedding <=> ? ASC
```

### IP 内积距离

```java title='按 Inner Product 距离排序'
List<ProductVector> results = lambda.query(ProductVector.class)
        .orderByIP(ProductVector::getEmbedding, target)
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector ORDER BY embedding <#> ? ASC
```

:::info[IP 距离说明]
pgvector 的 `<#>` 运算符返回**负内积**。排序后，内积最大（最相似）的记录排在最前面。
:::

### Top-K 查询

配合 `initPage` 实现只返回最近的 K 条记录：

```java title='Top-K 近邻'
int topK = 5;

List<ProductVector> results = lambda.query(ProductVector.class)
        .orderByL2(ProductVector::getEmbedding, target)
        .initPage(topK, 0)    // 只取前 5 条
        .queryForList();
```

### 通用度量接口

通过 `orderByMetric` 方法可以使用 `MetricType` 枚举动态指定度量方式：

```java title='使用 MetricType 枚举'
import net.hasor.dbvisitor.lambda.core.MetricType;

List<ProductVector> results = lambda.query(ProductVector.class)
        .orderByMetric(MetricType.L2, ProductVector::getEmbedding, target)
        .queryForList();
```

全部可用的度量方式参见上方 **[距离度量](./vector#metrics)** 章节。

| MetricType          | 快捷方法           | pgvector 运算符 |
|:--------------------|:------------------|:--------------|
| `MetricType.L2`     | `orderByL2`       | `<->`         |
| `MetricType.COSINE` | `orderByCosine`   | `<=>`         |
| `MetricType.IP`     | `orderByIP`       | `<#>`         |
| `MetricType.HAMMING`| `orderByHamming`  | `<~>`         |
| `MetricType.JACCARD`| `orderByJaccard`  | `<%>`         |
| `MetricType.BM25`   | `orderByBM25`     | `<?>`         |

## Range 范围过滤 {#range}

使用 `vectorBy*` 方法只返回到目标向量距离小于阈值（threshold）的记录。它属于 **WHERE 条件**，可与其他条件自由组合。

### L2 距离过滤

```java title='L2 距离范围过滤'
List<Float> target = ...;   // 可直接使用 List<Float>
double threshold = 5.0;

List<ProductVector> results = lambda.query(ProductVector.class)
        .vectorByL2(ProductVector::getEmbedding, target, threshold)
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector WHERE embedding <-> ? < ?
```

### Cosine 距离过滤

```java title='Cosine 距离范围过滤'
List<ProductVector> results = lambda.query(ProductVector.class)
        .vectorByCosine(ProductVector::getEmbedding, target, 0.1)
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector WHERE embedding <=> ? < ?
```

### IP 距离过滤

```java title='IP 距离范围过滤'
List<ProductVector> results = lambda.query(ProductVector.class)
        .vectorByIP(ProductVector::getEmbedding, target, -50.0)
        .queryForList();
```

:::info[vectorBy 与 orderBy 参数差异]
`vectorBy*` 的向量参数会经过实体映射的 TypeHandler 自动转换，因此**可以直接传递 `List<Float>`**。  
`orderBy*` 的向量参数直接进入 SQL 参数绑定，需要传递数据库能识别的类型（如 `PGobject`）。
:::

### 条件开关

所有 `vectorBy*` 方法都支持通过第一个 `boolean` 参数控制条件是否生效：

```java title='动态控制向量过滤'
boolean enableVectorFilter = ...;

List<ProductVector> results = lambda.query(ProductVector.class)
        .vectorByL2(enableVectorFilter, ProductVector::getEmbedding, target, threshold)
        .queryForList();

// enableVectorFilter = false 时，向量过滤条件不会出现在 SQL 中
```

## 组合查询 {#combine}

向量查询可以和标量条件自由组合，实现**先过滤再排序**或**先排序再过滤**。

### KNN + 标量过滤

```java title='只在特定类别中做近邻搜索'
List<ProductVector> results = lambda.query(ProductVector.class)
        .likeRight(ProductVector::getName, "Cat-A")            // 标量条件
        .orderByL2(ProductVector::getEmbedding, target)        // 向量排序
        .initPage(3, 0)                                        // Top-3
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector
//    WHERE name LIKE 'Cat-A%'
//    ORDER BY embedding <-> ?
//    LIMIT 3
```

### Range + 标量过滤

```java title='向量范围过滤 + 标量条件'
List<ProductVector> results = lambda.query(ProductVector.class)
        .likeRight(ProductVector::getName, "R-A")              // 标量条件
        .vectorByL2(ProductVector::getEmbedding, target, 6.0)  // 向量范围过滤
        .queryForList();

// 对应的 SQL（pgvector）：
//   SELECT * FROM product_vector
//    WHERE name LIKE 'R-A%'
//      AND embedding <-> ? < ?
```

## 基础操作 {#crud}

向量数据的增删改操作与普通实体完全一致，通过 TypeHandler 自动处理 `List<Float>` 的序列化和反序列化。

```java title='插入向量'
ProductVector p = new ProductVector();
p.setId(1001);
p.setName("sample");
p.setEmbedding(Arrays.asList(0.1f, 0.2f, 0.3f, ...)); // 128 维

lambda.insert(ProductVector.class)
      .applyEntity(p)
      .executeSumResult();
```

```java title='更新向量'
List<Float> newVec = Arrays.asList(0.9f, 0.8f, 0.7f, ...);

lambda.update(ProductVector.class)
      .eq(ProductVector::getId, 1001)
      .updateTo(ProductVector::getEmbedding, newVec)
      .doUpdate();
```

```java title='读取向量'
ProductVector loaded = lambda.query(ProductVector.class)
        .eq(ProductVector::getId, 1001)
        .queryForObject();

List<Float> vec = loaded.getEmbedding(); // 自动反序列化为 List<Float>
```

## API 参考 {#api}

### KNN 排序（QueryFunc 接口）

| 方法                                               | 说明              |
|:--------------------------------------------------|:-----------------|
| `orderByL2(P property, Object vector)`            | 按 L2 距离排序       |
| `orderByCosine(P property, Object vector)`        | 按 Cosine 距离排序   |
| `orderByIP(P property, Object vector)`            | 按 IP 距离排序       |
| `orderByHamming(P property, Object vector)`       | 按 Hamming 距离排序  |
| `orderByJaccard(P property, Object vector)`       | 按 Jaccard 距离排序  |
| `orderByBM25(P property, Object vector)`          | 按 BM25 评分排序     |
| `orderByMetric(MetricType, P property, Object vector)` | 通过枚举指定度量方式 |

### Range 过滤（QueryCompare 接口）

| 方法                                                           | 说明               |
|:-------------------------------------------------------------|:------------------|
| `vectorByL2(P property, Object vector, Number threshold)`    | L2 距离小于阈值        |
| `vectorByCosine(P property, Object vector, Number threshold)`| Cosine 距离小于阈值    |
| `vectorByIP(P property, Object vector, Number threshold)`    | IP 距离小于阈值        |
| `vectorByHamming(P property, Object vector, Number threshold)` | Hamming 距离小于阈值 |
| `vectorByJaccard(P property, Object vector, Number threshold)` | Jaccard 距离小于阈值 |
| `vectorByBM25(P property, Object vector, Number threshold)`  | BM25 距离小于阈值      |

所有 `vectorBy*` 方法均支持 `(boolean test, P property, Object vector, Number threshold)` 形式的重载，用于动态控制条件是否生效。

相关的类
- net.hasor.dbvisitor.lambda.core.MetricType
- net.hasor.dbvisitor.lambda.core.QueryFunc
- net.hasor.dbvisitor.lambda.core.QueryCompare
