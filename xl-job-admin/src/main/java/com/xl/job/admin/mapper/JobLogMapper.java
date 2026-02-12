package com.xl.job.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xl.job.admin.entity.JobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行日志Mapper
 */
@Mapper
public interface JobLogMapper extends BaseMapper<JobLog> {
}
