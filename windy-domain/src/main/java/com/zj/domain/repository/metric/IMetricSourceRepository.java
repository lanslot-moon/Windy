package com.zj.domain.repository.metric;

import com.zj.domain.entity.po.metric.MetricSource;

import java.util.List;

public interface IMetricSourceRepository {
    List<MetricSource> loadYesterdaySourceData();
}
