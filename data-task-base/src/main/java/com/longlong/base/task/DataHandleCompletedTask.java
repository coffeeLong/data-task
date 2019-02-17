package com.longlong.base.task;

/**
 * 所有任务完成后的操作
 * @author liaolonglong
 */
public interface DataHandleCompletedTask<T> {
    void run(T dataHandler);
}
