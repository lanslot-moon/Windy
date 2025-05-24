package com.zj.metrics.calculate.strategy;

import lombok.Getter;

@Getter
public enum MetricNameType {
    BUG_TOTAL("缺陷总数"),
    DEMAND_WORKLOAD("需求工时"),
    DEMAND_WAIT_TIME("需求搁置时长"),
    DEMAND_CATEGORY_COUNT("需求个数"),
    DEMAND_CATEGORY_WORKLOAD("需求统计时长"),
    DEMAND_COMPLETE_PERCENT("需求完成率"),
    BUG_COMPLETE_PERCENT("缺陷完成率"),
    DEMAND_OVERDUE_PERCENT("需求逾期率"),
    SYSTEM_HEALTH("系统健康评分"),
    ;

    private final String metricName;

    MetricNameType(String metricName) {
        this.metricName = metricName;
    }
}
