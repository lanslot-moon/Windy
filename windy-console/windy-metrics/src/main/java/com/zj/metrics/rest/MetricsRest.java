package com.zj.metrics.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.metrics.entity.*;
import com.zj.metrics.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops/metric")
public class MetricsRest {

    private final MetricsService metricsService;

    public MetricsRest(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/demand/statistics")
    public ResponseMeta<List<DemandStatisticsDto>> getDemandStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandStatistics());
    }

    @GetMapping("/demand/status/statistics")
    public ResponseMeta<List<DemandStatusStatisticsDto>> getDemandStatusStatistics(){
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandStatusStatistics());
    }

    @GetMapping("/system/healthy")
    public ResponseMeta<HealthyMetricDto> getSystemHealthy() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getSystemHealthy());
    }

    @GetMapping("/demand/workload")
    public ResponseMeta<List<DemandDelayWorkloadDto>> getDemandWorkload() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandWorkload());
    }

    @GetMapping("/bug/statistics")
    public ResponseMeta<List<BugStatusStatisticsDto>> getBugStatusStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getBugStatusStatistics());
    }

    @GetMapping("/healthy/statistics")
    public ResponseMeta<List<HealthyStatisticsDto>> getSystemHealthyStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getSystemHealthyStatistics());
    }
}
