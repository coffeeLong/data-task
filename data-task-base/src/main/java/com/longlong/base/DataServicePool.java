package com.longlong.base;

/**
 * 管理加载数据的线程池
 * @author liaolonglong
 */
public abstract class DataServicePool<T> {

    private DataTaskManager<T> dataTaskManager;

    private int threads;

    protected DataServicePool(int threads) {
        this.threads = threads;
    }

    /**
     * 由ExportTask创建时把本身传递到ExportTaskPool
     *
     * @param dataTaskManager
     */
    public void setDataTaskManager(DataTaskManager<T> dataTaskManager) {
        if (this.dataTaskManager == null) {
            this.dataTaskManager = dataTaskManager;
        }
    }

    public DataTaskManager<T> getDataTaskManager() {
        return dataTaskManager;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * 同步完成start到end间的所有任务,并把每个子任务获取的数据添加到缓存
     *
     * @param start           导出任务的起始页
     * @param end             导出任务的结束页
     * @param loadDataService 导出任务获取数据参数
     * @throws Exception 抛出异常
     */
    public abstract void invoke(int start, int end, DataTaskManager.LoadDataService loadDataService) throws Exception;

    /**
     * 导出任务线程池停止时调用
     */
    public abstract void shutdown();

}
