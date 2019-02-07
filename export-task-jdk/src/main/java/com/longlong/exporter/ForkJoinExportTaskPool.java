package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;
import com.longlong.exporter.config.ExportTaskConfig;
import com.longlong.exporter.task.AbstractExportTaskPool;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * forkjoin线程池实现导出任务
 *
 * @author liaolonglong
 */
public class ForkJoinExportTaskPool<T> extends AbstractExportTaskPool<T> {

    private final ForkJoinPool forkJoinPool;


    public ForkJoinExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ForkJoinExportTaskPool(int threads) {
        forkJoinPool = new ForkJoinPool(threads);
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, ExportTaskConfig<T> config, Set<Throwable> exceptions) throws Exception {
        forkJoinPool.invoke(new ForkJoinTask(start, end, exportService, config, exceptions));
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
        private final Set<Throwable> exceptions;

        private ForkJoinTask(int start, int end, ExportService exportService, ExportTaskConfig config, Set<Throwable> exceptions) {
            this.start = start;
            this.end = end;
            this.exportService = exportService;
            this.config = config;
            this.exceptions = exceptions;
        }

        @Override
        protected void compute() {
            if (end == start) {
                try {
                    getExportTask().new LoadDataCallable(end, exportService, config, exceptions).call();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            } else {
                //如果任务大于阀值，就分裂成两个子任务计算
                int mid = (start + end) >>> 1;
                ForkJoinTask leftTask = new ForkJoinTask(start, mid, exportService, config, exceptions);
                ForkJoinTask rightTask = new ForkJoinTask(mid + 1, end, exportService, config, exceptions);
                invokeAll(leftTask, rightTask);
            }
        }
    }


}
