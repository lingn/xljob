package com.xl.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.job.admin.entity.JobInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务信息Mapper
 */
@Mapper
public interface JobInfoMapper extends BaseMapper<JobInfo> {
}
