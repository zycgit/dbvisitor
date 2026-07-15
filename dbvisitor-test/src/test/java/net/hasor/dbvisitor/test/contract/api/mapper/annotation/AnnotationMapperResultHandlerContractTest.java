package net.hasor.dbvisitor.test.contract.api.mapper.annotation;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.dbvisitor.test.contract.material.dao.declarative.ResultHandlerMapper;
import net.hasor.dbvisitor.test.contract.material.model.UserInfo;
import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;
import net.hasor.dbvisitor.test.nxn.junit.NxnContract;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@NxnContract
public abstract class AnnotationMapperResultHandlerContractTest extends AbstractNxnContractTest {
    private static final String PATTERN = "AnnoHandler%";

    private ResultHandlerMapper mapper;

    @Before
    public void createResultHandlerMapper() throws Exception {
        Configuration configuration = newConfiguration();
        Session session = configuration.newSession(dataSource);
        this.mapper = session.createMapper(ResultHandlerMapper.class);
        prepareRows();
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_DEFAULT)
    public void annotationResultHandler_shouldUseDefaultMappingWithoutCustomHandler() throws SQLException {
        List<UserInfo> users = this.mapper.selectDefault(PATTERN);

        assertEquals(10, users.size());
        assertEquals("AnnoHandler1", users.get(0).getName());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR)
    public void annotationResultHandler_shouldUseResultSetExtractor() throws SQLException {
        List<UserInfo> users = this.mapper.selectWithExtractor(PATTERN);

        assertEquals(10, users.size());
        for (UserInfo user : users) {
            assertNotNull(user.getId());
            assertTrue(user.getName().startsWith("AnnoHandler"));
            assertNotNull(user.getCreateTime());
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EXTRACTOR_OPTIONS)
    public void annotationResultHandler_shouldKeepExtractorWhenOptionsArePresent() throws SQLException {
        List<UserInfo> defaultResult = this.mapper.selectDefault(PATTERN);
        List<UserInfo> extractorResult = this.mapper.selectWithExtractorAndOptions(PATTERN);

        assertEquals(defaultResult.size(), extractorResult.size());
        assertEquals(defaultResult.get(0).getId(), extractorResult.get(0).getId());
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER)
    public void annotationResultHandler_shouldUseRowMapperForListResults() throws SQLException {
        List<UserInfo> users = this.mapper.selectWithRowMapper(PATTERN);

        assertEquals(10, users.size());
        for (int i = 0; i < users.size(); i++) {
            assertTrue(users.get(i).getName().contains("[Row" + i + "]"));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_SINGLE)
    public void annotationResultHandler_shouldUseRowMapperForSingleResult() throws SQLException {
        UserInfo user = this.mapper.selectSingleWithRowMapper(id(1));

        assertNotNull(user);
        assertTrue(user.getName().contains("[Row0]"));
        assertTrue(user.getName().contains("AnnoHandler1"));
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_MAPPER_OPTIONS)
    public void annotationResultHandler_shouldKeepRowMapperWhenOptionsArePresent() throws SQLException {
        List<UserInfo> defaultResult = this.mapper.selectDefault(PATTERN);
        List<UserInfo> mappedResult = this.mapper.selectWithRowMapperAndOptions(PATTERN);

        assertEquals(defaultResult.size(), mappedResult.size());
        for (UserInfo user : mappedResult) {
            assertTrue(user.getName().contains("[Row"));
        }
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_ROW_CALLBACK)
    public void annotationResultHandler_shouldExecuteRowCallbackQueries() throws SQLException {
        this.mapper.selectWithRowCallback(PATTERN);
        this.mapper.selectWithRowCallbackAndTimeout(PATTERN);
        this.mapper.selectWithRowCallback("NoAnnoHandlerMatch%");
    }

    @Test
    @Capability(CapabilityId.MAPPER_ANNOTATION_RESULT_HANDLER_EMPTY)
    public void annotationResultHandler_shouldHandleEmptyAndNullResults() throws SQLException {
        assertEquals(0, this.mapper.selectWithExtractor("NoAnnoHandlerMatch%").size());
        assertEquals(0, this.mapper.selectWithRowMapper("NoAnnoHandlerMatch%").size());
        assertNull(this.mapper.selectSingleWithRowMapper(99999));
    }

    private void prepareRows() throws SQLException {
        for (int i = 1; i <= 10; i++) {
            UserInfo user = new UserInfo();
            user.setId(id(i));
            user.setName("AnnoHandler" + i);
            user.setAge(20 + i);
            user.setEmail("anno-handler" + i + "@nxn.test");
            user.setCreateTime(new Date());
            this.mapper.insertUser(user);
        }
    }

    private int id(int index) {
        return 53100 + index;
    }
}
