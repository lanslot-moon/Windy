package com.zj.metrics.calculate.strategy;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.entity.enums.DemandStatus;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.metric.IMetricResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DemandStatusMetric extends BaseMetric {

    private final IDemandRepository demandRepository;

    public DemandStatusMetric(IMetricResultRepository metricResultRepository, IDemandRepository demandRepository, UniqueIdService uniqueIdService) {
        super(uniqueIdService, metricResultRepository);
        this.demandRepository = demandRepository;
    }

    @Override
    public String getMetricType() {
        return "demand_status";
    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "demand") && Objects.equals(calcType, "top_status");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        List<DemandBO> allDemands = demandRepository.getAllDemands();
        Map<Integer, List<DemandBO>> statusMap = allDemands.stream().collect(Collectors.groupingBy(DemandBO::getStatus));

        List<MetricResultBO> metricResults = statusMap.keySet().stream().map(status -> {
            DemandStatus demandStatus = DemandStatus.convertType(status);
            if (Objects.isNull(demandStatus)) {
                log.info("can not find demand status={}", status);
                return null;
            }
            List<DemandBO> demands = statusMap.get(status);
            double percent =Optional.of(demands).filter(CollectionUtils::isNotEmpty)
                    .map(list -> (list.size() * 100d) / allDemands.size()).orElse(0d);
            return createMetricResult(demandStatus.getDesc(), metricDefinition.getMetricId(), getMetricType(), percent);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        boolean batchSave = batchSaveMetric(metricResults);
        log.info("batch save demand status metric result={}", batchSave);
        return metricResults.size();
    }
}
