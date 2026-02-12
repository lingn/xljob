package com.xl.job.admin.router;

import java.util.List;

/**
 * 路由策略接口
 */
public interface Router {

    /**
     * 路由选择
     *
     * @param addressList 执行器地址列表
     * @param jobId       任务ID
     * @return 选中的执行器地址
     */
    String route(List<String> addressList, Long jobId);
}
