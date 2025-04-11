package com.zj.pipeline.rest;

import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.pipeline.service.PipelineActionService;
import org.springframework.web.bind.annotation.*;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class PipelineActionRest {

    private final PipelineActionService service;

    public PipelineActionRest(PipelineActionService service) {
        this.service = service;
    }

    @PostMapping("/actions")
    public ResponseMeta<Boolean> createAction(@RequestBody PipelineActionBO actionDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.createAction(actionDto));
    }

    @PutMapping("/action")
    public ResponseMeta<Boolean> updateAction(@RequestBody PipelineActionBO actionDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.updateAction(actionDto));
    }

    @GetMapping("/action/{actionId}")
    public ResponseMeta<PipelineActionBO> getAction(@PathVariable("actionId") String actionId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, service.getAction(actionId));
    }

    @DeleteMapping("/action/{actionId}")
    public ResponseMeta<Boolean> deleteAction(@PathVariable("actionId") String actionId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, service.deleteAction(actionId));
    }

    @GetMapping("/actions")
    public PageSize<PipelineActionBO> getActions(@RequestParam("page") Integer page,
                                                 @RequestParam("size") Integer size, @RequestParam("name") String name) {
        return service.getActions(page, size, name);
    }
}
