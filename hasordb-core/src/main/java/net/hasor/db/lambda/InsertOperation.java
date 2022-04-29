package net.hasor.db.lambda;
import net.hasor.db.lambda.core.InsertExecute;

/**
 * lambda Insert for Entity.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface InsertOperation<T> extends  //
        CommonOperation<InsertOperation<T>>, //
        InsertExecute<InsertOperation<T>, T> {
}