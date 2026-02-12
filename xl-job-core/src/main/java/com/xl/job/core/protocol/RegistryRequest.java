package com.xl.job.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 执行器注册请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 注册类型: EXECUTOR-执行器注册
     */
    public static final String TYPE_EXECUTOR = "EXECUTOR";

    /**
     * 注册类型
     */
    private String type;

    /**
     * 执行器组名(AppName)
     */
    private String registryGroup;

    /**
     * 执行器标识
     */
    private String registryKey;

    /**
     * 执行器地址 (IP:Port)
     */
    private String registryValue;

    public static RegistryRequest executor(String appname, String address) {
        return RegistryRequest.builder()
                .type(TYPE_EXECUTOR)
                .registryGroup(appname)
                .registryKey(appname)
                .registryValue(address)
                .build();
    }
}
