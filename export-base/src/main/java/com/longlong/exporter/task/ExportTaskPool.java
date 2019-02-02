package com.longlong.exporter.task;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
     */
    void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config,ConcurrentMap<Integer, Object> cache, Set<Throwable> exceptions);

    /**
     * 导出任务线程池停止时调用
     */
    void shutdown();

}
