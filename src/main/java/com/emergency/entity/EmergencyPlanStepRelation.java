package com.emergency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预案-步骤关联实体
 */
@Data
@TableName("emergency_plan_step_relation")
public class EmergencyPlanStepRelation {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String planId;
    private String stepId;
    private String flowNodeId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}