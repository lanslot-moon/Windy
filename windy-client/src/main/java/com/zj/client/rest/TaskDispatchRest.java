package com.zj.client.rest;

import com.zj.client.entity.dto.GenerateDto;
import com.zj.client.handler.feature.executor.vo.FeatureParam;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.service.TaskDispatchService;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.dto.StopDispatch;
import com.zj.common.exception.ErrorCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@RestController
@RequestMapping("/v1/client")
public class TaskDispatchRest {

  private final TaskDispatchService taskDispatchService;

  public TaskDispatchRest(TaskDispatchService taskDispatchService) {
    this.taskDispatchService = taskDispatchService;
  }

  /**
   * api管理-二方包生成
   * @param generate
   * @return
   */
  @PostMapping("/dispatch/generate")
  public ResponseMeta<Boolean> dispatchGenerate(@RequestBody GenerateDto generate) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runGenerate(generate));
  }

  @PostMapping("/dispatch/feature")
  public ResponseMeta<Boolean> dispatchFeature(@RequestBody FeatureParam featureParam) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runFeature(featureParam));
  }

  /**
   * 运行任务节点 一个流水线任务可以分为构建,审批,执行,测试,发布等节点
   * @param taskNode
   * @return
   */
  @PostMapping("/dispatch/pipeline")
  public ResponseMeta<Boolean> createTask(@RequestBody TaskNode taskNode) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runPipeline(taskNode));
  }

  @PutMapping("/task/stop")
  public ResponseMeta<Boolean> stopTask(@RequestBody StopDispatch stopDispatch) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.stopDispatch(stopDispatch));
  }
}
