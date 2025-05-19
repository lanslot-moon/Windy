package com.zj.metrics.entity;

import lombok.Data;

@Data
public class DemandDelayWorkloadDto {

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 延迟天数
     */
    private Double value;

    private Long time;
}
