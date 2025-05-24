package com.zj.metrics.entity;

import lombok.Data;

@Data
public class BugStatusStatisticsDto {

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 状态值
     */
    private Double value;

    /**
     * 时间
     */
    private Long time;
}
