package com.zj.metrics.calculate.strategy;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.metric.IMetricResultRepository;
import lombok.extern.slf4j.Slf4j;
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

    private final IDemandRepository demandRepository;

    public DemandStatisticsMetric(IMetricResultRepository metricResultRepository, UniqueIdService uniqueIdService,
                                  IDemandRepository demandRepository) {
        super(uniqueIdService, metricResultRepository);

        this.demandRepository = demandRepository;
    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "demand") && Objects.equals(calcType, "top");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        List<DemandBO> allDemands = demandRepository.getAllDemands();
        Map<String, List<DemandBO>> splitDemands = allDemands.stream()
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
