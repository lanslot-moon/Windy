package com.zj.domain.entity.bo.metric;

import lombok.Data;

/**
 * 指标数据来源
 */
@Data
public class MetricSourceBO {

    private Long id;

    /**
     * 来源系统
     */
    private String sourceSystem;


    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 原始数据
     */
    private String rawJson;

    /**
     * 触发时间
     */
    private String createTime;
}
