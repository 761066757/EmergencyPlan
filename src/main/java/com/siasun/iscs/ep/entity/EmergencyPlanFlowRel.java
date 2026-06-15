package com.siasun.iscs.ep.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 预案与流程关联表
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
@TableName(value = "emergency_plan_flow_rel")
public class EmergencyPlanFlowRel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
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
     * 流程状态（0-未运行 1-运行中 2-已结束<流程正常走> 3-终止<手动强制中断流程>）
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
    @TableLogic
    private Integer isDeleted;
}