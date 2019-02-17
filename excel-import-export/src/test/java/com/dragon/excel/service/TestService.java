package com.dragon.excel.service;

import com.dragon.excel.entity.TestEntity;
import com.dragon.excel.entity.TestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestService {
    public List<TestEntity> findList(TestParam param) {
        return findList(param.getPageNo(), param.getPageSize(), param.getCount());
    }

    public List<TestEntity> findList(int pageNo, int pageSize, int count) {
        System.out.println(String.format("pageNo=%d,pageSize=%d,count=%d", pageNo, pageSize, count));
        List<TestEntity> list = new ArrayList<>();
        int start = (pageNo - 1) * pageSize + 1, end = (count > pageSize * pageNo ? pageSize * pageNo : count);
        TestEntity test;
        for (; start <= end; start++) {
            test = new TestEntity();
            test.setId(start);
            test.setCityPos("销售城市");
            test.setCountRequests("申请团队");
            test.setCountPassedRequests("证实团队");
            test.setCountCanceledRequests("撤回团队");
            list.add(test);
        }
        return list;
    }

    public Map<String, List<TestEntity>> findListMap(TestParam param) {
        Map<String, List<TestEntity>> map = new HashMap<>();
        map.put("Export" + param.getPageNo(), findList(param.getPageNo(), param.getPageSize(), param.getCount()));
        return map;
    }
}
