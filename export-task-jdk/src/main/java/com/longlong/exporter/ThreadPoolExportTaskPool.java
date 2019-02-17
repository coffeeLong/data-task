package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;

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
public class ThreadPoolExportTaskPool<T> extends ExportTaskPool<T> {

    private final ThreadPoolExecutor threadPoolExecutor;


    public ThreadPoolExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ThreadPoolExportTaskPool(int threads) {
        super(threads);
        this.threadPoolExecutor = new ThreadPoolExecutor(threads, threads, 30L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
                (r, executor) -> r.run());
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, Set<Throwable> exceptions) throws Exception {
        List<ExportTask<T>.LoadDataCallable> tasks = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            tasks.add(getExportTask().new LoadDataCallable(i, exportService, exceptions));
        }

        threadPoolExecutor.invokeAll(tasks);

    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }


}
