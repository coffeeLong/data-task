package com.longlong.exporter.task;

/**
 * 添加每批数据到数据处理对象
 * @author liaolonglong
 */
public interface DataTask<T> {
	/**
	 * 
	 * @param export
	 *            Excel导出工具类对象
	 * @param pageData
	 *            获取数据任务返回结果
	 */
	void run(T export, Object pageData);
}
