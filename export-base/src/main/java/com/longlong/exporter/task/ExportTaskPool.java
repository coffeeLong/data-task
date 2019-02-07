package com.longlong.exporter.task;

import com.longlong.exporter.SingleExportTask;
import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;

import java.util.Set;

/**
 * 管理导出任务线程池
 *
 * @author liaolonglong
 */
public interface ExportTaskPool<T> {

    /**
     * 同步完成start到end间的所有任务,并把每个子任务获取的数据添加到缓存
     * @param start         导出任务的起始页
     * @param end           导出任务的结束页
     * @param export        导出任务数据处理的对象
     * @param exportService 导出任务获取数据参数
     * @param config        导出任务当前属性配置
     * @param exceptions    存储异常信息
     * @throws Exception    抛出异常
     */
    void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config, Set<Throwable> exceptions) throws Exception;

    /**
     * 导出任务线程池停止时调用
     */
    void shutdown();

    /**
     * 由ExportTask创建时把本身传递到ExportTaskPool
     * @param exportTask
     */
    void exportTask(SingleExportTask<T> exportTask);

}
