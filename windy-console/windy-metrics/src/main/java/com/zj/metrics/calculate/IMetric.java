package com.zj.metrics.calculate;

import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;

import java.util.List;

public interface IMetric {

    String getMetricType();

    /**
     * 根据计算类型判断是否满足当前指标计算规则
     *
     * @param category 数据类型
     * @param calcType 计算方式
     * @return 是否匹配当前指标计算规则
     */
    boolean matchMetric(String category, String calcType);

    /**
     * 此方法完成指标体系建设
     *
     * @param metricDefinition 统计指标
     * @param metricSources    前一天采集的元数据
     * @return 返回计算的指标个数
     */
    Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources);
}
