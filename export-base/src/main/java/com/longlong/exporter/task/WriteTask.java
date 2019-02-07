package com.longlong.exporter.task;

/**
 * 导出写入任务
 * @author liaolonglong
 */
public interface WriteTask<T> {
    void run(T export);
}
