package net.hasor.dbvisitor.lambda;
/**
 *
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface CommonOperation<R> {
    /** 参考的样本对象 */
    Class<?> exampleType();

    R useQualifier();
}
