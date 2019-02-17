package com.longlong.exporter.task;

/**
 * 导出写入任务
 * @author liaolonglong
 */
public interface DataHandleCompletedTask<T> {
    void run(T export);
}
