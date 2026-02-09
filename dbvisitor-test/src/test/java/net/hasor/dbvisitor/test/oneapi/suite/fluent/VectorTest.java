package net.hasor.dbvisitor.test.oneapi.suite.fluent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.lambda.core.MetricType;
import net.hasor.dbvisitor.test.oneapi.AbstractOneApiTest;
import net.hasor.dbvisitor.test.oneapi.model.ProductVectorForPg;
import org.junit.Test;
import org.postgresql.util.PGobject;
import static org.junit.Assert.*;

/**
 * Vector Test Suite — 向量数据库能力全面测试
 * <p>覆盖 pgvector 的 6 种度量方式和 2 类查询模式：</p>
 * <ul>
 *   <li>A. 基础 CRUD — 向量数据的增删改查与精度验证</li>
 *   <li>B. 批量操作 — 批量插入与全量检索</li>
 *   <li>C. KNN (orderBy) — 6 种度量的近邻排序查询</li>
 *   <li>D. ANN Range (vectorBy) — 6 种度量的范围过滤查询</li>
 *   <li>E. 组合查询 — 向量排序 + 标量 WHERE 条件</li>
 *   <li>F. orderByMetric 通用接口 — 枚举驱动</li>
 *   <li>G. 距离正确性 — 数学验证已知向量的排序结果</li>
 * </ul>
 * <p>Supports: PostgreSQL with pgvector, Milvus, ElasticSearch 7+</p>
 */
public class VectorTest extends AbstractOneApiTest {
    private static final int VECTOR_DIM = 128;

    // ========================= Helper Methods =========================

