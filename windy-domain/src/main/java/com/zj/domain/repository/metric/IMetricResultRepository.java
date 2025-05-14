package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricResultBO;

import java.util.List;

public interface IMetricResultRepository {

    boolean saveResult(MetricResultBO metricResult);

    boolean batchSaveResult(List<MetricResultBO> metricResultList);
}
