package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.core.DeleteExecute;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryCompare;

/**
 * lambda Delete for Entity.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface EntityDeleteOperation<T> extends //
        CommonOperation<EntityDeleteOperation<T>>, //
        DeleteExecute<EntityDeleteOperation<T>>, //
        QueryCompare<EntityDeleteOperation<T>, SFunction<T>>,//
        EntityQueryCompare<EntityDeleteOperation<T>> {
}
