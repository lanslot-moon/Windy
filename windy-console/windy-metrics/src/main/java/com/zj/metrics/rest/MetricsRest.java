package com.zj.metrics.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.metrics.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devops/metric")
public class MetricsRest {

    private final MetricsService metricsService;

    public MetricsRest(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/demand/category")
    public ResponseMeta<Object> getDemandStatistics() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, metricsService.getDemandStatistics());
    }
}
