package com.zj.metrics.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MetricUtils {

    /**
     * double数据只保留1位
     */
    public static double scaleTo1Decimal(Double time) {
        return new BigDecimal(time).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
