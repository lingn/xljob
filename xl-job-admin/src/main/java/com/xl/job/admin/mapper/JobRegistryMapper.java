package com.xl.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.job.admin.entity.JobRegistry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行器注册表Mapper
 */
@Mapper
public interface JobRegistryMapper extends BaseMapper<JobRegistry> {
}
