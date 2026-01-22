package com.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预案-流程实例关联表（核心：关联预案ID和Flowable流程实例）
 */
@Data
@TableName(value = "emergency_plan_flow_rel")
public class EmergencyPlanFlowRel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    // 预案ID（关联emergency_plan.id）一个预案对应一个流程实例
    private String planId;
    // Flowable部署ID
    private String deployId;
    // Flowable流程实例ID
    private String procInstId;
    // 当前待办任务ID
    private String currentTaskId;
    // 当前待办任务名称
    private String currentTaskName;
    // 1-运行中 2-已结束 3-终止
    private Integer flowStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}