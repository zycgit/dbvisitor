package net.hasor.db.example.quick.dao1;
import net.hasor.db.dal.session.BaseMapper;
import net.hasor.db.dal.session.DalSession;
import net.hasor.db.example.DsUtils;
import net.hasor.db.example.PrintUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class DaoMain {
    public static void main(String[] args) throws SQLException, IOException {
        DataSource dataSource = DsUtils.dsMySql();

        DalSession session = new DalSession(dataSource);
        BaseMapper<TestUser> baseMapper = session.createBaseMapper(TestUser.class);

        baseMapper.template().loadSQL("CreateDB.sql");

        // 查询，所有数据
        List<TestUser> dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        // 插入新数据
        TestUser newUser = new TestUser();
        newUser.setName("new User");
        newUser.setAge(33);
        newUser.setCreateTime(new Date());
        int result1 = baseMapper.insert(newUser);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        // 更新，将name 从 mali 更新为 mala
        TestUser sample = baseMapper.queryById(1);
        sample.setName("mala");
        int result2 = baseMapper.updateById(sample);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        // 删除，ID 为 2 的数据删掉
        int result3 = baseMapper.deleteById(2);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);
    }
}
