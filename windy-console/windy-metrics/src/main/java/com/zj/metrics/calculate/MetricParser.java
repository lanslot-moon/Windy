package com.zj.metrics.calculate;

import com.zj.domain.entity.bo.metric.MetricDefinitionBO;
import com.zj.domain.entity.bo.metric.MetricSourceBO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MetricParser {

    private final List<IMetric> metricHandlers;

    public MetricParser(List<IMetric> metricHandlers) {
        this.metricHandlers = metricHandlers;
    }

    public Integer startParse(MetricDefinitionBO metricDefinition, List<MetricSourceBO> metricSources) {
        AtomicInteger totalMetric = new AtomicInteger(0);
        List<IMetric> matchedHandlers = metricHandlers.stream()
                .filter(handler -> handler.matchMetric(metricDefinition.getCategory(), metricDefinition.getCalcType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(matchedHandlers)) {
            log.info("can not find matched handlers,   category={} calcType={}", metricDefinition.getCategory(), metricDefinition.getCalcType());
            return 0;
        }

        matchedHandlers.forEach(metricHandler -> {
            try {
                Integer count = metricHandler.calculateMetric(metricDefinition, metricSources);
                long currentCount = totalMetric.addAndGet(count);
                log.info("current count={}", currentCount);
            } catch (Exception e) {
                log.error("handle current calculate type = {}", metricHandler.getClass().getName(), e);
            }
        });
        return totalMetric.get();
    }
}
