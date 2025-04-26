package com.zj.metrics.calculate;

import com.zj.domain.entity.po.metric.MetricSource;

import java.util.List;

public interface IMetricHandler {

    /**
     * 根据计算类型判断是否满足当前指标计算规则
     * @param calcType 计算方式
     * @return 是否满足当前指标计算规则
     */
    boolean matchMetric(String calcType);

    /**
     * 此方法完成指标体系建设
     * @param metricSources 前一天采集的元数据
     * @return 返回计算的指标个数
     */
    Long calculateMetric(List<MetricSource> metricSources);
}