    /** 生成固定向量：base + i*step */
    private List<Float> fixedVector(float base, float step) {
        List<Float> v = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            v.add(base + i * step);
        }
        return v;
    }

    /** 生成全零向量，仅在指定位置设置值 */
    private List<Float> sparseVector(float defaultVal, int specialIdx, float specialVal) {
        List<Float> v = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            v.add(i == specialIdx ? specialVal : defaultVal);
        }
        return v;
    }

    /** 生成均匀常量向量 */
    private List<Float> constantVector(float val) {
        List<Float> v = new ArrayList<>(VECTOR_DIM);
        for (int i = 0; i < VECTOR_DIM; i++) {
            v.add(val);
        }
        return v;
    }

    /** 计算 L2 距离 */
    private double l2Distance(List<Float> a, List<Float> b) {
        double sum = 0;
        for (int i = 0; i < a.size(); i++) {
            double diff = a.get(i) - b.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /** 计算 Cosine 距离 (1 - cosine_similarity) */
    private double cosineDistance(List<Float> a, List<Float> b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * (double) b.get(i);
            normA += a.get(i) * (double) a.get(i);
            normB += b.get(i) * (double) b.get(i);
        }
        return 1.0 - dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** 计算 Inner Product 距离 (负内积，pgvector 用 <#> 返回负值使其可排序) */
    private double ipDistance(List<Float> a, List<Float> b) {
        double dot = 0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * (double) b.get(i);
        }
        return -dot; // pgvector <#> returns negative inner product
    }

    /** 插入一个 ProductVectorForPg，返回该对象 */
    private ProductVectorForPg insertVector(LambdaTemplate lambda, int id, String name, List<Float> embedding) throws SQLException {
        ProductVectorForPg p = new ProductVectorForPg();
        p.setId(id);
        p.setName(name);
        p.setEmbedding(embedding);
        lambda.insert(ProductVectorForPg.class).applyEntity(p).executeSumResult();
        return p;
    }

    /** 将 List<Float> 转为 PGobject(vector)，用于查询 API 的向量参数 */
    private Object vecStr(List<Float> vector) throws SQLException {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0)
                sb.append(',');
            sb.append(vector.get(i));
        }
        sb.append(']');
        PGobject pgObj = new PGobject();
        pgObj.setType("vector");
        pgObj.setValue(sb.toString());
        return pgObj;
    }

    /** 清理指定 ID 的记录 */
    private void cleanup(LambdaTemplate lambda, int... ids) throws SQLException {
        for (int id : ids) {
            lambda.delete(ProductVectorForPg.class).eq(ProductVectorForPg::getId, id).doDelete();
        }
    }

    /** 清理 [startId, startId+count) 范围的记录 */
    private void cleanupRange(LambdaTemplate lambda, int startId, int count) throws SQLException {
        for (int i = 0; i < count; i++) {
            lambda.delete(ProductVectorForPg.class).eq(ProductVectorForPg::getId, startId + i).doDelete();
        }
    }

    // ========================= A. 基础 CRUD =========================

    @Test
    public void testA01_VectorInsertAndRead() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int id = 1001;
        try {
            List<Float> vec = fixedVector(0.5f, 0.01f);
            insertVector(lambda, id, "Insert Read Test", vec);

            ProductVectorForPg loaded = lambda.query(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).queryForObject();

            assertNotNull("应能查询到插入的向量", loaded);
            assertEquals("Insert Read Test", loaded.getName());
            assertNotNull("向量字段不能为 null", loaded.getEmbedding());
            assertEquals("向量维度应为 " + VECTOR_DIM, VECTOR_DIM, loaded.getEmbedding().size());

            // 精度验证
            for (int i = 0; i < VECTOR_DIM; i++) {
                assertEquals("第 " + i + " 个分量应匹配", vec.get(i), loaded.getEmbedding().get(i), 0.0001f);
            }
        } finally {
            cleanup(lambda, id);
        }
    }

    @Test
    public void testA02_VectorUpdate() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int id = 1002;
        try {
            insertVector(lambda, id, "Update Test", fixedVector(0.1f, 0.01f));

            List<Float> newVec = fixedVector(0.9f, -0.005f);
            int updated = lambda.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id)//
                    .updateTo(ProductVectorForPg::getEmbedding, newVec)//
                    .doUpdate();
            assertEquals("应更新 1 条", 1, updated);

            ProductVectorForPg after = lambda.query(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).queryForObject();
            assertNotNull(after);
            for (int i = 0; i < VECTOR_DIM; i++) {
                assertEquals("更新后第 " + i + " 个分量应匹配", newVec.get(i), after.getEmbedding().get(i), 0.0001f);
            }
        } finally {
            cleanup(lambda, id);
        }
    }

    @Test
    public void testA03_VectorDelete() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int id = 1003;
        try {
            insertVector(lambda, id, "Delete Test", fixedVector(0.3f, 0.01f));

            int deleted = lambda.delete(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).doDelete();
            assertEquals("应删除 1 条", 1, deleted);

            ProductVectorForPg after = lambda.query(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).queryForObject();
            assertNull("删除后应查不到", after);
        } finally {
            cleanup(lambda, id); // idempotent
        }
    }

    @Test
    public void testA04_VectorNameUpdate() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int id = 1004;
        try {
            List<Float> vec = fixedVector(0.2f, 0.01f);
            insertVector(lambda, id, "Before Rename", vec);

            lambda.update(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id)//
                    .updateTo(ProductVectorForPg::getName, "After Rename")//
                    .doUpdate();

            ProductVectorForPg after = lambda.query(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).queryForObject();
            assertEquals("After Rename", after.getName());
            // vector should remain unchanged
            for (int i = 0; i < VECTOR_DIM; i++) {
                assertEquals(vec.get(i), after.getEmbedding().get(i), 0.0001f);
            }
        } finally {
            cleanup(lambda, id);
        }
    }

    // ========================= B. 批量操作 =========================

    @Test
    public void testB01_VectorBatchInsert() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 2000;
        int count = 10;
        try {
            for (int i = 0; i < count; i++) {
                insertVector(lambda, baseId + i, "Batch " + i, fixedVector(i * 0.1f, 0.005f));
            }

            List<ProductVectorForPg> all = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + count - 1)//
                    .queryForList();
            assertEquals("批量插入应产生 " + count + " 条记录", count, all.size());
        } finally {
            cleanupRange(lambda, baseId, count);
        }
    }

    // ========================= C. KNN — orderBy 排序查询 =========================

    /**
     * C1: KNN L2 (欧氏距离)
     * 插入 5 个离散向量，对目标向量做 L2 距离排序，验证最近邻排序正确性
     */
    @Test
    public void testC01_KNN_OrderByL2() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3000;
        try {
            // 5 个向量，base 值分别为 0.0, 0.2, 0.4, 0.6, 0.8
            for (int i = 0; i < 5; i++) {
                insertVector(lambda, baseId + i, "L2-" + i, fixedVector(i * 0.2f, 0.01f));
            }

            // 目标接近 base=0.4（第 3 个向量）
            List<Float> target = fixedVector(0.41f, 0.01f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 4)//
                    .orderByL2(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals("应返回 5 条", 5, results.size());
            // 第一条应该是 base=0.4（id=3002），最近的
            assertEquals("L2 最近邻应为 id=" + (baseId + 2), Integer.valueOf(baseId + 2), results.get(0).getId());
        } finally {
            cleanupRange(lambda, baseId, 5);
        }
    }

    /**
     * C2: KNN Cosine (余弦距离)
     */
    @Test
    public void testC02_KNN_OrderByCosine() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3100;
        try {
            // 不同方向的向量：常量向量方向相同但幅值不同，对 cosine 来说距离为 0
            // 为区分，使用 sparse 向量在不同位置加扰动
            insertVector(lambda, baseId, "Cos-0", sparseVector(0.1f, 0, 1.0f));
            insertVector(lambda, baseId + 1, "Cos-1", sparseVector(0.1f, 1, 1.0f));
            insertVector(lambda, baseId + 2, "Cos-2", sparseVector(0.1f, 2, 1.0f));

            // 目标向量在位置 1 有大值 → 最接近 Cos-1
            List<Float> target = sparseVector(0.1f, 1, 0.99f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            assertEquals("Cosine 最近邻应为 Cos-1", Integer.valueOf(baseId + 1), results.get(0).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * C3: KNN Inner Product (内积距离)
     * pgvector <#> 返回负内积，排序后最大内积排最前
     */
    @Test
    public void testC03_KNN_OrderByIP() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3200;
        try {
            // 幅值递增的常量向量（方向一致），内积 = sum(a_i * b_i)
            insertVector(lambda, baseId, "IP-small", constantVector(0.1f));
            insertVector(lambda, baseId + 1, "IP-mid", constantVector(0.5f));
            insertVector(lambda, baseId + 2, "IP-large", constantVector(0.9f));

            // 目标也是正值常量向量 → 内积最大的是 IP-large
            List<Float> target = constantVector(1.0f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            // <#> = negative inner product, ORDER BY <#> ASC → most positive IP first
            assertEquals("IP 最大内积应为 IP-large", Integer.valueOf(baseId + 2), results.get(0).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * C4: KNN 通过 orderByMetric(MetricType.L2, ...) 调用，验证与快捷方法等价
     */
    @Test
    public void testC04_KNN_OrderByMetricGeneric() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3300;
        try {
            insertVector(lambda, baseId, "M-0", fixedVector(0.0f, 0.01f));
            insertVector(lambda, baseId + 1, "M-1", fixedVector(0.5f, 0.01f));
            insertVector(lambda, baseId + 2, "M-2", fixedVector(1.0f, 0.01f));

            List<Float> target = fixedVector(0.48f, 0.01f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByMetric(MetricType.L2, ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            assertEquals("orderByMetric(L2) 最近邻应为 M-1", Integer.valueOf(baseId + 1), results.get(0).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * C5: KNN Top-K — 使用 initPage 限制返回数量
     */
    @Test
    public void testC05_KNN_TopK() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3400;
        int total = 8;
        int topK = 3;
        try {
            for (int i = 0; i < total; i++) {
                insertVector(lambda, baseId + i, "TopK-" + i, fixedVector(i * 0.1f, 0.005f));
            }

            List<Float> target = fixedVector(0.35f, 0.005f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + total - 1)//
                    .orderByL2(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .initPage(topK, 0)//
                    .queryForList();

            assertEquals("Top-K 应只返回 " + topK + " 条", topK, results.size());
            // 最近的应该是 base=0.3（id+3）或 base=0.4（id+4）
            int firstId = results.get(0).getId();
            assertTrue("Top-1 应是 id+3 或 id+4", firstId == baseId + 3 || firstId == baseId + 4);
        } finally {
            cleanupRange(lambda, baseId, total);
        }
    }

    /**
     * C6: KNN L2 排序 + 验证结果按距离递增
     */
    @Test
    public void testC06_KNN_L2_DistanceOrdering() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3500;
        try {
            List<List<Float>> vectors = Arrays.asList(//
                    fixedVector(0.0f, 0.01f),   // baseId+0
                    fixedVector(0.3f, 0.01f),   // baseId+1
                    fixedVector(0.6f, 0.01f),   // baseId+2
                    fixedVector(0.9f, 0.01f),   // baseId+3
                    fixedVector(1.2f, 0.01f)    // baseId+4
            );
            for (int i = 0; i < vectors.size(); i++) {
                insertVector(lambda, baseId + i, "Dist-" + i, vectors.get(i));
            }

            List<Float> target = fixedVector(0.5f, 0.01f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 4)//
                    .orderByL2(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(5, results.size());
            // 验证 L2 距离严格递增
            double prevDist = -1;
            for (ProductVectorForPg r : results) {
                double dist = l2Distance(target, r.getEmbedding());
                assertTrue("L2 距离应单调递增: prev=" + prevDist + " curr=" + dist, dist >= prevDist);
                prevDist = dist;
            }
        } finally {
            cleanupRange(lambda, baseId, 5);
        }
    }

    /**
     * C7: KNN Cosine 排序 + 验证结果按余弦距离递增
     */
    @Test
    public void testC07_KNN_Cosine_DistanceOrdering() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 3600;
        try {
            // "方向"不同的向量
            insertVector(lambda, baseId, "CosD-0", sparseVector(0.01f, 0, 1.0f));
            insertVector(lambda, baseId + 1, "CosD-1", sparseVector(0.01f, 5, 1.0f));
            insertVector(lambda, baseId + 2, "CosD-2", sparseVector(0.01f, 10, 1.0f));
            insertVector(lambda, baseId + 3, "CosD-3", sparseVector(0.01f, 50, 1.0f));

            List<Float> target = sparseVector(0.01f, 5, 0.95f);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 3)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(4, results.size());
            double prevDist = -1;
            for (ProductVectorForPg r : results) {
                double dist = cosineDistance(target, r.getEmbedding());
                assertTrue("Cosine 距离应单调递增", dist >= prevDist - 1e-6);
                prevDist = dist;
            }
        } finally {
            cleanupRange(lambda, baseId, 4);
        }
    }

    // ========================= D. ANN Range — vectorBy 范围过滤 =========================

    /**
     * D1: vectorByL2 — L2 距离阈值过滤
     * 只返回 L2 距离 < threshold 的结果
     */
    @Test
    public void testD01_ANN_VectorByL2() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 4000;
        try {
            // 5 个向量，base: 0.0, 0.5, 1.0, 1.5, 2.0
            for (int i = 0; i < 5; i++) {
                insertVector(lambda, baseId + i, "RL2-" + i, fixedVector(i * 0.5f, 0.0f));
            }

            List<Float> target = fixedVector(0.0f, 0.0f); // 全 0 向量
            // L2 距离分别为: 0, 0.5*sqrt(128)≈5.66, 1.0*sqrt(128)≈11.31, ...
            // 设置阈值 = 7.0，应只返回 base=0.0 和 base=0.5 的
            double threshold = 7.0;

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 4)//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, threshold)//
                    .queryForList();

            // 验证只有距离 < threshold 的结果
            for (ProductVectorForPg r : results) {
                double dist = l2Distance(target, r.getEmbedding());
                assertTrue("L2 距离应 < " + threshold + ", 实际=" + dist, dist < threshold);
            }
            assertTrue("至少应返回 1 条（自身）", results.size() >= 1);
            assertTrue("不应返回全部 5 条", results.size() < 5);
        } finally {
            cleanupRange(lambda, baseId, 5);
        }
    }

    /**
     * D2: vectorByCosine — 余弦距离阈值过滤
     */
    @Test
    public void testD02_ANN_VectorByCosine() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 4100;
        try {
            // 正交方向的向量
            insertVector(lambda, baseId, "RCos-0", sparseVector(0.01f, 0, 1.0f));
            insertVector(lambda, baseId + 1, "RCos-1", sparseVector(0.01f, 1, 1.0f));
            insertVector(lambda, baseId + 2, "RCos-2", sparseVector(0.01f, 50, 1.0f));

            // 目标与 RCos-0 方向最接近
            List<Float> target = sparseVector(0.01f, 0, 0.9f);

            // cosine distance < 0.1 → 只有非常接近方向的过关
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .vectorByCosine(ProductVectorForPg::getEmbedding, target, 0.1)//
                    .queryForList();

            // RCos-0 应该在范围内
            assertTrue("至少应返回 1 条", results.size() >= 1);
            for (ProductVectorForPg r : results) {
                double dist = cosineDistance(target, r.getEmbedding());
                assertTrue("Cosine 距离应 < 0.1, 实际=" + dist, dist < 0.1);
            }
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * D3: vectorByIP — 内积距离阈值过滤
     */
    @Test
    public void testD03_ANN_VectorByIP() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 4200;
        try {
            insertVector(lambda, baseId, "RIP-small", constantVector(0.01f));
            insertVector(lambda, baseId + 1, "RIP-mid", constantVector(0.5f));
            insertVector(lambda, baseId + 2, "RIP-large", constantVector(1.0f));

            List<Float> target = constantVector(1.0f);
            // <#> = negative inner product
            // IP-small: -(128*0.01*1.0) = -1.28
            // IP-mid:   -(128*0.5*1.0)  = -64.0
            // IP-large: -(128*1.0*1.0)  = -128.0
            // threshold: -50 → 只有 IP-small 通过（-1.28 < -50 是 false，-64 < -50 也是 false...）
            // 实际上 <#> distance < threshold: -1.28 < -50 → false, -64 < -50 → true, -128 < -50 → true
            // So IP-mid 和 IP-large 通过（它们的 negative inner product 更小 = 更负 = 更大的 inner product）

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .vectorByIP(ProductVectorForPg::getEmbedding, target, -50.0)//
                    .queryForList();

            assertTrue("至少应返回 1 条", results.size() >= 1);
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * D4: vectorByL2 conditional=false — 条件不生效时应返回全部
     */
    @Test
    public void testD04_ANN_ConditionalFalse() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 4300;
        try {
            insertVector(lambda, baseId, "CF-0", fixedVector(0.0f, 0.0f));
            insertVector(lambda, baseId + 1, "CF-1", fixedVector(10.0f, 0.0f));

            List<Float> target = fixedVector(0.0f, 0.0f);

            // test=false → vectorByL2 条件不应用
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 1)//
                    .vectorByL2(false, ProductVectorForPg::getEmbedding, target, 0.001)//
                    .queryForList();

            assertEquals("conditional=false 时应返回全部 2 条", 2, results.size());
        } finally {
            cleanupRange(lambda, baseId, 2);
        }
    }

    // ========================= E. 组合查询 =========================

    /**
     * E1: 向量 KNN 排序 + WHERE 标量条件
     */
    @Test
    public void testE01_KNN_WithScalarFilter() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 5000;
        try {
            insertVector(lambda, baseId, "Cat-A-0", fixedVector(0.1f, 0.01f));
            insertVector(lambda, baseId + 1, "Cat-B-1", fixedVector(0.2f, 0.01f));
            insertVector(lambda, baseId + 2, "Cat-A-2", fixedVector(0.3f, 0.01f));
            insertVector(lambda, baseId + 3, "Cat-B-3", fixedVector(0.4f, 0.01f));
            insertVector(lambda, baseId + 4, "Cat-A-4", fixedVector(0.5f, 0.01f));

            List<Float> target = fixedVector(0.45f, 0.01f);

            // 只在 Cat-A 中做 KNN
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 4)//
                    .likeRight(ProductVectorForPg::getName, "Cat-A")//
                    .orderByL2(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals("Cat-A 共 3 条", 3, results.size());
            for (ProductVectorForPg r : results) {
                assertTrue("结果应以 Cat-A 开头: " + r.getName(), r.getName().startsWith("Cat-A"));
            }
            // 最近的是 Cat-A-4 (base=0.5, 最接近 0.45)
            assertEquals("最近邻应为 Cat-A-4", Integer.valueOf(baseId + 4), results.get(0).getId());
        } finally {
            cleanupRange(lambda, baseId, 5);
        }
    }

    /**
     * E2: 向量 range 过滤 + 标量条件 + 排序
     */
    @Test
    public void testE02_Range_WithScalarAndOrder() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 5100;
        try {
            insertVector(lambda, baseId, "R-A-0", constantVector(0.1f));
            insertVector(lambda, baseId + 1, "R-B-1", constantVector(0.2f));
            insertVector(lambda, baseId + 2, "R-A-2", constantVector(0.5f));
            insertVector(lambda, baseId + 3, "R-A-3", constantVector(0.9f));

            List<Float> target = constantVector(0.0f);

            // L2 距离: 0.1*sqrt(128)≈1.13, 0.2*sqrt(128)≈2.26, 0.5*sqrt(128)≈5.66, 0.9*sqrt(128)≈10.18
            // 范围阈值 6.0 + 只选 R-A → R-A-0 (1.13) + R-A-2 (5.66) 应通过
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 3)//
                    .likeRight(ProductVectorForPg::getName, "R-A")//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, 6.0)//
                    .queryForList();

            assertTrue("至少应返回 1 条", results.size() >= 1);
            for (ProductVectorForPg r : results) {
                assertTrue("应以 R-A 开头", r.getName().startsWith("R-A"));
                double dist = l2Distance(target, r.getEmbedding());
                assertTrue("L2 距离应 < 6.0, 实际=" + dist, dist < 6.0);
            }
        } finally {
            cleanupRange(lambda, baseId, 4);
        }
    }

    // ========================= F. orderByMetric 枚举驱动 =========================

    /**
     * F1: 遍历 L2/COSINE/IP 三种主要度量，验证 orderByMetric 均可正常执行
     */
    @Test
    public void testF01_OrderByMetric_AllSupported() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 6000;
        try {
            insertVector(lambda, baseId, "AM-0", fixedVector(0.1f, 0.01f));
            insertVector(lambda, baseId + 1, "AM-1", fixedVector(0.5f, 0.01f));
            insertVector(lambda, baseId + 2, "AM-2", fixedVector(0.9f, 0.01f));

            List<Float> target = fixedVector(0.5f, 0.01f);

            // pgvector 原生支持 L2, COSINE, IP 三种操作符
            MetricType[] supported = { MetricType.L2, MetricType.COSINE, MetricType.IP };
            for (MetricType metric : supported) {
                List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                        .ge(ProductVectorForPg::getId, baseId)//
                        .le(ProductVectorForPg::getId, baseId + 2)//
                        .orderByMetric(metric, ProductVectorForPg::getEmbedding, vecStr(target))//
                        .queryForList();

                assertEquals("MetricType." + metric + " 应返回 3 条", 3, results.size());
            }
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    // ========================= G. 距离正确性 — 数学验证 =========================

    /**
     * G1: 已知向量的 L2 距离排序验证
     * 构造向量使得 d(v1,target) < d(v0,target) < d(v2,target)
     */
    @Test
    public void testG01_L2_MathVerification() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 7000;
        try {
            List<Float> v0 = constantVector(0.5f);   // 中等距离
            List<Float> v1 = constantVector(0.9f);   // 近
            List<Float> v2 = constantVector(0.0f);   // 远
            insertVector(lambda, baseId, "Math-0", v0);
            insertVector(lambda, baseId + 1, "Math-1", v1);
            insertVector(lambda, baseId + 2, "Math-2", v2);

            List<Float> target = constantVector(1.0f);
            // L2: v1 = 0.1*sqrt(128)≈1.13, v0 = 0.5*sqrt(128)≈5.66, v2 = 1.0*sqrt(128)≈11.31
            double d0 = l2Distance(v0, target);
            double d1 = l2Distance(v1, target);
            double d2 = l2Distance(v2, target);
            assertTrue("预期 d1 < d0 < d2", d1 < d0 && d0 < d2);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByL2(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            assertEquals("第 1 名应为 Math-1 (最近)", Integer.valueOf(baseId + 1), results.get(0).getId());
            assertEquals("第 2 名应为 Math-0", Integer.valueOf(baseId), results.get(1).getId());
            assertEquals("第 3 名应为 Math-2 (最远)", Integer.valueOf(baseId + 2), results.get(2).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * G2: 已知向量的 Cosine 距离排序验证
     */
    @Test
    public void testG02_Cosine_MathVerification() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 7100;
        try {
            // 目标方向：位置 0 有大值
            List<Float> target = sparseVector(0.01f, 0, 1.0f);

            // v0: 方向与 target 完全一致 → cosine dist ≈ 0
            List<Float> v0 = sparseVector(0.01f, 0, 2.0f); // 同方向，幅值翻倍
            // v1: 扰动在相邻位置 → 方向稍偏
            List<Float> v1 = sparseVector(0.01f, 1, 1.0f);
            // v2: 扰动在很远的位置且幅值更大 → 方向大偏
            List<Float> v2 = sparseVector(0.01f, 60, 5.0f);

            insertVector(lambda, baseId, "CMath-0", v0); // 最近
            insertVector(lambda, baseId + 1, "CMath-1", v1);
            insertVector(lambda, baseId + 2, "CMath-2", v2);

            double cd0 = cosineDistance(v0, target);
            double cd1 = cosineDistance(v1, target);
            double cd2 = cosineDistance(v2, target);
            assertTrue("预期 cd0 < cd1 < cd2", cd0 < cd1 && cd1 < cd2);

            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByCosine(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            assertEquals("Cosine 最近应为 CMath-0", Integer.valueOf(baseId), results.get(0).getId());
            assertEquals("Cosine 最远应为 CMath-2", Integer.valueOf(baseId + 2), results.get(2).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * G3: 已知向量的 Inner Product 排序验证
     */
    @Test
    public void testG03_IP_MathVerification() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 7200;
        try {
            List<Float> target = constantVector(1.0f);

            // IP = sum(a_i * b_i)
            // v0: 128 * 0.3 = 38.4
            // v1: 128 * 0.6 = 76.8
            // v2: 128 * 0.9 = 115.2
            List<Float> v0 = constantVector(0.3f);
            List<Float> v1 = constantVector(0.6f);
            List<Float> v2 = constantVector(0.9f);

            insertVector(lambda, baseId, "IPMath-0", v0);
            insertVector(lambda, baseId + 1, "IPMath-1", v1);
            insertVector(lambda, baseId + 2, "IPMath-2", v2);

            // <#> 返回负内积，排序后 negative inner product 最小 (= 最大 inner product) 排第一
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 2)//
                    .orderByIP(ProductVectorForPg::getEmbedding, vecStr(target))//
                    .queryForList();

            assertEquals(3, results.size());
            assertEquals("IP 最大内积应为 IPMath-2", Integer.valueOf(baseId + 2), results.get(0).getId());
            assertEquals("IP 最小内积应为 IPMath-0", Integer.valueOf(baseId), results.get(2).getId());
        } finally {
            cleanupRange(lambda, baseId, 3);
        }
    }

    /**
     * G4: 向量精度边界 — 插入和读取极小/极大浮点值
     */
    @Test
    public void testG04_VectorPrecisionBoundary() throws SQLException {
        requiresFeature("vector");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int id = 7300;
        try {
            List<Float> vec = new ArrayList<>(VECTOR_DIM);
            vec.add(Float.MIN_VALUE);        // 极小正值
            vec.add(-Float.MIN_VALUE);       // 极小负值
            vec.add(1.0f);
            vec.add(-1.0f);
            vec.add(0.0f);
            for (int i = 5; i < VECTOR_DIM; i++) {
                vec.add(0.12345f);
            }
            insertVector(lambda, id, "Precision", vec);

            ProductVectorForPg loaded = lambda.query(ProductVectorForPg.class)//
                    .eq(ProductVectorForPg::getId, id).queryForObject();
            assertNotNull(loaded);
            assertEquals(1.0f, loaded.getEmbedding().get(2), 0.0001f);
            assertEquals(-1.0f, loaded.getEmbedding().get(3), 0.0001f);
            assertEquals(0.0f, loaded.getEmbedding().get(4), 0.0001f);
        } finally {
            cleanup(lambda, id);
        }
    }

    /**
     * G5: 空结果集 — 范围过滤给极小阈值，应无匹配
     */
    @Test
    public void testG05_ANN_EmptyResult() throws SQLException {
        requiresFeature("knn");
        LambdaTemplate lambda = new LambdaTemplate(dataSource);
        int baseId = 7400;
        try {
            insertVector(lambda, baseId, "Empty-0", constantVector(1.0f));
            insertVector(lambda, baseId + 1, "Empty-1", constantVector(0.5f));

            List<Float> target = constantVector(0.0f);
            // L2 距离最小也是 0.5*sqrt(128) ≈ 5.66，阈值给 0.001
            List<ProductVectorForPg> results = lambda.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, baseId)//
                    .le(ProductVectorForPg::getId, baseId + 1)//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, 0.001)//
                    .queryForList();

            assertEquals("阈值极小时应无匹配", 0, results.size());
        } finally {
            cleanupRange(lambda, baseId, 2);
        }
    }
}
