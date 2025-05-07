package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.metric.MetricSource;
import com.zj.domain.mapper.metric.MetricSourceMapper;
import com.zj.domain.repository.metric.IMetricSourceRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class MetricSourceRepositoryImpl extends ServiceImpl<MetricSourceMapper, MetricSource> implements IMetricSourceRepository {
    @Override
    public List<MetricSource> loadYesterdaySourceData() {
        return Collections.emptyList();
    }
}
