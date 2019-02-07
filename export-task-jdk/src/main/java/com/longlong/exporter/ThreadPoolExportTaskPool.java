package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.task.ExportTaskPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * forkjoin线程池实现导出任务
 *
 * @author liaolonglong
 */
public class ThreadPoolExportTaskPool<T> implements ExportTaskPool<T> {

    private final ThreadPoolExecutor threadPoolExecutor;

    private SingleExportTask<T> exportTask;


    public ThreadPoolExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ThreadPoolExportTaskPool(int threads) {
        this.threadPoolExecutor = new ThreadPoolExecutor(threads, threads, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
                (r, executor) -> r.run());
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config, Set<Throwable> exceptions) throws Exception {
        List<SingleExportTask<T>.LoadDataCallable> tasks = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            tasks.add(exportTask.new LoadDataCallable(i, exportService, config, exceptions));
        }

        threadPoolExecutor.invokeAll(tasks);

    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    @Override
    public void exportTask(SingleExportTask<T> exportTask) {
        this.exportTask = exportTask;
    }


}
