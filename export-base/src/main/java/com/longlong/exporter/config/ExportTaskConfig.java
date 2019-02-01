package com.longlong.exporter.config;

import com.longlong.exporter.task.*;

/**
 * 导出任务配置属性
 *
 * @author liaolonglong
 */
public class ExportTaskConfig<T> {

    /**
     * 自定义参数任务
     */
    private ParamsTask<T> paramsTask;

    /**
     * 全局默认获取总记录数任务
     */
    private CountTask<T> countTask;

    /**
     * 在开始导出数据之前执行的任务
     */
    private BeforeDataTask<T> beforeDataTask;
    /**
     * 自定义Excel设置数据任务,默认返回list数据并调用ExportExcel.setDataList(list)
     */
    private DataTask<T> dataTask;
    /**
     * 在结束导出数据后执行的任务
     */
    private AfterDataTask<T> afterDataTask;
    /**
     * 自定义Excel写入任务，默认调用{@link}ExportExcel的write()方法
     */
    private WriteTask<T> writeTask;

    /**
     * 一次任务执行的任务数
     */
    private Integer taskSize;

    /**
     * 允许开启最大导出线程数
     */
    private Integer maxThread;

    /**
     * 一页读取的最大记录数
     */
    private Integer pageSize;

    /**
     * 导出的总记录数
     */
    private Integer count;
    /**
     * 指定任务数
     */
    private Integer tasks;
    /**
     * 设置是否停止请求数据的任务，默认false
     */
    private Boolean stopDataTask;
    /**
     * 导出报表失败显示信息
     */
    private String errorMessage;
    /**
     * 记录异常日志显示信息
     */
    private String logErrorMessage;


    public ExportTaskConfig<T> setTaskSize(Integer taskSize) {
        this.taskSize = taskSize;
        return this;
    }

    public ExportTaskConfig<T> setMaxThread(Integer maxThread) {
        this.maxThread = maxThread;
        return this;
    }

    public ExportTaskConfig<T> setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ExportTaskConfig<T> setCount(Integer count) {
        this.count = count;
        return this;
    }

    public ExportTaskConfig<T> setParamsTask(ParamsTask<T> paramsTask) {
        this.paramsTask = paramsTask;
        return this;
    }

    public ExportTaskConfig<T> setCountTask(CountTask<T> countTask) {
        this.countTask = countTask;
        return this;
    }

    public ExportTaskConfig<T> setBeforeDataTask(BeforeDataTask<T> beforeDataTask) {
        this.beforeDataTask = beforeDataTask;
        return this;
    }

    public ExportTaskConfig<T> setDataTask(DataTask<T> dataTask) {
        this.dataTask = dataTask;
        return this;
    }

    public ExportTaskConfig<T> setAfterDataTask(AfterDataTask<T> afterDataTask) {
        this.afterDataTask = afterDataTask;
        return this;
    }

    public ExportTaskConfig<T> setWriteTask(WriteTask<T> writeTask) {
        this.writeTask = writeTask;
        return this;
    }

    public ExportTaskConfig<T> setTasks(Integer tasks) {
        this.tasks = tasks;
        return this;
    }

    public ExportTaskConfig<T> setStopDataTask(Boolean stopDataTask) {
        this.stopDataTask = stopDataTask;
        return this;
    }

    public ExportTaskConfig<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public ExportTaskConfig<T> setLogErrorMessage(String logErrorMessage) {
        this.logErrorMessage = logErrorMessage;
        return this;
    }

    public ParamsTask<T> getParamsTask() {
        return paramsTask;
    }

    public CountTask<T> getCountTask() {
        return countTask;
    }

    public BeforeDataTask<T> getBeforeDataTask() {
        return beforeDataTask;
    }

    public DataTask<T> getDataTask() {
        return dataTask;
    }

    public AfterDataTask<T> getAfterDataTask() {
        return afterDataTask;
    }

    public WriteTask<T> getWriteTask() {
        return writeTask;
    }

    public Integer getTaskSize() {
        return taskSize;
    }

    public Integer getMaxThread() {
        return maxThread;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getTasks() {
        return tasks;
    }

    public Boolean getStopDataTask() {
        return stopDataTask;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getLogErrorMessage() {
        return logErrorMessage;
    }
}
