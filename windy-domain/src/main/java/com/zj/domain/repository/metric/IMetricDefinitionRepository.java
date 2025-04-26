package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricDefinitionBO;

import java.util.List;

public interface IMetricDefinitionRepository {
    List<MetricDefinitionBO> getAllMetricDefinitions();
}
