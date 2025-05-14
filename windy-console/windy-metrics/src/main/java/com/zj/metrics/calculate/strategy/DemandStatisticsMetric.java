package com.zj.metrics.calculate.strategy;

import com.alibaba.fastjson2.JSON;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.repository.metric.IMetricResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 此类用于统计需求统计数据
 */
@Slf4j
@Component
public class DemandStatisticsMetric extends BaseMetric{

    public DemandStatisticsMetric(IMetricResultRepository metricResultRepository, UniqueIdService uniqueIdService) {
        super(uniqueIdService, metricResultRepository);

    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "demand") && Objects.equals(calcType, "top");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        List<MetricSourceBO> filterMetricSources = metricSources.stream()
                .filter(metricSource -> Objects.equals(metricDefinition.getCategory(), metricSource.getDataType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterMetricSources)) {
            log.info("filter metric source data is empty, not calculate category={} calcType={}",
                    metricDefinition.getCategory(), metricDefinition.getCalcType());
            return 0;
        }

        Map<String, List<DemandBO>> splitDemands = filterMetricSources.stream()
                .map(metricSource -> JSON.parseObject(metricSource.getRawJson(), DemandBO.class))
                .filter(Objects::nonNull).filter(demandBO -> StringUtils.isNotBlank(demandBO.getCustomerValue()))
                .collect(Collectors.groupingBy(DemandBO::getCustomerValue));

        List<MetricResultBO> metricResultList= splitDemands.keySet().stream().map(customerValue -> {
            String metricId = metricDefinition.getMetricId();
            List<DemandBO> demandBOS = splitDemands.get(customerValue);
            MetricResultBO countMetric = createMetricResult("需求个数", metricId, customerValue, (double) demandBOS.size());

            double count = demandBOS.stream().mapToInt(DemandBO::getWorkload).sum();
            MetricResultBO workloadMetric = createMetricResult("需求工时",metricId, customerValue, count);
            return Arrays.asList(countMetric, workloadMetric);
        }).flatMap(Collection::stream).collect(Collectors.toList());

        boolean batchSaveResult = batchSaveMetric(metricResultList);
        log.info("batch save demand statistics metric result={}", batchSaveResult);
        return metricResultList.size();
    }
}
