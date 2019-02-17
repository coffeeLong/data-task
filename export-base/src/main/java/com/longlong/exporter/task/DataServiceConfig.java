package com.longlong.exporter.task;

/**
 * 数据服务接口
 */
public interface DataServiceConfig {

    Object[] initParams();

    Object invoke(Object[] params, int pageNo);

    void setParamsPageSize(Object[] params, int pageSize);

    void setParamsCount(Object[] params, int count);

}
