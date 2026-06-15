package com.siasun.iscs.ep.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @description: 预案与流程关联DTO
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
public class EmergencyPlanFlowRelDTO {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 车站
     */
    private String station;

    /**
     * 预案ID（一个预案对应一个流程实例）
     */
    private String planId;

    /**
     * Flowable部署ID
     */
    private String deployId;

    /**
     * Flowable流程实例ID（当前）
     */
    private String curProcInstId;

    /**
     * 当前待办任务ID
     */
    private String currentTaskId;

    /**
     * 当前待办任务名称
     */
    private String currentTaskName;

    /**
     * 流程状态（1-运行中 2-已结束 3-终止）
     */
    private Integer flowStatus;

    /**
     * 操作人
     */
    private String optName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     */
    private Integer isDeleted;
}
