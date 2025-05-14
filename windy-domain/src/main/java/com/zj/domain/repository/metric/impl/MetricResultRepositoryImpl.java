package com.zj.domain.repository.metric.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.po.metric.MetricResult;
import com.zj.domain.mapper.metric.MetricResultMapper;
import com.zj.domain.repository.metric.IMetricResultRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricResultRepositoryImpl extends ServiceImpl<MetricResultMapper, MetricResult> implements IMetricResultRepository {

    @Override
    public boolean saveResult(MetricResultBO metricResult) {
        MetricResult result = OrikaUtil.convert(metricResult, MetricResult.class);
        result.setCreateTime(System.currentTimeMillis());
        return save(result);
    }

    @Override
    public boolean batchSaveResult(List<MetricResultBO> metricResultList) {
        List<MetricResult> metricResults = OrikaUtil.convertList(metricResultList, MetricResult.class);
        return saveBatch(metricResults);
    }
}
