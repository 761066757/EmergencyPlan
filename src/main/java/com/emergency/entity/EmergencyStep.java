package com.emergency.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 步骤配置表
 */
@Data
@TableName("emergency_step")
public class EmergencyStep {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 步骤编码
     */
    private String stepCode;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤类型（预警/处置/上报/评估）
     */
    private String stepType;

    /**
     * 步骤描述
     */
    private String stepDesc;

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