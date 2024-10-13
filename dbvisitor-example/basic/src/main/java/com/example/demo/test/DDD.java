package com.example.demo.test;
import com.example.demo.DsUtils;
import net.hasor.dbvisitor.dal.session.DalSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class DDD {

    public static void main(String[] args) throws SQLException, ReflectiveOperationException, IOException {
        DataSource dataSource = DsUtils.dsMySql();
        DalSession reportSession = new DalSession(dataSource);
        reportSession.getDalRegistry().loadMapper(VehicleMapper.class);
        VehicleMapper mapper = reportSession.createMapper(VehicleMapper.class);
        // mapper.updateState(2, "/test", 1, 49356L, new Date(1591860012000L), 2);
    }
}
