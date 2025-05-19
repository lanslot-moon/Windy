package com.zj.service.entity;

import lombok.Data;

import java.util.List;

@Data
public class GenerateParams {

    /**
     * 需要生成二方包的API列表
     */
    private List<String> apiIds;

    /**
     * 服务Id
     */
    private String serviceId;

    /**
     * 打包的包名路径
     */
    private String packageName;

    /**
     * maven打包的版本
     */
    private String version;

    /**
     * jar包groupId
     */
    private String groupId;

    /**
     * jar包artifactId
     */
    private String artifactId;
}
