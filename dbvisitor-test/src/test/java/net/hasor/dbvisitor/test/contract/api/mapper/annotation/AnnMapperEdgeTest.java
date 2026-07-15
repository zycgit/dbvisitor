package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.AnnotationTestMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.capability.FeatureId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@NxnContract
public abstract class AnnMapperEdgeTest extends AbstractNxnContractTest {
    private AnnotationTestMapper mapper;

    @Before
    public void createAnnMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(AnnotationTestMapper.class);
    }

    protected int baseId() {
        return 496000;
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NULL_VALUE)
    public void annotationMapperInsertNullColValues() throws Exception {
        int id = baseId() + 1;

        assertEquals(1, this.mapper.insertUserWithParams(id, "AnnoNull", null, null));

        UserInfo loaded = this.mapper.selectById(id);
        assertNotNull(loaded);
        assertEquals("AnnoNull", loaded.getName());
        assertNull(loaded.getAge());
        assertNull(loaded.getEmail());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_SPECIAL_TEXT)
    public void annotationMapperInsertQuotesAndUnicodeAsValues() throws Exception {
        int quotedId = baseId() + 2;
        int unicodeId = baseId() + 3;

        assertEquals(1, this.mapper.insertUserWithParams(quotedId, "O'Brien & Co.", 28, "quoted@nxn.test"));
        assertEquals(1, this.mapper.insertUserWithParams(unicodeId, "测试用户 テスト", 25, "unicode@nxn.test"));

        assertEquals("O'Brien & Co.", this.mapper.selectById(quotedId).getName());
        assertEquals("测试用户 テスト", this.mapper.selectById(unicodeId).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_NO_MATCH_AFFECTED_ROWS)
    public void annotationMapperDmlZeroWhenNoRowsMatch() throws Exception {
        requiresNxnFeature(FeatureId.EXACT_MUTATION_AFFECTED_ROWS);

        assertEquals(0, this.mapper.updateUserAge(baseId() + 1000, 50));
        assertEquals(0, this.mapper.deleteById(baseId() + 1000));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_EXECUTE_ERROR)
    public void annotationMapperExecuteErrorAfterDroppingTemporaryTable() throws Exception {
        dropTableIfExists("temp_anno_test");
        jdbcTemplate.executeUpdate(createSimpleTempTableSql("temp_anno_test"));
        dropTableIfExists("temp_anno_test");

        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectTempData(1);
            }
        });
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_SQL_ERROR)
    public void annotationMapperSqlErrorsFromSyntaxTableColAndDupKeyFailures() throws Exception {
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectWithSyntaxError();
            }
        });
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectFromNonExistentTable();
            }
        });
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.selectNonExistentColumn();
            }
        });
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_DUPLICATE_KEY)
    public void annotationMapperDupKeyDupPrimaryKeyFailure() throws Exception {
        requiresNxnFeature(FeatureId.DUPLICATE_PRIMARY_KEY_REJECTED);

        int id = baseId() + 4;
        assertEquals(1, this.mapper.insertUserWithParams(id, "AnnoPkOne", 25, "pk1@nxn.test"));
        expectMapperFailure(new FailingMapperCall() {
            @Override
            public void run() throws Exception {
                mapper.insertUserWithParams(id, "AnnoPkTwo", 26, "pk2@nxn.test");
            }
        });
    }

    private void expectMapperFailure(FailingMapperCall call) throws Exception {
        try {
            call.run();
        } catch (Exception e) {
            assertNotNull(e);
            return;
        }
        throw new AssertionError("Expected mapper call to fail.");
    }

    private interface FailingMapperCall {
        void run() throws Exception;
    }
}
