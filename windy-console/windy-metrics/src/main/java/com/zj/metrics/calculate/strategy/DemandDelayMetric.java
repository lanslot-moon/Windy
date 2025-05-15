package com.zj.metrics.calculate.strategy;

import com.google.common.util.concurrent.AtomicDouble;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.demand.DemandBO;
import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricResultBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import com.zj.domain.repository.demand.IDemandRepository;
import com.zj.domain.repository.metric.IMetricResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DemandDelayMetric extends BaseMetric {

    private static final Logger log = LoggerFactory.getLogger(DemandDelayMetric.class);
    private final IDemandRepository demandRepository;

    public DemandDelayMetric(IDemandRepository demandRepository, UniqueIdService uniqueIdService,
                             IMetricResultRepository metricResultRepository) {
        super(uniqueIdService, metricResultRepository);
        this.demandRepository = demandRepository;
    }

    @Override
    public String getMetricType() {
        return "demand_delay";
    }

    @Override
    public boolean matchMetric(String category, String calcType) {
        return Objects.equals(category, "demand") && Objects.equals(calcType, "delay");
    }

    @Override
    public Integer calculateMetric(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        List<DemandBO> notCompleteDemands = demandRepository.getNotCompleteDemands();
        AtomicDouble demandPeriod = new AtomicDouble(0);
        AtomicDouble delayPeriod = new AtomicDouble(0);
        double dayPeriod = 1000 * 60 * 60 * 24d;
        notCompleteDemands.forEach(demandBO -> {
            long workTime = System.currentTimeMillis() - demandBO.getCreateTime();
            long demandStartTime = Optional.ofNullable(demandBO.getStartTime()).orElse(System.currentTimeMillis());
            long delayTime = demandStartTime - demandBO.getCreateTime();
            double currentPeriod = demandPeriod.addAndGet(round1(workTime/ dayPeriod));
            double currentDelay = delayPeriod.addAndGet(round1(delayTime/ dayPeriod));
            log.info("current demand period={} delay period={}", currentPeriod, currentDelay);
        });
        MetricResultBO workMetricResult = createMetricResult(MetricNameType.DEMAND_WORKLOAD.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), demandPeriod.get());
        MetricResultBO delayMetricResult = createMetricResult(MetricNameType.DEMAND_WAIT_TIME.getMetricName(),
                metricDefinition.getMetricId(), getMetricType(), delayPeriod.get());
        List<MetricResultBO> metricResultList = Arrays.asList(workMetricResult, delayMetricResult);
        boolean batchSaveResult = batchSaveMetric(metricResultList);
        log.info("batch save demand delay metric result={}", batchSaveResult);
        return metricResultList.size();
    }

    private static double round1(double val) {
        return Math.round(val * 10) / 10.0;
    }
}
