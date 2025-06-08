package net.hasor.dbvisitor.driver;

//
// pending > receive > finish > ready > pending
//
public enum AdapterReceiveState {
    /** 就绪状，表示随时可以开始新的查询。如果连接上发起多次查询，当前一个查询的所有结果都被读取后也会进入就绪状态。 */
    Ready(true),
    /** 等待中，表示正在或者已经将查询请求发送给远程数据库服务器，数据库服务器尚未响应，通常是指在第一条数据到达之前。*/
    Pending(false),
    /** 接收中，表示查询已经完毕，服务器开始向客户端传送查询结果数据。 */
    Receive(false),
    /** 完成，所有查询结果全部接收完毕，但是发起程序还在处理中。当所有结果都被处理完毕会进入 Ready */
    Finish(true),

    ;
    private final boolean finish;

    public boolean isFinish() {
        return this.finish;
    }

    AdapterReceiveState(boolean finish) {
        this.finish = finish;
    }
}
