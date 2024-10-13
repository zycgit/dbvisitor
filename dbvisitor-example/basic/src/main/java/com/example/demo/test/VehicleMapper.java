package com.example.demo.test;
import net.hasor.dbvisitor.dal.mapper.BaseMapper;
import net.hasor.dbvisitor.dal.repository.Param;
import net.hasor.dbvisitor.dal.repository.Query;
import net.hasor.dbvisitor.dal.repository.RefMapper;

import java.util.List;

@RefMapper("/mapper/VehicleMapper.xml")
public interface VehicleMapper extends BaseMapper<Vehicle> {
    /**
     * 查询全部车
     * @param updateTime:
     * @return
     **/
    List<VehicleG> queryByUpdateTime(@Param("updateTime") String updateTime);

    /**
     * 查询车辆id通过车组id
     * @param groupIds:
     * @return
     **/
    @Query(value = "select vehicleId from vehicle @{and, groupId in (:groupIds)}", resultType = Long.class)
    List<Long> queryVehicleIdByGroupIds(@Param("groupIds") List<Long> groupIds);
}