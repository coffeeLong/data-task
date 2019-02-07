package com.longlong.exporter.task;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.exception.ExportException;

/**
 * 导出任务调用接口
 *
 * @param <T>
 */
public interface ExportTask<T> {
    /**
     * 执行导出任务，主要工作是设置参数、参数校验和控制整个流程，核心导出处理流程由doExport方法实现
     *
     * @param export        导出处理对象
     * @param exportService
     * @param config
     * @throws ExportException
     */
    void export(T export, ExportService exportService, ExportTaskConfig<T> config) throws ExportException;

    /**
     * 销毁导出任务对象和导出任务线程池
     */
    void shutdown();
}
