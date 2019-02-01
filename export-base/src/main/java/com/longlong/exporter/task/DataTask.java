package com.longlong.exporter.task;

public interface DataTask<T> {
	/**
	 * 
	 * @param export
	 *            Excel导出工具类对象
	 * @param pageData
	 *            获取数据任务返回结果
	 */
	void exec(T export, Object pageData);
}
