package com.zj.metrics.entity;

import lombok.Data;

@Data
public class DemandStatisticsDto {

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 需求个数
     */
    private Integer dataValue;

    /**
     * 需求类型
     */
    private String demandType;
}
