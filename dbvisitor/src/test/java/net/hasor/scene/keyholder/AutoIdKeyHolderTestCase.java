package net.hasor.scene.keyholder;
import net.hasor.dbvisitor.lambda.InsertOperation;
import net.hasor.dbvisitor.lambda.LambdaTemplate;
import net.hasor.scene.keyholder.dto.UserDTO_32;
import net.hasor.scene.keyholder.dto.UserDTO_36;
import net.hasor.scene.keyholder.dto.UserDTO_KEYHOLDER;
import net.hasor.scene.keyholder.dto.UserDTO_SEQ;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class AutoIdKeyHolderTestCase {

    @Test
    public void autoUUID32TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserDTO_32 userData = new UserDTO_32();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertOperation<UserDTO_32> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO_32.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 32;
        }
    }

    @Test
    public void autoUUID36TestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);

            UserDTO_36 userData = new UserDTO_36();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertOperation<UserDTO_36> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO_36.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            assert userData.getId() != null;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void autoSeqTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.lambdaDelete(UserDTO_SEQ.class).allowEmptyWhere().doDelete();

            UserDTO_SEQ userData = new UserDTO_SEQ();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertOperation<UserDTO_SEQ> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO_SEQ.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 1;
            assert userData.getName().length() == 36;
        }
    }

    @Test
    public void myHolderTestCase() throws SQLException {
        try (Connection c = DsUtils.h2Conn()) {
            LambdaTemplate lambdaTemplate = new LambdaTemplate(c);
            lambdaTemplate.lambdaDelete(UserDTO_KEYHOLDER.class).allowEmptyWhere().doDelete();

            UserDTO_KEYHOLDER userData = new UserDTO_KEYHOLDER();
            userData.setAge(36);
            userData.setCreateTime(new Date());
            assert userData.getId() == null;
            assert userData.getName() == null;

            InsertOperation<UserDTO_KEYHOLDER> lambdaInsert = lambdaTemplate.lambdaInsert(UserDTO_KEYHOLDER.class);
            assert 1 == lambdaInsert.applyEntity(userData).executeSumResult();

            assert userData.getId() == 111111;
            assert userData.getName().length() == 36;
        }
    }

}
