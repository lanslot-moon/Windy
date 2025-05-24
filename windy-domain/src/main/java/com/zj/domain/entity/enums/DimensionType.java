package com.zj.domain.entity.enums;

import lombok.Getter;

@Getter
public enum DimensionType {
    PERSONAL(1), //个人维度
    STATISTICS(2); //全量统计

    DimensionType(Integer type) {
        this.type = type;
    }

    private final Integer type;
}
