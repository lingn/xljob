package com.xl.job.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xl.job.admin.entity.JobGroup;

import java.util.List;

/**
 * 执行器组服务接口
 */
public interface JobGroupService extends IService<JobGroup> {

    /**
     * 获取执行器组的所有地址
     *
     * @param appName 执行器AppName
     * @return 地址列表
     */
    List<String> getAddressList(String appName);

    /**
     * 根据AppName获取执行器组
     *
     * @param appName 执行器AppName
     * @return 执行器组
     */
    JobGroup getByAppName(String appName);
}
