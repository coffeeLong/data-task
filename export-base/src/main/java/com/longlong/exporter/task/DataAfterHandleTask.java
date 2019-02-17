package com.longlong.exporter.task;

/**
 * 数据处理之后执行的任务
 * @author liaolonglong
 * @param <T>
 */
public interface DataAfterHandleTask<T> {
	/**
	 * 
	 * @param export
	 *            Excel导出工具类对象
	 */
	void run(T export);
}
