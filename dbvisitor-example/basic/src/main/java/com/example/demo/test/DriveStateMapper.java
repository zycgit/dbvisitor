package com.example.demo.test;
import net.hasor.dbvisitor.dal.repository.SimpleMapper;
import net.hasor.dbvisitor.dal.repository.Update;

import java.util.Date;

@SimpleMapper()
public interface DriveStateMapper {

    @Update(value = "update drivestate set result = #{arg0}, filePath = #{arg1}, fileType = #{arg2} where vehicleId = #{arg3} and reocrdTime = #{arg4} and state = #{arg5}")
    void updateState(int result, String filePath, int fileType, long vehicleId, Date time, int state);
}
