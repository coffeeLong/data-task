package com.longlong.exporter.task;

/**
 * 
 * @author liaolonglong
 *
 */
public interface AfterDataTask<T> {
	/**
	 * 
	 * @param export
	 *            Excel导出工具类对象
	 */
	void exec(T export);
}
