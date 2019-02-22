package com.longlong.base;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * forkjoin线程池管理数据服务任务
 *
 * @author liaolonglong
 */
public class ForkJoinDataServicePool<T> extends AbstractDataServicePool<T> {

    private final ForkJoinPool forkJoinPool;

    public ForkJoinDataServicePool() {
        this(Runtime.getRuntime().availableProcessors() * 2);
    }

    public ForkJoinDataServicePool(int threads) {
        super(threads);
        forkJoinPool = new ForkJoinPool(threads);
    }

    @Override
    public void invoke(int start, int end, DataTaskManager.LoadDataService loadDataService) throws Exception {
        forkJoinPool.invoke(new ForkJoinTask(start, end, loadDataService));
    }

    @Override
    public void shutdown() {
        forkJoinPool.shutdown();
    }

    private class ForkJoinTask extends RecursiveAction {
        private final int start;
        private final int end;
        private final DataTaskManager.LoadDataService loadDataService;

        private ForkJoinTask(int start, int end, DataTaskManager.LoadDataService loadDataService) {
            this.start = start;
            this.end = end;
            this.loadDataService = loadDataService;
        }

        @Override
        protected void compute() {
            if (end == start) {
                loadDataService.loadData(end);
            } else {
                //如果任务大于阀值，就分裂成两个子任务计算
                int mid = (start + end) >>> 1;
                ForkJoinTask leftTask = new ForkJoinTask(start, mid, loadDataService);
                ForkJoinTask rightTask = new ForkJoinTask(mid + 1, end, loadDataService);
                invokeAll(leftTask, rightTask);
            }
        }
    }


}
