package com.zj.domain.entity.enums;

import lombok.Getter;

import java.util.*;

@Getter
public enum BugStatus {
    NOT_HANDLE(1, "待接受"),
    WORKING(2, "处理中"),
    TESTING(3, "测试中"),
    REJECT(4, "测试驳回"),
    PASS(5, "测试通过"),
    PUBLISHED(6, "已发布"),
    ;
    private final Integer type;
    private final String desc;

    BugStatus(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static List<BugStatus> getNotHandleBugs() {
        return Arrays.asList(NOT_HANDLE, WORKING, REJECT, TESTING);
    }

    public static BugStatus convertType(Integer status) {
        return Arrays.stream(BugStatus.values())
                .filter(bugStatus -> Objects.equals(status, bugStatus.getType())).findAny().orElse(null);
    }
}
