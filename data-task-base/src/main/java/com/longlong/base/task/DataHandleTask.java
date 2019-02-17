package com.longlong.base.task;

/**
 * 添加数据到数据处理对象
 * @author liaolonglong
 */
public interface DataHandleTask<T> {
	/**
	 * 
	 * @param dataHandler
	 *            Excel导出工具类对象
	 * @param pageData
	 *            获取数据任务返回结果
	 */
	void run(T dataHandler, Object pageData);
}
