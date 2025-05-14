package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum DemandStatus {
    NOT_HANDLE(1,"未处理"),
    ACCEPTED(2,"已接受"),
    WORKING(3, "处理中"),
    WAIT_TEST(4, "待验收"),
    PUBLISHED(5, "已发布"),
    REFUSED(6, "已拒绝"),
    ;
    private final Integer type;
    private final String desc;

    DemandStatus(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static List<DemandStatus> getNotHandleDemands() {
        return Arrays.asList(NOT_HANDLE, WORKING, ACCEPTED, WAIT_TEST);
    }

    public static DemandStatus convertType(Integer status) {
        return Arrays.stream(DemandStatus.values())
                .filter(demandStatus -> Objects.equals(status, demandStatus.getType())).findAny().orElse(null);
    }

}
