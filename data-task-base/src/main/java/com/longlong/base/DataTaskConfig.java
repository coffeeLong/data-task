package com.longlong.base;

import com.longlong.base.task.*;

/**
 * 导出任务配置属性
 *
 * @author liaolonglong
 */
public class DataTaskConfig<T> {

    /**
     * 全局默认获取总记录数任务
     */
    private TotalTask<T> totalTask;

    /**
     * 在开始导出数据之前执行的任务
     */
    private DataBeforeHandleTask<T> beforeHandleTask;
    /**
     * 自定义Excel设置数据任务,默认返回list数据并调用ExportExcel.setDataList(list)
     */
    private DataHandleTask<T> dataHandleTask;
    /**
     * 在结束导出数据后执行的任务
     */
    private DataAfterHandleTask<T> afterHandleTask;
    /**
     * 自定义Excel写入任务，默认调用{@link}ExportExcel的write()方法
     */
    private DataHandleCompletedTask<T> completedTask;

    /**
     * 一次任务执行的任务数
     */
    private Integer taskSize;

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


    DataTaskConfig<T> setTaskSize(Integer taskSize) {
        this.taskSize = taskSize;
        return this;
    }

    public DataTaskConfig<T> setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public DataTaskConfig<T> setCount(Integer count) {
        this.count = count;
        return this;
    }

    public DataTaskConfig<T> setTotalTask(TotalTask<T> totalTask) {
        this.totalTask = totalTask;
        return this;
    }

    public DataTaskConfig<T> setBeforeHandleTask(DataBeforeHandleTask<T> beforeHandleTask) {
        this.beforeHandleTask = beforeHandleTask;
        return this;
    }

    public DataTaskConfig<T> setDataHandleTask(DataHandleTask<T> dataHandleTask) {
        this.dataHandleTask = dataHandleTask;
        return this;
    }

    public DataTaskConfig<T> setAfterHandleTask(DataAfterHandleTask<T> afterHandleTask) {
        this.afterHandleTask = afterHandleTask;
        return this;
    }

    public DataTaskConfig<T> setCompletedTask(DataHandleCompletedTask<T> completedTask) {
        this.completedTask = completedTask;
        return this;
    }

    public DataTaskConfig<T> setTasks(Integer tasks) {
        this.tasks = tasks;
        return this;
    }

    public DataTaskConfig<T> setStopDataTask(Boolean stopDataTask) {
        this.stopDataTask = stopDataTask;
        return this;
    }

    public DataTaskConfig<T> setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public DataTaskConfig<T> setLogErrorMessage(String logErrorMessage) {
        this.logErrorMessage = logErrorMessage;
        return this;
    }

    public TotalTask<T> getTotalTask() {
        return totalTask;
    }

    public DataBeforeHandleTask<T> getBeforeHandleTask() {
        return beforeHandleTask;
    }

    public DataHandleTask<T> getDataHandleTask() {
        return dataHandleTask;
    }

    public DataAfterHandleTask<T> getAfterHandleTask() {
        return afterHandleTask;
    }

    public DataHandleCompletedTask<T> getCompletedTask() {
        return completedTask;
    }

    public Integer getTaskSize() {
        return taskSize;
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
