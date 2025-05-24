package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricResultBO;

import java.util.List;

public interface IMetricResultRepository {

    boolean batchSaveResult(List<MetricResultBO> metricResultList);

    MetricResultBO getLatestMetricByType(String tag, String resultName);

    List<MetricResultBO> getDemandDelayWorkLoadByTag(String demandDelay, long startTime);
}
