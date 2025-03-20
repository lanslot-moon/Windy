package com.zj.metrics.calculate;

import com.zj.domain.repository.metric.IMetricSourceRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetricScanSchedule {

    private final IMetricSourceRepository metricSourceRepository;

    public MetricScanSchedule(IMetricSourceRepository metricSourceRepository) {
        this.metricSourceRepository = metricSourceRepository;
    }

    @Scheduled(cron = "0 */10 * * * ?")
    public void scanMetrics() {
    }
}
