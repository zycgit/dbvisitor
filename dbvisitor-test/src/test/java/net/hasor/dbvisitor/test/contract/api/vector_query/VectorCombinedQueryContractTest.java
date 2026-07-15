package net.hasor.dbvisitor.test.contract.api.vector_query;

import java.sql.SQLException;
import java.util.List;
import net.hasor.dbvisitor.test.contract.material.model.ProductVectorForPg;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

@NxnContract
public abstract class VectorCombinedQueryContractTest extends VectorQuerySupport {
    @Test
    @Capability(CapabilityId.VECTOR_RANGE_WITH_SCALAR_FILTER)
    public void knn_shouldComposeVectorRangeWithScalarPredicates() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 460;
        try {
            insertVector(startId, "Range-A-0", constantVector(0.1f));
            insertVector(startId + 1, "Range-B-1", constantVector(0.2f));
            insertVector(startId + 2, "Range-A-2", constantVector(0.5f));
            insertVector(startId + 3, "Range-A-3", constantVector(0.9f));

            List<Float> target = constantVector(0.0f);
            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 3)//
                    .likeRight(ProductVectorForPg::getName, "Range-A")//
                    .vectorByL2(ProductVectorForPg::getEmbedding, target, 6.0)//
                    .queryForList();

            assertFalse(rows.isEmpty());
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Range-A"));
                assertTrue(l2Distance(target, row.getEmbedding()) < 6.0);
            }
        } finally {
            cleanupRange(startId, 4);
        }
    }

    @Test
    @Capability(CapabilityId.VECTOR_KNN_WITH_SCALAR_FILTER)
    public void knn_shouldComposeVectorOrderingWithScalarPredicates() throws SQLException {
        requiresNxnFeature(FeatureId.KNN);
        int startId = baseId() + 500;
        try {
            insertVector(startId, "Cat-A-0", fixedVector(0.1f, 0.01f));
            insertVector(startId + 1, "Cat-B-1", fixedVector(0.2f, 0.01f));
            insertVector(startId + 2, "Cat-A-2", fixedVector(0.3f, 0.01f));
            insertVector(startId + 3, "Cat-B-3", fixedVector(0.4f, 0.01f));
            insertVector(startId + 4, "Cat-A-4", fixedVector(0.5f, 0.01f));

            List<ProductVectorForPg> rows = lambdaTemplate.query(ProductVectorForPg.class)//
                    .ge(ProductVectorForPg::getId, startId)//
                    .le(ProductVectorForPg::getId, startId + 4)//
                    .likeRight(ProductVectorForPg::getName, "Cat-A")//
                    .orderByL2(ProductVectorForPg::getEmbedding, pgVector(fixedVector(0.45f, 0.01f)))//
                    .queryForList();

            assertEquals(3, rows.size());
            for (ProductVectorForPg row : rows) {
                assertTrue(row.getName().startsWith("Cat-A"));
            }
            assertEquals(Integer.valueOf(startId + 4), rows.get(0).getId());
        } finally {
            cleanupRange(startId, 5);
        }
    }

}
