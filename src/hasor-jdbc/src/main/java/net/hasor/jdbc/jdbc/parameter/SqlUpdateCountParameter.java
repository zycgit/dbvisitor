package net.hasor.jdbc.jdbc.parameter;
import java.sql.Types;
/**
 * �Ӵ洢���̷��صĸ��¼�¼�������ò����Ǵ���������
 * @version : 2013-10-14
 * @author ������(zyc@hasor.net)
 */
public class SqlUpdateCountParameter extends SqlInOutParameter {
    /** ����һ�� SqlReturnUpdateCount ���� SQL ��������*/
    public SqlUpdateCountParameter(String name) {
        super(name, Types.INTEGER);
    }
}