package net.hasor.dbvisitor.test.suite.mapping.annotation;
import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.test.AbstractOneApiTest;
import net.hasor.dbvisitor.test.model.UserInfo;
import net.hasor.dbvisitor.test.model.annotation.DiffPkEntity;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Primary Key Mapping Test
 * 验证 @Column(primary=true) 注解的主键元数据解析和主键条件操作。
 */
public class PrimaryKeyMappingTest extends AbstractOneApiTest {

    /**
     * @Column(primary=true) 元数据验证。
     * 标注了 primary=true 的属性在 TableMapping 中应被识别为主键。
     */
    @Test
    public void testPrimaryKeyMetadata() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class);

        TableMapping<?> mapping = registry.findByEntity(UserInfo.class);
        assertNotNull(mapping);

        // id 标注了 @Column(primary=true)，应是主键
        ColumnMapping idCol = mapping.getPropertyByName("id");
        assertNotNull("id property should be mapped", idCol);
        assertTrue("id should be primary key", idCol.isPrimaryKey());

        // name 未标注 primary，不应是主键
        ColumnMapping nameCol = mapping.getPropertyByName("name");
        assertNotNull(nameCol);
        assertFalse("name should NOT be primary key", nameCol.isPrimaryKey());

        // age 未标注 primary，不应是主键
        ColumnMapping ageCol = mapping.getPropertyByName("age");
        assertNotNull(ageCol);
        assertFalse("age should NOT be primary key", ageCol.isPrimaryKey());
    }

    /**
     * 多实体类的主键元数据一致性验证。
     * 不同实体类中标注 @Column(primary=true) 的字段都应被正确识别。
     */
    @Test
    public void testPrimaryKeyMetadata_MultipleEntities() throws Exception {
        MappingRegistry registry = new MappingRegistry();
        registry.loadEntityToSpace(UserInfo.class, "", "userInfo");
        registry.loadEntityToSpace(DiffPkEntity.class, "", "diffPk");

        // UserInfo 的 id 是主键
        TableMapping<?> userMapping = registry.findBySpace("", "userInfo");
        ColumnMapping userId = userMapping.getPropertyByName("id");
        assertTrue("UserInfo.id should be primary key", userId.isPrimaryKey());

        // DiffPkEntity 的 code 是主键，id 不是主键
        TableMapping<?> diffMapping = registry.findBySpace("", "diffPk");
        ColumnMapping diffCode = diffMapping.getPropertyByName("code");
        ColumnMapping diffId = diffMapping.getPropertyByName("id");

        assertNotNull(diffCode);
        assertTrue("DiffPkEntity.code should be primary key", diffCode.isPrimaryKey());

        assertNotNull(diffId);
        assertFalse("DiffPkEntity.id should NOT be primary key", diffId.isPrimaryKey());
    }

    /**
     * 通过主键条件进行更新操作。
     * 使用 eq(主键字段) 条件更新，验证主键条件过滤正确。
     */
    @Test
    public void testPrimaryKeyConditionUpdate() throws SQLException {
        LambdaTemplate lambda = new LambdaTemplate(dataSource);

        // 插入两条不同主键的数据
        UserInfo user1 = createUser(38701, "PKUpdate1", 25);
        UserInfo user2 = createUser(38702, "PKUpdate2", 30);
        lambda.insert(UserInfo.class).applyEntity(user1).executeSumResult();
        lambda.insert(UserInfo.class).applyEntity(user2).executeSumResult();

        // 通过主键条件更新第一条
        int updated = lambda.update(UserInfo.class)//
                .eq(UserInfo::getId, 38701)//
                .updateTo(UserInfo::getName, "PKUpdated")//
                .doUpdate();
        assertEquals(1, updated);

        // 验证只有第一条被更新，第二条不受影响
        UserInfo loaded1 = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 38701).queryForObject();
        UserInfo loaded2 = lambda.query(UserInfo.class)//
                .eq(UserInfo::getId, 38702).queryForObject();

        assertEquals("PKUpdated", loaded1.getName());
        assertEquals("PKUpdate2", loaded2.getName()); // 未被影响
    }

    /**
     * Helper: 创建 UserInfo
     */
    private UserInfo createUser(int id, String name, Integer age) {
        UserInfo u = new UserInfo();
        u.setId(id);
        u.setName(name);
        u.setAge(age);
        u.setEmail(name.toLowerCase() + "@pk.test");
        u.setCreateTime(new Date());
        return u;
    }
}
