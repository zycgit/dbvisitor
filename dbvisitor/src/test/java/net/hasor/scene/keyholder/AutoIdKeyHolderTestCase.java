package net.hasor.scene.keyholder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.lambda.Insert;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.scene.keyholder.dto.UserDTO_32;
import net.hasor.scene.keyholder.dto.UserDTO_36;
import net.hasor.scene.keyholder.dto.UserDTO_KEYHOLDER;
import net.hasor.scene.keyholder.dto.UserDTO_SEQ;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

public class AutoIdKeyHolderTestCase {

    @Test
    public void autoUUID32TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c);
            wrapper.delete(UserDTO_32.class).allowEmptyWhere().doDelete();

            UserDTO_32 userData = new UserDTO_32();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            Insert<UserDTO_32> insert = wrapper.insert(UserDTO_32.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 32;
        }
    }

    @Test
    public void autoUUID36TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c);
            wrapper.delete(UserDTO_36.class).allowEmptyWhere().doDelete();

            UserDTO_36 userData = new UserDTO_36();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            Insert<UserDTO_36> insert = wrapper.insert(UserDTO_36.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void autoSeqTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            Options o = Options.of().dialect(SqlDialectRegister.findDialect(c));
            LambdaTemplate wrapper = new LambdaTemplate(c, o);
            wrapper.delete(UserDTO_SEQ.class).allowEmptyWhere().doDelete();

            UserDTO_SEQ userData = new UserDTO_SEQ();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            Insert<UserDTO_SEQ> insert = wrapper.insert(UserDTO_SEQ.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 1;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void myHolderTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate wrapper = new LambdaTemplate(c);
            wrapper.delete(UserDTO_KEYHOLDER.class).allowEmptyWhere().doDelete();

            UserDTO_KEYHOLDER userData = new UserDTO_KEYHOLDER();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            Insert<UserDTO_KEYHOLDER> insert = wrapper.insert(UserDTO_KEYHOLDER.class);
            assert 1 == insert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 111111;
            assert userData.getName().length() == 36;
        }
    }
}
