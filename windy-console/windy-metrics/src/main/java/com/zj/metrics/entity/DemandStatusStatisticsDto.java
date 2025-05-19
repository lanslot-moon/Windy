package com.zj.metrics.entity;

import lombok.Data;

@Data
public class DemandStatusStatisticsDto {

    /**
     * 状态描述
     */
    private String statusName;

    /**
     * 状态百分比
     */
    private Double percent;
}
