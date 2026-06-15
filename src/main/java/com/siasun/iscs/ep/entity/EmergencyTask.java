package com.siasun.iscs.ep.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description: 任务表
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */
@Data
@TableName("emergency_task")
public class EmergencyTask {
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
     * 任务编码
     */
    private Integer taskCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 是否允许跳过：0-否，1-是
     */
    private Integer isSkippable;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;
}