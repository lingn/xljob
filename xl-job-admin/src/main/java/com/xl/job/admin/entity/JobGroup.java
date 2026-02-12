package com.xl.job.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行器组实体
 */
@Data
@TableName("xl_job_group")
public class JobGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 执行器AppName
     */
    private String appName;

    /**
     * 执行器名称
     */
    private String title;

    /**
     * 地址类型:0-自动注册,1-手动录入
     */
    private Integer addressType;

    /**
     * 执行器地址列表(逗号分隔)
     */
    private String addressList;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
