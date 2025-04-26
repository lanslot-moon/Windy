package com.zj.metrics.calculate;

import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.po.metric.MetricSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MetricParser {

    private final List<IMetricHandler> metricHandlers;

    public MetricParser(List<IMetricHandler> metricHandlers) {
        this.metricHandlers = metricHandlers;
    }

    public Long startParse(MetricDefinitionBO metricDefinition, List<MetricSource> metricSources) {
        List<IMetricHandler> matchedHandlers = metricHandlers.stream()
                .filter(handler -> handler.matchMetric(metricDefinition.getCalcType()))
                .collect(Collectors.toList());

        AtomicLong totalMetric = new AtomicLong(0);
        matchedHandlers.forEach(metricHandler -> {
            try {
                Long count = metricHandler.calculateMetric(metricSources);
                long currentCount = totalMetric.addAndGet(count);
                log.info("current count={}", currentCount);
            }catch (Exception e){
                log.error("handle current calculate type = {}", metricHandler.getClass().getName(), e);
            }
        });
        return totalMetric.get();
    }
}
