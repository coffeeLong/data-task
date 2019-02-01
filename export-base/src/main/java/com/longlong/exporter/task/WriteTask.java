package com.longlong.exporter.task;

/**
 * Excel导出写入任务
 * @author liaolonglong
 */
public interface WriteTask<T> {
    void run(T export);
}
