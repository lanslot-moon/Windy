package com.zj.pipeline.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.vo.Create;
import com.zj.domain.entity.vo.Update;
import com.zj.pipeline.entity.dto.PipelineDto;
import com.zj.pipeline.entity.dto.PipelineStatusDto;
import com.zj.pipeline.service.PipelineService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author guyuelan
 * @since 2021/9/28
 * @description: 流水线接口
 */
@RequestMapping("/v1/devops/pipeline")
@RestController
public class PipelineRest {

    private final PipelineService pipelineService;

    public PipelineRest(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @ResponseBody
    @GetMapping("/detail/{pipelineId}")
    public ResponseMeta<PipelineBO> queryPipeline(@PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.getPipelineDetail(pipelineId));
    }

    /**
     * 创建流水线
     * @param pipelineDto 流水线信息
     * @return 流水线Id
     */
    @ResponseBody
    @PostMapping("")
    public ResponseMeta<String> createPipeline(@Validated(Create.class) @RequestBody PipelineDto pipelineDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.createPipeline(pipelineDto));
    }

    @ResponseBody
    @PutMapping("/{service}/{pipelineId}")
    public ResponseMeta<Boolean> updatePipeline(@PathVariable("service") String service,
                                                @PathVariable("pipelineId") String pipelineId,
                                                @Validated(Update.class) @RequestBody PipelineDto pipelineDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.updatePipeline(service, pipelineId, pipelineDto));
    }

    /**
     * 获取指定服务下的流水线列表
     * @param serviceId 应用服务Id
     * @return 流水线列表
     */
    @ResponseBody
    @GetMapping("/{serviceId}/list")
    public ResponseMeta<List<PipelineBO>> listPipelines(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.listPipelines(serviceId));
    }

    @ResponseBody
    @GetMapping("/services/{serviceId}/status")
    public ResponseMeta<List<PipelineStatusDto>> getServicePipelineStatus(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.getServicePipelineStatus(serviceId));
    }

    @ResponseBody
    @GetMapping("/{pipelineId}/status")
    public ResponseMeta<PipelineStatusDto> getPipelineStatus(@PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.getPipelineStatus(pipelineId));
    }

    @ResponseBody
    @DeleteMapping("/{service}/{pipelineId}")
    public ResponseMeta<Boolean> deletePipeline(@PathVariable("service") String service,
                                                @PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.deletePipeline(service, pipelineId));
    }

    /**
     * 运行指定流水线
     * @param pipelineId 流水线Id
     * @return
     */
    @ResponseBody
    @PostMapping("/{pipelineId}")
    public ResponseMeta<String> execute(@PathVariable("pipelineId") String pipelineId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.execute(pipelineId));
    }

    @ResponseBody
    @PutMapping("/{historyId}/pause")
    public ResponseMeta<Boolean> pause(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, pipelineService.pause(historyId));
    }
}
