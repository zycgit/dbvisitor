package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.QueryFunc;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryCompare;

/**
 * lambda Query for Entity.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface EntityQueryOperation<T> extends //
        CommonOperation<EntityQueryOperation<T>>, //
        QueryFunc<EntityQueryOperation<T>, T, SFunction<T>>, //
        QueryCompare<EntityQueryOperation<T>, SFunction<T>>,//
        EntityQueryCompare<EntityQueryOperation<T>> {
}
