package com.zj.domain.entity.bo.metric;

import lombok.Data;

/**
 * 指标定义
 */
@Data
public class MetricDefinitionBO {

    private Long id;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private String category;

    /**
     * 计算方式
     */
    private String calcType;

    /**
     * 计算频次
     */
    private String calcFrequency;

    private Long createTime;

    private Long updateTime;
}
