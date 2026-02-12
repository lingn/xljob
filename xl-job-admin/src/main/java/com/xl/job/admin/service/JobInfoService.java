package com.xl.job.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xl.job.admin.entity.JobInfo;

import java.util.List;

/**
 * 任务信息服务接口
 */
public interface JobInfoService extends IService<JobInfo> {

    /**
     * 获取所有运行中的任务
     *
     * @return 任务列表
     */
    List<JobInfo> listRunningJobs();

    /**
     * 启动任务
     *
     * @param id 任务ID
     * @return 是否成功
     */
    boolean startJob(Long id);

    /**
     * 停止任务
     *
     * @param id 任务ID
     * @return 是否成功
     */
    boolean stopJob(Long id);
}
