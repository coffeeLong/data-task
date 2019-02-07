package com.longlong.exporter.task;

/**
 * 数据处理之前执行的任务
 * @author liaolonglong
 * @param <T>
 */
public interface BeforeDataTask<T> {
    void run(T export);
}
