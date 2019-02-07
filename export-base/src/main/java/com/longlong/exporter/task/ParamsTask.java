package com.longlong.exporter.task;

/**
 * 参数处理任务
 *
 * @author liaolonglong
 */
public interface ParamsTask<T> {
    /**
     * Excel设置数据执行任务
     *
     * @param params
     */
    void run(Object[] params, int pageNo, int pageSize, int count);
}
