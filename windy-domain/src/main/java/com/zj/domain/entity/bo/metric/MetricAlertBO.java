package com.zj.domain.entity.bo.metric;

import lombok.Data;

/**
 * 指标告警
 */
@Data
public class MetricAlertBO {

    private Long id;

    /**
     * 告警规则ID
     */
    private String ruleId;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 计算条件
     */
    private String condition;

    /**
     * 通知通道
     */
    private String notifyChannel;

    /**
     * 通知模版
     */
    private String notifyTemplate;

    private Long createTime;

    private Long updateTime;
}
