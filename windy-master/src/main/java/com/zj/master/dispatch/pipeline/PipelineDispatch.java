package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.pipeline.ConfigDetail;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.entity.bo.pipeline.*;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.*;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.pipeline.builder.RefreshContextBuilder;
import com.zj.master.dispatch.pipeline.builder.RequestContextBuilder;
import com.zj.master.entity.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/15
 * 流水线分发执行器
 */
@Slf4j
@Component
public class PipelineDispatch implements IDispatchExecutor {

    private final IPipelineRepository pipelineRepository;
    private final IPipelineNodeRepository pipelineNodeRepository;
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final IPipelineActionRepository pipelineActionRepository;
    private final UniqueIdService uniqueIdService;
    private final PipelineExecuteProxy pipelineExecuteProxy;
    private final ISubDispatchLogRepository subDispatchLogRepository;
    private final IDispatchLogRepository dispatchLogRepository;
    private final IBindBranchRepository bindBranchRepository;

    public PipelineDispatch(IPipelineRepository pipelineRepository,
                            IPipelineNodeRepository pipelineNodeRepository,
                            IPipelineHistoryRepository pipelineHistoryRepository,
                            IPipelineActionRepository pipelineActionRepository, UniqueIdService uniqueIdService,
                            PipelineExecuteProxy pipelineExecuteProxy, ISubDispatchLogRepository subDispatchLogRepository,
                            IDispatchLogRepository dispatchLogRepository, IBindBranchRepository bindBranchRepository) {
        this.pipelineRepository = pipelineRepository;
        this.pipelineNodeRepository = pipelineNodeRepository;
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.pipelineActionRepository = pipelineActionRepository;
        this.uniqueIdService = uniqueIdService;
        this.pipelineExecuteProxy = pipelineExecuteProxy;
        this.subDispatchLogRepository = subDispatchLogRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.bindBranchRepository = bindBranchRepository;
    }

    @Override
    public LogType type() {
        return LogType.PIPELINE;
    }

    @Override
    public boolean isExistInJvm(DispatchLogBO taskLog) {
        return pipelineExecuteProxy.isExitTask(taskLog.getSourceRecordId());
    }

