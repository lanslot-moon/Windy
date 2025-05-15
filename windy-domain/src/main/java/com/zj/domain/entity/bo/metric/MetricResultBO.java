package com.zj.domain.entity.bo.metric;

import lombok.Data;

/**
 * 指标计算结果
 */
@Data
public class MetricResultBO {

    private Long id;

    /**
     * 指标ID
     */
    private String metricId;

    /**
     * 计算时间
     */
    private Long createTime;

    /**
     * 计算后的值
     */
    private Double value;

    /**
     * 计算结果的名称，用于表示计算的结果类型
     */
    private String resultName;

    /**
     * 计算结果类型
     */
    private String resultType;

    /**
     * 标签计算结果的类型区分
     */
    private String tag;

    /**
     * 计算维度: 1个人 2统计
     */
    private Integer dimension;

    /**
     * 关联ID
     * ，用于记录用户ID或者团队ID
     */
    private String relatedId;
}
