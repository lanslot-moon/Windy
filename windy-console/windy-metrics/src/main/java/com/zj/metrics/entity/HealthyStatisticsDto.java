package com.zj.metrics.entity;

import lombok.Data;

@Data
public class HealthyStatisticsDto {

    /**
     * 健康度评估类型
     */
    private String typeName;

    /**
     * 健康度评估值
     */
    private Double value;

    /**
     * 评估时间
     */
    private Long time;
}