    @Override
    public String dispatch(DispatchTaskModel task, String logId) {
        // 获取到对应的流水线信息
        PipelineBO pipeline = pipelineRepository.getPipeline(task.getSourceId());
        if (Objects.isNull(pipeline)) {
            log.info("can not find pipeline name={} pipelineId={}", task.getSourceName(), task.getSourceId());
            return null;
        }

        // 获取到所有的自定义节点(排除掉了开始节点和结束节点的其余节点)
        List<PipelineNodeBO> pipelineNodes = pipelineNodeRepository.getPipelineNodes(pipeline.getPipelineId());
        if (CollectionUtils.isEmpty(pipelineNodes)) {
            log.info("can not find pipeline nodes name={} pipelineId={}", task.getSourceName(), task.getSourceId());
            return null;
        }

        // 记录流水线运行状态
        String historyId = uniqueIdService.getUniqueId();
        saveHistory(pipeline.getPipelineId(), historyId);
        log.info("start run pipeline={} name={} historyId={}", task.getSourceId(), task.getSourceName(), historyId);

        // 更新日志记录的源记录ID
        dispatchLogRepository.updateLogSourceRecord(logId, historyId);

        // 创建流水线任务对象
        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.setPipelineId(pipeline.getPipelineId());
        pipelineTask.setHistoryId(historyId);
        pipelineTask.setLogId(logId);

        // 将流水线节点转换为任务节点，并按排序顺序排序
        List<TaskNode> taskNodeList = pipelineNodes.stream()
                .sorted(Comparator.comparing(PipelineNodeBO::getSortOrder))
                .map(node -> buildTaskNode(node, historyId, pipeline.getServiceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        pipelineTask.addAll(taskNodeList);
        // 收集任务节点的执行类型
        Map<String, String> executeTypeMap = taskNodeList.stream()
                .collect(Collectors.toMap(TaskNode::getNodeId, TaskNode::getExecuteType));

        // 创建子任务日志
        createSubTaskLog(pipelineNodes, pipelineTask.getLogId(), executeTypeMap);
        // 执行流水线任务
        pipelineExecuteProxy.runTask(pipelineTask);
        return historyId;
    }

    private void createSubTaskLog(List<PipelineNodeBO> pipelineNodes, String logId,
                                  Map<String, String> executeTypeMap) {
        List<SubDispatchLogBO> logList = pipelineNodes.stream().map(pipelineNode -> {
            SubDispatchLogBO subTaskLog = new SubDispatchLogBO();
            subTaskLog.setSubTaskId(uniqueIdService.getUniqueId());
            subTaskLog.setSubTaskName(pipelineNode.getNodeName());
            subTaskLog.setLogId(logId);
            subTaskLog.setExecuteId(pipelineNode.getNodeId());
            subTaskLog.setExecuteType(executeTypeMap.get(pipelineNode.getNodeId()));
            subTaskLog.setStatus(ProcessStatus.RUNNING.getType());
            long dateNow = System.currentTimeMillis();
            subTaskLog.setCreateTime(dateNow);
            subTaskLog.setUpdateTime(dateNow);
            return subTaskLog;
        }).collect(Collectors.toList());
        subDispatchLogRepository.batchSaveLogs(logList);
    }

    private void saveHistory(String pipelineId, String historyId) {
        PipelineHistoryBO pipelineHistory = new PipelineHistoryBO();
        pipelineHistory.setHistoryId(historyId);
        pipelineHistory.setPipelineId(pipelineId);
        pipelineHistory.setPipelineStatus(ProcessStatus.RUNNING.getType());
        pipelineHistory.setPipelineConfig(null);
        pipelineHistory.setCreateTime(System.currentTimeMillis());
        pipelineHistory.setUpdateTime(System.currentTimeMillis());
        BindBranchBO bindBranch = bindBranchRepository.getPipelineBindBranch(pipelineId);
        pipelineHistory.setBranch(bindBranch.getGitBranch());
        pipelineHistoryRepository.createPipelineHistory(pipelineHistory);
    }


    private TaskNode buildTaskNode(PipelineNodeBO pipelineNode, String historyId, String serviceId) {
        log.info("start build taskNode ={}", JSON.toJSONString(pipelineNode));
        TaskNode taskNode = new TaskNode();
        taskNode.setNodeId(pipelineNode.getNodeId());
        taskNode.setName(pipelineNode.getNodeName());
        taskNode.setExecuteTime(System.currentTimeMillis());
        taskNode.setHistoryId(historyId);
        taskNode.setServiceId(serviceId);

        ConfigDetail configDetail = pipelineNode.getConfigDetail();
        PipelineActionBO action = pipelineActionRepository.getAction(configDetail.getActionId());
        if (Objects.isNull(action)) {
            return null;
        }
        taskNode.setExecuteType(action.getExecuteType());

        ActionDetail actionDetail = new ActionDetail(configDetail, action);
        RequestContext requestContext = RequestContextBuilder.createContext(actionDetail);
        requestContext.setPipelineId(pipelineNode.getPipelineId());
        taskNode.setRequestContext(requestContext);

        RefreshContext refreshContext = RefreshContextBuilder.createContext(actionDetail);
        taskNode.setRefreshContext(refreshContext);

        NodeConfig nodeConfig = new NodeConfig();
        taskNode.setNodeConfig(nodeConfig);
        return taskNode;
    }

    @Override
    public boolean resume(DispatchLogBO dispatchLog) {
        String pipelineId = dispatchLog.getSourceId();
        PipelineBO pipeline = pipelineRepository.getPipeline(pipelineId);
        if (Objects.isNull(pipeline)) {
            log.info("resume task not find pipeline name={} pipelineId={}", dispatchLog.getSourceName(),
                    pipelineId);
            return false;
        }

        List<PipelineNodeBO> pipelineNodes = pipelineNodeRepository.getPipelineNodes(
                pipeline.getPipelineId());
        if (CollectionUtils.isEmpty(pipelineNodes)) {
            log.info("can not find pipeline nodes name={} pipelineId={}", dispatchLog.getSourceName(),
                    pipelineId);
            return false;
        }

        BindBranchBO pipelineBindBranch = bindBranchRepository.getPipelineBindBranch(pipelineId);
        if (Objects.isNull(pipelineBindBranch)) {
            log.info("can not find pipeline bind branch pipelineId={}", pipelineId);
            return false;
        }
        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.setPipelineId(pipeline.getPipelineId());
        pipelineTask.setLogId(dispatchLog.getLogId());
        if (StringUtils.isBlank(dispatchLog.getSourceRecordId())) {
            String historyId = uniqueIdService.getUniqueId();
            saveHistory(pipelineId, historyId);
            dispatchLog.setSourceRecordId(historyId);
        }

        //过滤掉已经执行完成的任务
        List<SubDispatchLogBO> subLogs = subDispatchLogRepository.getSubLogByLogId(
                dispatchLog.getLogId());
        List<String> subTasks = subLogs.stream()
                .filter(subTask -> !ProcessStatus.isCompleteStatus(subTask.getStatus()))
                .map(SubDispatchLogBO::getExecuteId).collect(Collectors.toList());
        List<TaskNode> taskNodeList = pipelineNodes.stream()
                .filter(node -> subTasks.contains(node.getNodeId()))
                .map(node -> buildTaskNode(node, dispatchLog.getSourceRecordId(), pipeline.getServiceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        pipelineTask.setHistoryId(dispatchLog.getSourceRecordId());
        pipelineTask.addAll(taskNodeList);

        if (CollectionUtils.isEmpty(subLogs)) {
            //如果找不到子任务执行记录，那么就需要重新创建
            Map<String, String> executeTypeMap = taskNodeList.stream()
                    .collect(Collectors.toMap(TaskNode::getNodeId, TaskNode::getExecuteType));
            createSubTaskLog(pipelineNodes, dispatchLog.getLogId(), executeTypeMap);
        }
        pipelineExecuteProxy.runTask(pipelineTask);
        return true;
    }

    @Override
    public Integer getExecuteCount() {
        return pipelineExecuteProxy.getTaskSize();
    }
}
