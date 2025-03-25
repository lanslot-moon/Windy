package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.MasterCollectDto;
import com.zj.common.entity.dto.PluginInfo;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.common.entity.dto.ResultEvent;

import java.util.List;

public interface IMasterInvoker {

    /**
     * 执行用例任务
     */
    String runFeatureTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行生成二方包任务
     */
    Boolean runGenerateTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行流水线任务
     */
    String startPipelineTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 获取用例任务执行状态
     */
    ResponseStatusModel getFeatureTaskStatus(String taskRecordId);

    /**
     * 接受来自Client节点的结果通知事件
     * @param resultEvent 通知的结果
     * @return 处理是否成功
     */
    boolean notifyExecuteEvent(ResultEvent resultEvent);

    /**
     * 获取流水线审批记录，用于判断能否继续执行流水线
     * @param recordId 记录ID
     * @return 返回执行记录信息
     */
    ResponseStatusModel getApprovalRecord(String recordId);

    /**
     * 加载可用的用例插件信息，client会定时轮训当前接口以此来完成每个节点的插件安装
     * @return 返回插件信息
     */
    List<PluginInfo> getAvailablePlugins();

    /**
     * 停止执行任务
     * @param dispatchTaskModel 停止任务执行信息
     * @return 返回停止是否成功
     */
    boolean stopDispatchTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 获取master节点监控状态信息
     * @return 返回监控信息
     */
    List<MasterCollectDto> requestMasterMonitor();
}
