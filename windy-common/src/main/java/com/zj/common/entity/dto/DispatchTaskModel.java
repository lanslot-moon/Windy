package com.zj.common.entity.dto;

import com.zj.common.enums.LogType;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Data
public class DispatchTaskModel {

  /**
   * 执行任务来源Id(测试集合Id，流水线Id，用例任务Id,生成二方包的生成任务Id)
   * 如果是测试集批量执行那么sourceId就是用例Id列表
   * */
  private String sourceId;

  /**
   * 执行任务来源名称
   * */
  private String sourceName;

  /**
   * 任务类型{@link LogType}
   * */
  private Integer type;

  /**
   * 触发源头方
   */
  private String triggerId;

  /**
   * 任务触发人
   */
  private String user;

  public DispatchTaskModel() {
  }

  public DispatchTaskModel(String sourceId, String sourceName, Integer type) {
    this.sourceId = sourceId;
    this.sourceName = sourceName;
    this.type = type;
  }
}
