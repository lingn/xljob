package com.xl.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型枚举
 */
@Getter
@AllArgsConstructor
public enum GlueTypeEnum {

    BEAN("BEAN", "Bean模式"),
    GLUE_GROOVY("GLUE_GROOVY", "Groovy脚本"),
    GLUE_SHELL("GLUE_SHELL", "Shell脚本"),
    GLUE_PYTHON("GLUE_PYTHON", "Python脚本");

    private final String code;
    private final String desc;

    public static GlueTypeEnum fromCode(String code) {
        for (GlueTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return BEAN;
    }
}
