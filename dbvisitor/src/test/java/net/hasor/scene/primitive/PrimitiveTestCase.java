package net.hasor.scene.primitive;
import net.hasor.dbvisitor.session.Configuration;
import net.hasor.dbvisitor.wrapper.WrapperAdapter;
import net.hasor.scene.primitive.dto.PrimitiveDTO;
import net.hasor.test.utils.DsUtils;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;

public class PrimitiveTestCase {

    @Test
    public void stringTestCase() throws Exception {
        Configuration conf = new Configuration();
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter template = conf.newWrapper(c);
            template.delete(PrimitiveDTO.class).allowEmptyWhere().doDelete();

            PrimitiveDTO dto = new PrimitiveDTO();
            dto.setId(1);
            dto.setIntValue(1);
            dto.setVarcharValue("string 1");
            template.insert(PrimitiveDTO.class).applyEntity(dto).executeSumResult();
            dto.setId(2);
            dto.setIntValue(2);
            dto.setVarcharValue("string 2");
            template.insert(PrimitiveDTO.class).applyEntity(dto).executeSumResult();

            // using sql
            List<String> strings1 = template.jdbc().queryForList("select c_varchar from tb_h2_types order by id asc", String.class);
            assert strings1.get(0).equals("string 1");
            assert strings1.get(1).equals("string 2");

            // using lambda
            List<String> strings2 = template.query(PrimitiveDTO.class)//
                    .select(PrimitiveDTO::getVarcharValue)  //
                    .asc(PrimitiveDTO::getId)               //
                    .queryForList(String.class);
            assert strings2.get(0).equals("string 1");
            assert strings2.get(1).equals("string 2");

            // using dal
            PrimitiveMapper mapper = conf.newSession(c).createMapper(PrimitiveMapper.class);
            List<String> strings3 = mapper.queryString();
            assert strings3.get(0).equals("string 1");
            assert strings3.get(1).equals("string 2");
        }
    }

    @Test
    public void intTestCase() throws Exception {
        Configuration conf = new Configuration();
        try (Connection c = DsUtils.h2Conn()) {
            WrapperAdapter template = conf.newWrapper(c);
            template.delete(PrimitiveDTO.class).allowEmptyWhere().doDelete();

            PrimitiveDTO dto = new PrimitiveDTO();
            dto.setId(1);
            dto.setIntValue(10);
            dto.setVarcharValue("string 1");
            template.insert(PrimitiveDTO.class).applyEntity(dto).executeSumResult();
            dto.setId(2);
            dto.setIntValue(20);
            dto.setVarcharValue("string 2");
            template.insert(PrimitiveDTO.class).applyEntity(dto).executeSumResult();

            // using sql
            List<Integer> ints1 = template.jdbc().queryForList("select c_int from tb_h2_types order by id asc", Integer.class);
            assert ints1.get(0).equals(10);
            assert ints1.get(1).equals(20);

            // using lambda
            List<Integer> ints2 = template.query(PrimitiveDTO.class)//
                    .select(PrimitiveDTO::getIntValue)  //
                    .asc(PrimitiveDTO::getId)               //
                    .queryForList(Integer.class);
            assert ints2.get(0).equals(10);
            assert ints2.get(1).equals(20);

            // using dal
            PrimitiveMapper mapper = conf.newSession(c).createMapper(PrimitiveMapper.class);
            List<Integer> ints3 = mapper.queryInteger();
            assert ints3.get(0).equals(10);
            assert ints3.get(1).equals(20);
        }
    }
}
