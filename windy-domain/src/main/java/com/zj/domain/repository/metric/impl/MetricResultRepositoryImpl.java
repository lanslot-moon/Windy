package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.metric.MetricResult;
import com.zj.domain.mapper.metric.MetricResultMapper;
import com.zj.domain.repository.metric.IMetricResultRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MetricResultRepositoryImpl extends ServiceImpl<MetricResultMapper, MetricResult> implements IMetricResultRepository {
}
