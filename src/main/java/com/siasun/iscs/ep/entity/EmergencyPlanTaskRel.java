package com.siasun.iscs.ep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 预案与任务关联表
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
@TableName("emergency_plan_task_rel")
public class EmergencyPlanTaskRel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 车站
     */
    private String station;

    /**
     * 预案ID
     */
    private String planId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * Flowable节点ID
     */
    private String flowNodeId;
    
    /**
     * Flowable节点名称
     */
    private String flowNodeName;

    /**
     * 任务执行补充说明
     */
    private String executeNote;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 删除标记 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}