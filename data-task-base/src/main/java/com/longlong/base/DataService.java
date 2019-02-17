package com.longlong.base;


/**
 *
 * 功能描述: 数据服务接口
 * @author liaolonglong
 * @date: 2019/2/17 上午11:26
 */
public interface DataService {

    /**
     * 设置获取数据的初始参数
     * @return
     */
    Object[] initParams();

    /**
     * 获取数据
     * @param params
     * @param pageNo
     * @return
     */
    Object getData(Object[] params, int pageNo);

    /**
     * 设置获取数据参数的分页大小
     * @param params
     * @param pageSize
     */
    void setParamsPageSize(Object[] params, int pageSize);

    /**
     * 设置获取数据参数的总记录数
     * @param params
     * @param count
     */
    void setParamsCount(Object[] params, int count);

}
