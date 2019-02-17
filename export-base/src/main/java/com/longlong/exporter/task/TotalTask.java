package com.longlong.exporter.task;


/**
 * 获取总记录数任务
 *
 * @author liaolonglong
 */
public interface TotalTask<T> {
	int run(Object[] params);
}
