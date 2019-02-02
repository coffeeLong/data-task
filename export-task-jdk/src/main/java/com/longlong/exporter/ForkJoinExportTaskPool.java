package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.task.DataCallable;
import com.longlong.exporter.task.ExportTaskPool;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * forkjoin线程池实现导出任务
 *
 * @author liaolonglong
 */
public class ForkJoinExportTaskPool<T> implements ExportTaskPool<T> {

    private ForkJoinPool forkJoinPool;

    public ForkJoinExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ForkJoinExportTaskPool(int threads) {
        forkJoinPool = new ForkJoinPool(threads);
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config, ConcurrentMap<Integer, Object> cache, Set<Throwable> exceptions) {
        forkJoinPool.invoke(new ForkJoinTask(start, end, exportService, config, cache, exceptions));
    }

    @Override
    public void shutdown() {
        forkJoinPool.shutdown();
    }

    private class ForkJoinTask extends RecursiveAction {
        private final int start;
        private final int end;
        private final ExportService exportService;
        private final ExportTaskConfig config;
        private final ConcurrentMap<Integer, Object> cache;
        private final Set<Throwable> exceptions;

        private ForkJoinTask(int start, int end, ExportService exportService, ExportTaskConfig config, ConcurrentMap<Integer, Object> cache, Set<Throwable> exceptions) {
            this.start = start;
            this.end = end;
            this.exportService = exportService;
            this.config = config;
            this.cache = cache;
            this.exceptions = exceptions;
        }

        @Override
        protected void compute() {
            if (end == start) {
                try {
                    Object res = new DataCallable(end, exportService, config).call();
                    cache.put(end, res);
                } catch (Exception e) {
                    exceptions.add(e);
                }
            } else {
                //如果任务大于阀值，就分裂成两个子任务计算
                int mid = (start + end) >>> 1;
                ForkJoinTask leftTask = new ForkJoinTask(start, mid, exportService, config, cache, exceptions);
                ForkJoinTask rightTask = new ForkJoinTask(mid + 1, end, exportService, config, cache, exceptions);
                invokeAll(leftTask, rightTask);
            }
        }
    }


}
