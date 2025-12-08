package net.hasor.scene.mongodb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.session.Session;
import net.hasor.scene.mongodb.dto1.UserInfo1;
import net.hasor.scene.mongodb.dto1.UserInfo1BaseMapper;
import net.hasor.test.AbstractDbTest;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class MongoLambdaTest extends AbstractDbTest {
    @Test
    public void testLambdaCRUD() throws SQLException {
        try (Connection c = DsUtils.mongoConn()) {
            LambdaTemplate lambda = new LambdaTemplate(c);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("test_user");
            user.setLoginName("test_login");
            user.setLoginPassword("password");
            int r1 = lambda.insert(UserInfo1.class).applyEntity(user).executeSumResult();
            assert r1 == 1;

            // Select
            UserInfo1 l1 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert l1 != null;
            assert "test_user".equals(l1.getName());

            // Update
            int r2 = lambda.update(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).updateTo(UserInfo1::getName, "updated_user").doUpdate();
            assert r2 == 1;

            // Verify Update
            UserInfo1 l2 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert "updated_user".equals(l2.getName());

            // Delete
            int r3 = lambda.delete(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).doDelete();
            assert r3 == 1;
            UserInfo1 l3 = lambda.query(UserInfo1.class).eq(UserInfo1::getUid, user.getUid()).queryForObject();
            assert l3 == null;
        }
    }

    @Test
    public void testGenericMapperCRUD() throws Exception {
        Configuration config = new Configuration();
        try (Session session = config.newSession(DsUtils.mongoConn())) {
            UserInfo1BaseMapper mapper = session.createMapper(UserInfo1BaseMapper.class);

            // Insert
            UserInfo1 user = new UserInfo1();
            user.setUid(UUID.randomUUID().toString());
            user.setName("mapper_user");
            user.setLoginName("mapper_login");
            user.setLoginPassword("password");
            int r1 = mapper.insert(user);
            assert r1 == 1;

            // Select
            UserInfo1 l1 = mapper.selectById(user.getUid());
            assert l1 != null;
            assert "mapper_user".equals(l1.getName());

            // Update
            user.setName("mapper_updated");
            int r2 = mapper.update(user);
            assert r2 == 1;

            // Verify Update
            UserInfo1 l2 = mapper.selectById(user.getUid());
            assert "mapper_updated".equals(l2.getName());

            // Delete
            int r3 = mapper.deleteById(user.getUid());
            assert r3 == 1;
            UserInfo1 l3 = mapper.selectById(user.getUid());
            assert l3 == null;
        }
    }
}
