package com.example.demo.quick.dao1;
import com.example.demo.DsUtils;
import com.example.demo.PrintUtils;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.session.DalSession;

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
        int result1 = baseMapper.save(newUser);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        // 更新，将name 从 mali 更新为 mala
        TestUser sample = baseMapper.getById(1);
        sample.setName("mala");
        int result2 = baseMapper.saveOrUpdate(sample);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);

        // 删除，ID 为 2 的数据删掉
        int result3 = baseMapper.deleteById(2);

        dtoList = baseMapper.query().queryForList();
        PrintUtils.printObjectList(dtoList);
    }
}
