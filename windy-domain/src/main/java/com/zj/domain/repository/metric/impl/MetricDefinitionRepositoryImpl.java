package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.domain.entity.po.metric.MetricDefinition;
import com.zj.domain.mapper.metric.MetricDefinitionMapper;
import com.zj.domain.repository.metric.IMetricDefinitionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MetricDefinitionRepositoryImpl extends ServiceImpl<MetricDefinitionMapper, MetricDefinition> implements IMetricDefinitionRepository {
}
