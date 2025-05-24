package com.zj.metrics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthyMetricDto {

    /**
     * 健康值
     */
    private Integer healthyValue;
}
