package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;

import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * forkjoin线程池实现导出任务
 *
 * @author liaolonglong
 */
public class ForkJoinExportTaskPool<T> extends ExportTaskPool<T> {

    private final ForkJoinPool forkJoinPool;

    public ForkJoinExportTaskPool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ForkJoinExportTaskPool(int threads) {
        super(threads);
        forkJoinPool = new ForkJoinPool(threads);
    }


    @Override
    public void invoke(int start, int end, T export, ExportService exportService, Set<Throwable> exceptions) throws Exception {
        forkJoinPool.invoke(new ForkJoinTask(start, end, exportService, exceptions));
    }

    @Override
    public void shutdown() {
        forkJoinPool.shutdown();
    }

    private class ForkJoinTask extends RecursiveAction {
        private final int start;
        private final int end;
        private final ExportService exportService;
        private final Set<Throwable> exceptions;

        private ForkJoinTask(int start, int end, ExportService exportService, Set<Throwable> exceptions) {
            this.start = start;
            this.end = end;
            this.exportService = exportService;
            this.exceptions = exceptions;
        }

        @Override
        protected void compute() {
            if (end == start) {
                try {
                    getExportTask().new LoadDataCallable(end, exportService, exceptions).call();
                } catch (Exception e) {
                    exceptions.add(e);
                }
            } else {
                //如果任务大于阀值，就分裂成两个子任务计算
                int mid = (start + end) >>> 1;
                ForkJoinTask leftTask = new ForkJoinTask(start, mid, exportService, exceptions);
                ForkJoinTask rightTask = new ForkJoinTask(mid + 1, end, exportService, exceptions);
                invokeAll(leftTask, rightTask);
            }
        }
    }


}
