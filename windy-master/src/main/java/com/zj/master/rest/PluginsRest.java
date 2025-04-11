package com.zj.master.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.feature.PluginInfoBO;
import com.zj.master.service.PluginsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@RestController
@RequestMapping("/v1/devops/master")
public class PluginsRest {

    private final PluginsService pluginsService;

    public PluginsRest(PluginsService pluginsService) {
        this.pluginsService = pluginsService;
    }

    /**
     * 获取所有可用插件
     *
     * @return
     */
    @GetMapping(value = "/plugins")
    public ResponseMeta<List<PluginInfoBO>> getPlugins() {
        return new ResponseMeta(ErrorCode.SUCCESS, pluginsService.getPlugins());
    }

}
