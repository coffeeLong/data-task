package com.longlong.exporter;

import com.longlong.exporter.config.ExportService;

import java.util.Set;

public abstract class ExportTaskPool<T> {

    private ExportTask<T> exportTask;

    private int threads;

    protected ExportTaskPool(int threads) {
        this.threads = threads;
    }

    /**
     * 由ExportTask创建时把本身传递到ExportTaskPool
     *
     * @param exportTask
     */
    public void setExportTask(ExportTask<T> exportTask) {
        if (this.exportTask == null) {
            this.exportTask = exportTask;
        }
    }

    public ExportTask<T> getExportTask() {
        return exportTask;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * 同步完成start到end间的所有任务,并把每个子任务获取的数据添加到缓存
     *
     * @param start         导出任务的起始页
     * @param end           导出任务的结束页
     * @param export        导出任务数据处理的对象
     * @param exportService 导出任务获取数据参数
     * @param exceptions    存储异常信息
     * @throws Exception 抛出异常
     */
    public abstract void invoke(int start, int end, T export, ExportService exportService, Set<Throwable> exceptions) throws Exception;

    /**
     * 导出任务线程池停止时调用
     */
    public abstract void shutdown();

}
