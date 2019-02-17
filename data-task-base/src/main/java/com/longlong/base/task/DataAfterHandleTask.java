package com.longlong.base.task;

/**
 * 数据处理之后执行的任务
 *
 * @param <T>
 * @author liaolonglong
 */
public interface DataAfterHandleTask<T> {
    /**
     * @param dataHandler 数据处理对象
     */
    void run(T dataHandler);
}
