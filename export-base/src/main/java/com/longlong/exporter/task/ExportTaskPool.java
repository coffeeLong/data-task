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

    void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config, Set<Throwable> exceptions);

    void shutdown();

    void refresh(int threads);


    void cache(ConcurrentMap<Integer, Object> cache);

}
