package com.zj.domain.repository.metric;

import com.zj.domain.entity.bo.metric.MetricSourceBO;

import java.util.List;

public interface IMetricSourceRepository {
    List<MetricSourceBO> loadYesterdaySourceData();
}
