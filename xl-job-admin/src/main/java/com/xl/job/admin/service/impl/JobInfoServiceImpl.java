package com.xl.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.job.admin.entity.JobInfo;
import com.xl.job.admin.mapper.JobInfoMapper;
import com.xl.job.admin.service.JobInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo> implements JobInfoService {

    @Override
    public List<JobInfo> listRunningJobs() {
        LambdaQueryWrapper<JobInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobInfo::getStatus, 1);
        return list(wrapper);
    }

    @Override
    public boolean startJob(Long id) {
        LambdaUpdateWrapper<JobInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobInfo::getId, id)
                .set(JobInfo::getStatus, 1);
        return update(wrapper);
    }

    @Override
    public boolean stopJob(Long id) {
        LambdaUpdateWrapper<JobInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(JobInfo::getId, id)
                .set(JobInfo::getStatus, 0);
        return update(wrapper);
    }
}
