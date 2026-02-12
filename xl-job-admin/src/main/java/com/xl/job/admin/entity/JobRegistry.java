package com.xl.job.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 执行器注册表实体
 */
@Data
@TableName("xl_job_registry")
public class JobRegistry {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 执行器组名
     */
    private String registryGroup;

    /**
     * 执行器标识
     */
    private String registryKey;

    /**
     * 执行器地址(IP:Port)
     */
    private String registryValue;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
