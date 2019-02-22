package com.longlong.base;


/**
 * 功能描述: 数据服务接口
 *
 * @author liaolonglong
 * @date: 2019/2/17 上午11:26
 */
public interface DataService {

    /**
     * @return 获取数据的初始参数
     */
    Object[] initParams();

    /**
     * @param params 数据服务获取数据需传入的参数，与初始参数相比可能会修改总记录数和一页大小
     * @param pageNo 获取数据的页数
     * @return 获取的数据
     */
    Object getData(Object[] params, int pageNo);

    /**
     * 设置获取数据参数的分页大小
     *
     * @param params   数据服务获取数据需传入的参数，与初始参数相比可能会修改总记录数和一页大小
     * @param pageSize
     */
    void setParamsPageSize(Object[] params, int pageSize);

    /**
     *
     * @param params 数据服务获取数据需传入的参数，与初始参数相比可能会修改总记录数和一页大小
     * @param count 需要任务总共需要加载的总记录数
     */
    void setParamsCount(Object[] params, int count);

}
