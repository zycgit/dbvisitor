package net.hasor.db.example.mapper;
import net.hasor.db.dal.repository.Param;

import java.util.List;
import java.util.Map;

public interface Mapper1 {

    public List<Map<String, Object>> queryListByAge(@Param("age") int age);
}
