package com.longlong.base.task;

/**
 * 数据处理之前执行的任务
 * @author liaolonglong
 * @param <T>
 */
public interface DataBeforeHandleTask<T> {
    void run(T dataHandler);
}
