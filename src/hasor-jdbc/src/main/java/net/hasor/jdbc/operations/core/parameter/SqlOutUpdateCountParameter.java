package net.hasor.jdbc.operations.core.parameter;
import java.sql.Types;
/**
 * ����һ�������ķ��ؽ������ʾ�Ӵ洢���̷��صĸ��¼�¼������
 * @version : 2013-10-14
 * @author ������(zyc@hasor.net)
 */
public class SqlOutUpdateCountParameter extends SqlOutVarParameter {
    /** ����һ�� SqlReturnUpdateCount ���� SQL ��������*/
    public SqlOutUpdateCountParameter(String name) {
        super(name, Types.INTEGER);
    }
}