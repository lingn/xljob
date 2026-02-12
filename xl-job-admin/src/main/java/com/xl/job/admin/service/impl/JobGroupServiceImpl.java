package com.xl.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xl.job.admin.entity.JobGroup;
import com.xl.job.admin.entity.JobRegistry;
import com.xl.job.admin.mapper.JobGroupMapper;
import com.xl.job.admin.mapper.JobRegistryMapper;
import com.xl.job.admin.service.JobGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 执行器组服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobGroupServiceImpl extends ServiceImpl<JobGroupMapper, JobGroup> implements JobGroupService {

    private final JobRegistryMapper jobRegistryMapper;

    @Override
    public List<String> getAddressList(String appName) {
        JobGroup group = getByAppName(appName);
        if (group == null) {
            return Collections.emptyList();
        }

        // 手动录入
        if (group.getAddressType() == 1) {
            if (group.getAddressList() == null || group.getAddressList().isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(group.getAddressList().split(","));
        }

        // 自动注册
        LambdaQueryWrapper<JobRegistry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobRegistry::getRegistryGroup, appName);
        List<JobRegistry> registries = jobRegistryMapper.selectList(wrapper);
        return registries.stream()
                .map(JobRegistry::getRegistryValue)
                .collect(Collectors.toList());
    }

    @Override
    public JobGroup getByAppName(String appName) {
        LambdaQueryWrapper<JobGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobGroup::getAppName, appName);
        return getOne(wrapper);
    }
}
